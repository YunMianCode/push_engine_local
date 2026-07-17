# TFPredictor 数据流完整示例

> 本文档用简化配置（3 个特征）从模型加载到请求返回，演示数据在 TFPredictor 各阶段的完整变化流程。
> 参考配置：[tensor.yml](oss_files/rta_model_version_1/tensor.yml)、[features.yml](oss_files/rta_model_version_1/features.yml)
> 参考代码：[TFPredictor.java](mkt_push_engine_server_web/src/main/java/com/qunar/marketing/mkt/pub/alg/tfserving/push/engine/server/service/utils/Predictors/tf/TFPredictor.java)

---

## 配置（简化版，3 个特征）

**tensor.yml**（输入张量 → 特征 + dtype）：

```yaml
inputs:
  - name: serving_default_ip
    features: [ip]
    dtype: DT_STRING
  - name: serving_default_age
    features: [age]
    dtype: DT_STRING
  - name: serving_default_diff_date
    features: [diff_date]
    dtype: DT_FLOAT
outputs:
  - name: StatefulPartitionedCall
    dtype: DT_FLOAT
```

**features.yml**（特征组 → 特征名 + dtype code）：

```yaml
features:
  - featuregroups: user_id
    featuresnames: [ip, age, diff_date]
    featuresdtype: [7, 7, 1]     # 7=string, 1=float
```

---

## 阶段 0：模型加载（init）

`parseSignature()` 从 SavedModel 签名解析，得到：

```java
dataTypes = {
  "serving_default_ip"         → DT_STRING,
  "serving_default_age"        → DT_STRING,
  "serving_default_diff_date"  → DT_FLOAT,
  "StatefulPartitionedCall"    → DT_FLOAT     // 输出
}
inputShapes = {
  "serving_default_ip"         → [−1, 1],   // −1=变长batch
  "serving_default_age"        → [−1, 1],
  "serving_default_diff_date"  → [−1, 1]
}
```

`loadTensorYml()` 读 tensor.yml 得到：

```java
inputToFeaturesMap = {
  "serving_default_ip"         → ["ip"],          // 1个特征
  "serving_default_age"        → ["age"],
  "serving_default_diff_date"  → ["diff_date"]
}
outputNames = ["StatefulPartitionedCall"]
```

`checkModelConfig()` 校验 tensor.yml 的输入输出名都在签名里 → 通过。

---

## 阶段 1：请求到来（predict 入口）

两个用户的预测请求，batch=2。调用方把特征数据散装填进 `PredictParam`：

```java
param.getSubStringMatrices() = {              // String 特征表
  "ip"  → [["192.168.1.1"], ["10.0.0.1"]],   // 2行×1列
  "age" → [["25"], ["30"]]                    // 2行×1列
}
param.getSubMatrices() = {                     // float 特征表
  "diff_date" → [[1.5], [2.0]]               // 2行×1列
}
param.getOutputs() = ["StatefulPartitionedCall"]   // 要取的输出
```

每行是一个用户，每列是一个特征值。此刻数据是**按特征名散装**的，还没组装成模型要的张量。

---

## 阶段 2：convertToTensor — 把散装数据转成输入张量

遍历 `inputToFeaturesMap`，对每个输入张量：按签名 dtype 判分支 → `collectSubMatrices` 收集 → `concatColumns*` 拼接 → `TensorBuilder.build*` 转张量。

### 输入 1：`serving_default_ip`（dtype=DT_STRING，features=["ip"]）

```java
dataType = DT_STRING → 走 string 分支
collectSubMatrices("serving_default_ip", ["ip"], subStringMatrices, "string")
  → 遍历 ["ip"]，从 string 表取 "ip" → [["192.168.1.1"],["10.0.0.1"]]
  → 返回 List<String[][]> = [ ip数组 ]          // 1个元素
concatColumnsString([ip数组])
  → prepareConcat: 校验行数=2，算每行列数=[1,1]，返回 ConcatLayout{rowCount=2, totalColumnsPerRow=[1,1]}
  → 只有1个数组，复制循环把它的两行拷进 result
  → 返回 [["192.168.1.1"],["10.0.0.1"]]         // 2行×1列
TensorBuilder.buildString([["192.168.1.1"],["10.0.0.1"]])
  → NdArray → copyTo → TString
outputs.put("serving_default_ip", TString张量)
```

### 输入 2：`serving_default_age`（dtype=DT_STRING，features=["age"]）

同理，收集 `age` → 拼接（单元素退化为透传）→ buildString：

```
outputs.put("serving_default_age", TString([["25"],["30"]]))
```

### 输入 3：`serving_default_diff_date`（dtype=DT_FLOAT，features=["diff_date"]）

```java
dataType = DT_FLOAT → 走 float 分支
collectSubMatrices("serving_default_diff_date", ["diff_date"], subMatrices, "float")
  → 返回 List<float[][]> = [ diff_date数组 ]  // [[1.5],[2.0]]
concatColumnsfloat([diff_date数组])
  → 透传 → [[1.5],[2.0]]                      // 2行×1列
TensorBuilder.buildFloat([[1.5],[2.0]])
  → TFloat32.tensorOf(...) → TFloat32张量
outputs.put("serving_default_diff_date", TFloat32([1.5],[2.0]))
```

### convertToTensor 返回

