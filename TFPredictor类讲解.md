# TFPredictor 类讲解

> 类位置：[TFPredictor.java](mkt_push_engine_server_web/src/main/java/com/qunar/marketing/mkt/pub/alg/tfserving/push/engine/server/service/utils/Predictors/tf/TFPredictor.java)
> 接口：实现 `IPredictor`（`init` / `warmUp` / `predict` / `close` 四方法）
> 职责：**加载模型 → 预热 → 接收请求做预测 → 释放资源**

---

## 一、字段（对象状态，4 个）

| 字段 | 类型 | 说明 |
|---|---|---|
| `modelConfig` | `ModelConfig` | 模型配置（路径、签名解析出的输入输出形状/dtype、yml 配置） |
| `model` | `SavedModelBundle` | 加载到内存的 TF 模型 |
| `session` | `Session` | TF 会话，推理的执行器 |
| `signatures` | `List<Signature>` | 模型签名（定义有哪些输入输出、各自 dtype/shape） |

---

## 二、生命周期方法（IPredictor 接口，外部调用）

### 1. `init(ModelConfig)` — 加载阶段

```
init → SavedModelBundle.load → 取 session/signatures → initModelConfig → checkModelConfig
```

- 加载 `saved_model` 目录到内存，拿到 session 和 signatures。
- 调 `initModelConfig()` 解析配置。
- 调 `checkModelConfig()` 校验 yml 配置和签名是否一致，不一致直接抛异常阻止加载。
- 整个包在 try-catch 里，任何失败转成 RuntimeException。

### 2. `warmUp()` — 预热阶段

```
warmUp → MockData.floats/ints/strings → TensorBuilder.build* → runner.feed → runner.fetch → runner.run → close 所有 Tensor
```

- 用假数据跑一次推理，触发 TF 引擎的**图编译**和**内存预分配**，避免首请求冷启动慢。
- **输出结果丢弃**（try 体为空），只在 finally 释放所有 Tensor。

### 3. `predict(PredictParam)` — 核心推理阶段

```
predict → convertToTensor → runner.feed → runner.fetch → runner.run → convertTensorFloatArray → 封装 PredictResult
```

数据流：

1. `convertToTensor(param)` 把请求参数转成输入张量 map
2. 喂给 runner（feed）、声明要取的输出（fetch）
3. `runner.run()` 执行推理拿到输出张量
4. 每个输出张量用 `convertTensorFloatArray` 转成 `float[][]`
5. 封进 `PredictResult` 返回，带 `predictTimeMs` 耗时元数据
6. 异常 → `log.error` + 返回 `PredictResult.failure`
7. **finally 释放所有 Tensor**（输入在 outer finally，输出在内层 finally），防内存泄漏

### 4. `close()` — 卸载阶段

```
close → session.close() → model.close()
```

释放 TF 会话和模型。⚠️ 目前没人调用它（REFACTOR_CHECKLIST B3），等热更新功能才用。

---

## 三、配置解析方法（init 的内部步骤，私有）

### `initModelConfig()` — 编排器

```
initModelConfig → parseSignature() → loadTensorYml() → loadFeaturesYml()
```

自身只调三个方法，把签名信息和两个 yml 配置全部回填到 `this.modelConfig`（共享可变对象）。

### `parseSignature()` — 解析模型签名

从 `signatures` 提取每个输入输出的 **dtype、shape、name**，填进 modelConfig 的 `inputMap/outputMap/dataTypes/inputShapes/outputShapes`。

- name 形如 `serving_default_input:0`，用 `split(":")` 取冒号前的部分作 key。

### `loadTensorYml()` — 读 tensor.yml

解析 `tensor.yml`：哪些输入、每个输入由哪些特征拼成、输出叫什么。填 `tensorModelConfig/inputToFeaturesMap/outputNames`。

- `inputToFeaturesMap` 是**输入张量名 → [特征名列表]** 的映射，`convertToTensor` 用它知道每个输入要喂哪些特征。

### `loadFeaturesYml()` — 读 features.yml

解析 `features.yml`：特征组到特征名、dtype code 的映射。填 `featuresKey/featuresDtype`。这块主要给特征平台用，TFPredictor 自己不直接消费。

### `checkModelConfig()` — 校验一致性

检查 tensor.yml 声明的输入输出名是否都在模型签名里。任一不在 → 返回 false → `init` 抛异常阻止加载。

---

## 四、张量转换方法（predict 的内部步骤，私有）

### `convertToTensor(PredictParam)` — 输入转换

```
convertToTensor → (遍历 inputToFeaturesMap) → 按签名 dtype 判分支 → collectSubMatrices → concatColumns* → TensorBuilder.build*
```

对每个模型输入：

1. 按签名声明的 dtype `dataTypes.get(inputName)` 决定走 float/string/int 哪个分支（**显式按签名 dtype 判**，非隐式推断）
2. `collectSubMatrices` 把该输入所需的所有特征子矩阵从对应表收集起来，任一缺失抛 `IllegalArgumentException`
3. 用 `concatColumns*` **按列拼接**成一个二维数组（多个特征拼成一个大矩阵）
4. 用 `TensorBuilder.build*` 转成 TF 张量

异常处理：dtype 为 null / 不支持（非 DT_FLOAT/DT_STRING/DT_INT32）/ 特征数据缺失 → 抛 `IllegalArgumentException`，由 `predict` 的 catch 转成 `PredictResult.failure`。