```java
inputs = {
  "serving_default_ip"         → TString([["192.168.1.1"],["10.0.0.1"]]),
  "serving_default_age"        → TString([["25"],["30"]]),
  "serving_default_diff_date"  → TFloat32([[1.5],[2.0]])
}
```

**这一步的意义**：散装的"按特征名"数据，变成了"按模型输入张量名"组装好的张量。

> 说明：本例每个输入只有 1 个特征（风格 A），所以 `concatColumns*` 实际是透传。如果某输入 features 有多个（如 `["age","gender","city"]`），`concatColumns*` 会把它们按列拼成 3 列宽矩阵——这就是拼接真正发挥作用的场景。

---

## 阶段 3：喂给 TF + 推理

```java
Session.Runner runner = session.runner();
inputs.forEach(runner::feed);          // 喂3个输入张量
param.getOutputs().forEach(runner::fetch);  // 声明取 "StatefulPartitionedCall"
List<Tensor> outputs = runner.run();   // TF 执行推理
```

TF 内部跑模型图，输出一个 TFloat32 张量，假设形状 `[2, 2]`（2 个用户 × 2 个输出值，比如点击概率 + 不点击概率）：

```
[[0.8, 0.2],     // 用户1
 [0.6, 0.4]]     // 用户2
```

---

## 阶段 4：convertTensorFloatArray — 输出张量转回 Java 数组

对每个输出张量：

```java
convertTensorFloatArray(输出张量)
  shape = [2, 2]
  batchSize = 2, outputDim = 2
  result = new float[2][2]
  StdArrays.copyFrom(TFloat32, result)   // 原生内存拷到 Java 数组
  → [[0.8,0.2],[0.6,0.4]]
outputResults.put("StatefulPartitionedCall", [[0.8,0.2],[0.6,0.4]])
```

---

## 阶段 5：封装结果返回

```java
result.setResults(outputResults);                    // 结果 map
result.addMetadata("predictTimeMs", 耗时);
return PredictResult{ success=true, results={"StatefulPartitionedCall": [[0.8,0.2],[0.6,0.4]]} }
```

最后 finally 释放所有输入/输出 Tensor 的原生内存。

---

## 完整数据流总览

```
配置阶段(init):
  tensor.yml    → inputToFeaturesMap = {输入名 → [特征名]}
  SavedModel签名→ dataTypes = {输入名 → dtype}, inputShapes
  features.yml  → featuresKey/featuresDtype (给特征平台用,TFPredictor 不直接消费)

请求阶段(predict):
  param散装数据(按特征名):
    subStringMatrices = {ip:[["192.168.1.1"],["10.0.0.1"]], age:[["25"],["30"]]}
    subMatrices       = {diff_date:[[1.5],[2.0]]}
       │
       ▼ convertToTensor (按签名dtype判分支)
       │  collectSubMatrices: 从散装表按featureNames捞数据
       │  concatColumns*: 单特征透传/多特征按列拼
       │  TensorBuilder.build*: 转 TFloat32/TString/TInt32
       ▼
  inputs(按输入张量名组装):
    serving_default_ip        → TString([["192.168.1.1"],["10.0.0.1"]])
    serving_default_age       → TString([["25"],["30"]])
    serving_default_diff_date → TFloat32([[1.5],[2.0]])
       │
       ▼ runner.feed → runner.fetch → runner.run (TF推理)
       ▼
  TF输出张量: TFloat32 shape=[2,2] = [[0.8,0.2],[0.6,0.4]]
       │
       ▼ convertTensorFloatArray (取shape[0]/shape[1], StdArrays.copyFrom)
       ▼
  Java数组: float[2][2] = [[0.8,0.2],[0.6,0.4]]
       │
       ▼ 封装 PredictResult
       ▼
  返回: {success=true, results={"StatefulPartitionedCall":[[0.8,0.2],[0.6,0.4]]}}
```

---

## 关键转换节点

| 节点 | 数据形态变化 | 负责方法 |
|---|---|---|
| 散装 → 组装 | 按特征名 → 按输入张量名 | `convertToTensor` |
| 收集 | 从散装表捞出该输入要的特征 | `collectSubMatrices` |
| 拼接 | 多特征列 → 宽矩阵（单特征透传） | `concatColumns*` + `prepareConcat` |
| 打包 | Java二维数组 → TF张量 | `TensorBuilder.build*` |
| 推理 | TF张量进 → TF张量出 | `runner.run` |
| 拆包 | 输出TF张量 → Java二维数组 | `convertTensorFloatArray` |

---

## 附：单特征 vs 多特征输入

本例模型为**风格 A**（每个输入张量对应 1 个特征），所以 `concatColumns*` 实际是透传：

```
单特征输入: features=["ip"]
  collectSubMatrices → [ip数组]            (1个元素)
  concatColumnsString → 透传 ip数组        (没东西可拼)
```

若换**风格 B**模型（一个输入吃多个特征），`concatColumns*` 才真正按列拼：

```
多特征输入: features=["age","gender","city"]
  collectSubMatrices → [age数组, gender数组, city数组]   (3个元素)
  concatColumnsString:
    age   [["25"],["30"]]      ┐
    gender[["M"], ["F"]]       ├ 按列拼 → [["25","M","city1"], ["30","F","city2"]]
    city  [["city1"],["city2"]]┘        // 2行 × 3列
```

代码必须支持风格 B，所以 `concatColumns*` 即使在风格 A 下"看起来白做"也要保留——它是为多特征输入预留的通用能力。