### `collectSubMatrices` — 特征数据收集与缺失校验（私有泛型）

```
collectSubMatrices → 遍历特征名 → 从对应子矩阵表取数据 → 缺失抛异常 → 返回数据列表
```

泛型方法，三个 dtype 分支共用。逐个特征名从子矩阵表取数据，任一为 null 抛 `IllegalArgumentException("Missing {type} sub-matrix for feature '{name}' of input '{input}'")`。用泛型 `<T>` + lambda 无需反射。

### `convertTensorFloatArray(Tensor)` — 输出转换

```
convertTensorFloatArray → 取 shape[0]/shape[1] → new float[][] → StdArrays.copyFrom
```

把输出张量（TFloat32）前两维当 [batchSize, outputDim]，拷贝成 `float[][]` 返回。⚠️ 假设输出是二维，一维/标量会越界（原 bug 未修）。

---

## 五、三个内部类（工具，静态）

### `MatrixConcat` — 数组按列拼接

```
concatColumnsfloat/Int/String (public 转发) → MatrixConcat 内的真实实现 → prepareConcat
```

- 三个 public 方法是一行转发，保留对外签名。
- `prepareConcat` 用 lambda 强类型访问长度，做**校验**（行数一致）+ **预计算每行总列数**，返回 `ConcatLayout`。
- 复制循环留各自强类型方法（`System.arraycopy` 要求类型严格匹配，泛型做不到）。
- **用途**：`convertToTensor` 里把多个特征子矩阵按列拼成一个输入矩阵。

### `TensorBuilder` — 张量构建

```
buildFloat/buildInt → tensorOf(shape, copyTo)        # 单步
buildString → NdArray → copyTo → tensorOf            # 三步，严禁合并
```

把二维数组转成 TF 张量。float/int 用单步 `tensorOf`，String 用三步 NdArray 路径（历史固化）。

- **用途**：`convertToTensor`（真实数据）和 `warmUp`（mock 数据）都调它，去重了 6 处样板。

### `MockData` — 预热假数据

```
floats/ints/strings → 按 shape 造 10×dim 的全 0/0/"NAN" 数组
```

- **用途**：只被 `warmUp` 调用。

---

## 六、完整调用关系图

```
外部 Model.createPredictor → new TFPredictor()
  │
  ├─ init(modelConfig)              [加载]
  │    ├─ SavedModelBundle.load
  │    ├─ initModelConfig()
  │    │    ├─ parseSignature()
  │    │    ├─ loadTensorYml()
  │    │    └─ loadFeaturesYml()
  │    └─ checkModelConfig()
  │
  ├─ warmUp()                        [预热]
  │    ├─ MockData.floats/ints/strings
  │    ├─ TensorBuilder.buildFloat/Int/String
  │    └─ session.runner().feed→fetch→run → close
  │
  ├─ predict(param)                  [推理，每次请求]
  │    ├─ convertToTensor(param)
  │    │    ├─ dataTypes.get(inputName)  [按签名 dtype 判分支]
  │    │    ├─ collectSubMatrices()       [收集特征数据 + 缺失校验]
  │    │    ├─ concatColumnsfloat/Int/String → MatrixConcat
  │    │    └─ TensorBuilder.buildFloat/Int/String
  │    ├─ session.runner().feed→fetch→run
  │    ├─ convertTensorFloatArray()  [每个输出]
  │    └─ close 所有 Tensor
  │
  └─ close()                         [卸载，目前未调用]
       └─ session.close + model.close
```

---

## 七、一句话总结每个方法

| 方法 | 职责 |
|---|---|
| `init` | 加载模型、解析配置、校验一致性 |
| `initModelConfig` | 编排配置解析（调下面 4 个） |
| `parseSignature` | 从签名提取 dtype/shape |
| `loadTensorYml` | 读 tensor.yml（输入→特征映射） |
| `loadFeaturesYml` | 读 features.yml（特征组映射） |
| `checkModelConfig` | 校验 yml 与签名一致 |
| `warmUp` | 假数据跑一次，预热引擎 |
| `predict` | 真实请求推理，出结果 |
| `convertToTensor` | 请求参数 → 输入张量（按签名 dtype 判分支） |
| `collectSubMatrices` | 收集特征数据 + 缺失校验 |
| `convertTensorFloatArray` | 输出张量 → float[][] |
| `close` | 释放 session/model |
| `MatrixConcat` | 多个特征按列拼接 |
| `TensorBuilder` | 二维数组 → TF 张量 |
| `MockData` | 预热假数据 |

---

## 八、已知遗留问题

已修复：
- ✅ ~~`convertToTensor` 隐式 dtype 推断~~：已改为按签名 dtype 判分支 + 缺失/不支持 dtype 抛异常转 failure（见 REFACTOR_CHECKLIST A7）。

仍未修（重构与修 bug 分离）：
- **`convertTensorFloatArray` shape[1] 越界**：假设输出二维，一维/标量张量会越界。
- **`warmUp` 未覆盖 dtype 静默跳过**：只处理 DT_FLOAT/DT_INT32/DT_STRING，其他 dtype 不喂给 runner 可能报错。
- **`prepareConcat` 空列表越界**：短路顺序敏感（原 bug）。
- **`close()` 未被调用**：模型卸载无生命周期管理，等热更新功能。
