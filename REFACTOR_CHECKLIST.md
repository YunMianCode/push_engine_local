# 重构与 Bug 修复清单

> 使用方式：逐个告诉 Claude 做哪一项（按编号即可，如"做 B2"），Claude 修改后会展示 diff，确认后更新状态。
> 状态说明：⬜ 待办 | 🔄 进行中 | ✅ 已完成 | ⏸️ 已跳过/暂缓
> 改动原则：不引入新 Bug；行为变更项已标注⚠️；每项改动单独展示供 review。

---

## A. 真实 Bug（影响正确性，改动会改变线上行为 ⚠️）

### A1. `getModel` 精确版本查找结果被丢弃
- **位置**：[ModelManager.java:131-145](mkt_push_engine_server_web/src/main/java/com/qunar/marketing/mkt/pub/alg/tfserving/push/engine/server/service/model/ModelManager.java#L131)
- **问题**：`getModel(modelName, version)` 指定版本时，第 139 行已精确 `modelContainer.get(modelKey)` 查到模型，但第 143 行又调 `searchModelByName` 返回模糊匹配的第一个，精确结果被丢弃。多版本共存时会返回错误版本。
- **修复方案**：精确查到则直接返回；未查到再走模糊匹配或抛异常。
- **风险**：⚠️ 改变多版本场景下的返回结果。需确认线上是否多版本共存。
- **状态**：✅ 已完成（精确查到直接返回，未查到抛 ParamException，不再退化走模糊匹配）

### A2. 预测失败被当成功返回
- **位置**：[ModelServerService.java:76-79](mkt_push_engine_server_web/src/main/java/com/qunar/marketing/mkt/pub/alg/tfserving/push/engine/server/service/model/ModelServerService.java#L76) `packageResponse`
- **问题**：不检查 `PredictResult.success`，直接 `getResults()`。TFPredictor 内部异常返回 `failure` 时 results 为 null，但 response 仍设 `code=C_SUCCESS`、`vectors=null`，调用方拿到"成功+空结果"无法察觉错误。
- **修复方案**：`packageResponse` 先判 `predictResult.isSuccess()`，失败则设 `code=C_MODEL_ERROR` + 错误信息，或抛 `ModelServerException`。
- **风险**：⚠️ 之前静默失败的请求会变成显式错误。对调用方是行为变化（但更正确）。
- **状态**：✅ 已完成（抛 ModelServerException，复用 provider 现有 catch 链路设 C_MODEL_ERROR）

### A3. `fillUserFeatures` int 特征未扩展 + float 重复复制
- **位置**：[FeaturePlatformServer.java:194-222](mkt_push_engine_server_web/src/main/java/com/qunar/marketing/mkt/pub/alg/tfserving/push/engine/server/service/utils/feature/FeaturePlatformServer.java#L194)
- **问题**：batch>1 时，float 特征被 forEach 复制了两次（第 199 行和第 213 行重复），int 特征从未被扩展。导致 TF 输入维度不对齐 / int 特征缺失。
- **修复方案**：float 只复制一次；补 int 特征的扩展逻辑。
- **风险**：⚠️ 改变 batch>1 时的特征矩阵内容。需用真实模型验证输出。
- **状态**：✅ 已完成（删除重复的 float 复制，补 int 特征扩展；float/int/string 三类各复制一次）

### A4. `merge3` 原地修改 fv1 导致语义非直觉
- **位置**：[FeaturePlatformServer.java:293-295](mkt_push_engine_server_web/src/main/java/com/qunar/marketing/mkt/pub/alg/tfserving/push/engine/server/service/utils/feature/FeaturePlatformServer.java#L293)
- **问题**：`merge2(merge2(fv1, fv2), merge2(fv1, fv3))` —— fv1 作为两次 merge2 的第一参数被原地修改两次，第二次 merge2(fv1, fv3) 里的 fv1 已被 fv2 污染。当 fv2、fv3 有同名 key 时行为依赖调用顺序。
- **修复方案**：改为新建容器合并，不原地修改入参；或明确文档化语义。
- **风险**：⚠️ 可能改变特征合并结果。需验证。
- **状态**：⬜

### A5. `CheckModelConfig` 校验失败不阻止加载
- **位置**：[TFPredictor.java:46-48](mkt_push_engine_server_web/src/main/java/com/qunar/marketing/mkt/pub/alg/tfserving/push/engine/server/service/utils/Predictors/tf/TFPredictor.java#L46)
- **问题**：配置不一致时只 `log.error`，模型仍被注册，运行时才出错。
- **修复方案**：校验失败抛异常，阻止该模型加载。
- **风险**：⚠️ 之前能启动的（配置不一致的）模型会加载失败。需确认线上模型配置都一致。
- **状态**：✅ 已完成（CheckModelConfig 返回 false 时 init 抛 RuntimeException 阻止加载，异常带 modelPath；CheckModelConfig 内部 warn 保留具体缺失项）

### A6. `fillUserFeatures` 用户特征广播行数多 1（batchSize+1 应为 batchSize）
- **位置**：[FeaturePlatformServer.java:194-222](mkt_push_engine_server_web/src/main/java/com/qunar/marketing/mkt/pub/alg/tfserving/push/engine/server/service/utils/feature/FeaturePlatformServer.java#L194) `fillUserFeatures`
- **问题**：多物品预测时，用户特征被广播成 `batchSize+1` 行，而物品特征 `mergeItemFeatures` 合并后是 `batchSize` 行（K=物品数）。两者行数差 1，对不齐。merge2 按 key 合并不校验行数，问题被推迟到喂给 TF 时——用户类特征比物品类特征多一行，导致 TF 输入 batch 维错位。单物品分支（itemFeatures.size()==1）不调本方法、行数一致，说明设计意图就是「用户行数=物品行数=K」，多物品分支的 +1 是 bug。
- **修复方案**：`fillUserFeatures` 内两处 `batchSize + 1` 改为 `batchSize`，循环边界同步，用户特征广播成 K 行与物品特征对齐。
- **风险**：⚠️ 改变 batch>1 时的用户特征矩阵行数。当前 /test-predict 走 batchSize=1 且 itemIds 空，不触发本方法，线上无影响；多物品预测场景需回归验证。
- **状态**：✅ 已完成（三处 batchSize+1 改为 batchSize，用户特征广播成 K 行与物品特征对齐）

### A7. `convertToTensor` 隐式 dtype 推断（首个特征名落表）
- **位置**：[TFPredictor.java](mkt_push_engine_server_web/src/main/java/com/qunar/marketing/mkt/pub/alg/tfserving/push/engine/server/service/utils/Predictors/tf/TFPredictor.java) `convertToTensor`
- **问题**：原按首个特征名 `v.get(0)` 落在 float/string/int 哪张子矩阵表来判 dtype，而非按签名声明的 dtype。三个风险：(1) 三表都没该特征名时静默落 int 分支，喂错类型；(2) 首个特征类型 ≠ 该输入真实类型时判错；(3) 特征数据在对应表里缺失时拿到 null 喂给 concatColumns（容错跳过，喂错维度给模型）。
- **修复方案**：改为按 `modelConfig.getDataTypes().get(inputName)`（SavedModel 签名声明的 dtype）判分支：DT_FLOAT/DT_STRING/DT_INT32 各走对应子矩阵表 + TensorBuilder，其他 dtype 抛 IllegalArgumentException；抽泛型 `collectSubMatrices` 统一收集特征数据并做缺失校验，任一特征缺失抛异常。异常由 predict catch 转 PredictResult.failure。
- **风险**：⚠️ 行为变更。之前三表都缺失时静默当 int，现在按签名 dtype 正确判定；之前特征缺失静默跳过，现在失败。dataTypes key 与 inputToFeaturesMap key 已由 checkModelConfig 校验一致，故 `dataTypes.get(inputName)` 必有值。需回归验证 predict 正常路径。
- **状态**：✅ 已完成（按签名 dtype 判分支 + collectSubMatrices 缺失校验 + 不支持 dtype 抛异常转 failure）

---

## B. 资源与安全

### B1. TFPredictor Tensor 原生内存泄漏
- **位置**：[TFPredictor.java:61-115](mkt_push_engine_server_web/src/main/java/com/qunar/marketing/mkt/pub/alg/tfserving/push/engine/server/service/utils/Predictors/tf/TFPredictor.java#L61) `predict`，及 `warmUp` 第 639-642 行
- **问题**：`convertToTensor` 创建的输入 Tensor、`runner.run()` 返回的输出 Tensor，读取后均未 `close()`。Tensor 持有原生内存，高 QPS 下 OOM。warmUp 同样。
- **修复方案**：用 try-finally / try-with-resources 关闭所有 Tensor（inputs 和 outputs）。
- **风险**：低。纯资源释放，不改变返回值。需确认 Tensor close 顺序不影响已拷贝出的 float[][]（copyFrom 已拷贝，close 安全）。
- **状态**：⬜

### B2. S3 密钥泄露到 git + 日志打印密钥
- **位置**：`oss.properties`（accessKey/secretKey 硬编码并提交 git）；[AWS3Service.java:60](mkt_push_engine_server_web/src/main/java/com/qunar/marketing/mkt/pub/alg/tfserving/push/engine/server/service/utils/aws/AWS3Service.java#L60) `log.info("accessKey:{} secretKey:{}",...)`
- **问题**：密钥进 git 仓库；启动日志明文打印密钥。
- **修复方案**：日志改为不打印密钥（或只打前几位）；密钥改为从环境变量/qconfig 读取（需协调部署）。git 历史中的密钥应轮换。
- **风险**：日志改动低风险；密钥来源改动需协调线上部署方式，建议分两步。
- **状态**：⬜

### B3. `IPredictor.close()` 从未被调用
- **位置**：[IPredictor.java:34](mkt_push_engine_server_web/src/main/java/com/qunar/marketing/mkt/pub/alg/tfserving/push/engine/server/service/utils/Predictors/IPredictor.java#L34) close 定义；ModelManager 无卸载逻辑
- **问题**：模型热更新/卸载时不释放 session/bundle，资源泄漏。当前无热更新，影响暂小。
- **修复方案**：与"模型热更新"特性一起做，或先在 ModelManager 加 destroy 钩子。
- **风险**：中。涉及生命周期管理，建议与 A 类 bug 解耦。
- **状态**：⬜

---

## C. 重复代码（不改变行为，安全）

### C1. 3 个 Dubbo provider 的 predict 逻辑重复
- **位置**：[DubboBlogApiServiceImpl.java:51-89](mkt_push_engine_server_web/src/main/java/com/qunar/marketing/mkt/pub/alg/tfserving/push/engine/server/service/provider/DubboBlogApiServiceImpl.java#L51)、[ApacheDubboFutureAsyncServiceImpl.java:44-79](mkt_push_engine_server_web/src/main/java/com/qunar/marketing/mkt/pub/alg/tfserving/push/engine/server/service/provider/ApacheDubboFutureAsyncServiceImpl.java#L44)、[DubboBetterAsyncServiceImpl.java:45-82](mkt_push_engine_server_web/src/main/java/com/qunar/marketing/mkt/pub/alg/tfserving/push/engine/server/service/provider/DubboBetterAsyncServiceImpl.java#L45)
- **问题**：3 处 predict 几乎逐行相同（CPU 计时 + 三段 catch + 错误码 + 委托 modelServerService）。
- **修复方案**：抽到 `PredictExecutor` 或基类，3 个 Impl 只保留同步/CompletableFuture/@AsyncImpl 的包装差异。
- **风险**：低。逻辑等价抽取，需保证异常码、CPU 计时行为完全一致。
- **状态**：⬜

### C2. TFPredictor 三个 concatColumns 方法重复
- **位置**：[TFPredictor.java](mkt_push_engine_server_web/src/main/java/com/qunar/marketing/mkt/pub/alg/tfserving/push/engine/server/service/utils/Predictors/tf/TFPredictor.java)
- **问题**：`concatColumnsfloat`/`concatColumnsInt`/`concatColumnsString` 逻辑完全相同，仅类型不同。
- **修复方案**：泛型抽取，或保留 3 个但抽公共校验/循环逻辑。
- **风险**：低。需保证泛型版本与原逻辑逐行等价（含 System.arraycopy）。
- **状态**：✅ 已完成（抽 `MatrixConcat` 内部静态类：三个 public 方法保留对外签名做一行转发，真实实现移入内部类；公共的校验+预计算抽成 `prepareConcat`，用 lambda 强类型访问长度、无反射；复制循环因 System.arraycopy 要求类型严格匹配留在各强类型方法。逐行等价）

### C3. FeaturePlatformServer 的 print*/printHash* 方法重复
- **位置**：[FeaturePlatformServer.java:341-443](mkt_push_engine_server_web/src/main/java/com/qunar/marketing/mkt/pub/alg/tfserving/push/engine/server/service/utils/feature/FeaturePlatformServer.java#L341)
- **问题**：6 个格式化方法逻辑相同仅类型不同，且多为调试用。
- **修复方案**：评估是否真有调用，无调用直接删除；有调用则泛型化。
- **风险**：低。
- **状态**：⬜

### C4. RedisConfig 3 个 Sedis3 Bean 重复
- **位置**：[RedisConfig.java:26-80](mkt_push_engine_server_web/src/main/java/com/qunar/marketing/mkt/pub/alg/tfserving/push/engine/server/common/config/RedisConfig.java#L26)
- **问题**：3 个 @Bean 方法几乎相同，仅配置前缀和 bean 名不同。
- **修复方案**：抽公共方法，或用循环/配置化注册。
- **风险**：低。需保证 bean 名和参数注入完全一致。
- **状态**：⬜

---

## D. 死代码 / 未使用（删除，安全）

### D1. `ModelServerService.buildContext` 空方法
- **位置**：[ModelServerService.java:64-68](mkt_push_engine_server_web/src/main/java/com/qunar/marketing/mkt/pub/alg/tfserving/push/engine/server/service/model/ModelServerService.java#L64)
- **问题**：返回空 context，predict 实际调的是 `modelService.buildContext`，此方法从未被调用，误导维护者。
- **修复方案**：删除。
- **风险**：无（确认无其他引用后）。
- **状态**：⬜

### D2. `Model.getModelType` 重复且未调用
- **位置**：[Model.java:163-174](mkt_push_engine_server_web/src/main/java/com/qunar/marketing/mkt/pub/alg/tfserving/push/engine/server/service/utils/model/Model.java#L163)
- **问题**：与 `readProperty` 中 switch 逻辑重复，且从未被调用。
- **修复方案**：删除。
- **风险**：无。
- **状态**：⬜

### D3. `TFModel` 整个类是空壳
- **位置**：[TFModel.java](mkt_push_engine_server_web/src/main/java/com/qunar/marketing/mkt/pub/alg/tfserving/push/engine/server/service/utils/model/TFModel.java)
- **问题**：继承 Model 但构造器空、predict 返回 null，从未被实例化。
- **修复方案**：删除整个文件。
- **风险**：无（确认无引用后）。
- **状态**：⬜

### D4. ModelManager 未使用字段
- **位置**：[ModelManager.java:27](mkt_push_engine_server_web/src/main/java/com/qunar/marketing/mkt/pub/alg/tfserving/push/engine/server/service/model/ModelManager.java#L27) `modellist`、[ModelManager.java:30](mkt_push_engine_server_web/src/main/java/com/qunar/marketing/mkt/pub/alg/tfserving/push/engine/server/service/model/ModelManager.java#L30) `bucketName`（硬编码 "qunar" 未用）
- **修复方案**：删除字段。
- **风险**：无。
- **状态**：⬜

### D5. ModelInfoSnapshot.isOnlyXmlUpdateFlag 未使用
- **位置**：[ModelInfoSnapshot.java:12](mkt_push_engine_server_web/src/main/java/com/qunar/marketing/mkt/pub/alg/tfserving/push/engine/server/service/utils/model/ModelInfoSnapshot.java#L12)
- **问题**：字段有 getter/setter 但 init() 中未使用。
- **修复方案**：若未来热更新要用则保留并标注 TODO；否则删除。建议保留（语义相关，删除影响 JSON 反序列化兼容性）。
- **风险**：删除可能影响 dictInfo.list 反序列化（多余字段无害，但语义上是为热更新预留）。倾向保留。
- **状态**：⬜

### D6. OnnxPredictor / PyTorchPredictor 空壳
- **位置**：[OnnxPredictor.java](mkt_push_engine_server_web/src/main/java/com/qunar/marketing/mkt/pub/alg/tfserving/push/engine/server/service/utils/Predictors/onnx/OnnxPredictor.java)、[PyTorchPredictor.java](mkt_push_engine_server_web/src/main/java/com/qunar/marketing/mkt/pub/alg/tfserving/push/engine/server/service/utils/Predictors/pytorch/PyTorchPredictor.java)
- **问题**：所有方法空实现/返回 null。`Model.createPredictor` 会 new 出来但 init 空、predict 返回 null，若配置了 onnx/pytorch 模型会静默返回 null（与 A2 叠加）。
- **修复方案**：保留骨架但 predict 抛 `UnsupportedOperationException`（快速失败），或补 TODO 注释。不建议删除（架构预留）。
- **风险**：改为抛异常是行为变化（但本就该报错）。低风险。
- **状态**：⬜

### D7. 各处注释掉的代码块
- **位置**：TFPredictor 多处、ModelService.createPredictParamFromContext、AWS3Service.unzipFile 旧实现等
- **修复方案**：清理注释代码。
- **风险**：无。
- **状态**：⬜

---

## E. 异常处理

### E1. ModelManager.init() 吞掉 IOException
- **位置**：[ModelManager.java:57-59](mkt_push_engine_server_web/src/main/java/com/qunar/marketing/mkt/pub/alg/tfserving/push/engine/server/service/model/ModelManager.java#L57)
- **问题**：`catch (IOException e) {}` 完全空 catch，模型加载失败无日志无监控，服务空载启动后预测全报 "no such model"。
- **修复方案**：catch 内 `LOGGER.error` + `QMonitor.recordOne`。是否抛异常导致启动失败需讨论（倾向记录后继续，保持启动）。
- **风险**：低。仅加日志监控，不改控制流。
- **状态**：⬜

### E2. TFPredictor.predict 异常只 log.info 不抛
- **位置**：[TFPredictor.java:110-114](mkt_push_engine_server_web/src/main/java/com/qunar/marketing/mkt/pub/alg/tfserving/push/engine/server/service/utils/Predictors/tf/TFPredictor.java#L110)
- **问题**：catch 用 `log.info` 记录异常（级别过低），返回 failure。
- **修复方案**：改为 `log.error`。与 A2 配合（A2 会让 failure 显式上报）。
- **风险**：无。
- **状态**：⬜

---

## F. 命名 / 规范 / 小问题

### F1. 字段拼写错误 `cupTimeFlag`
- **位置**：[AlgoInnerRequest.java:7](mkt_push_engine_server_web_web-api/src/main/java/com/qunar/marketing/mkt/pub/alg/tfserving/push/engine/server/api/AlgoInnerRequest.java#L7)（web-api 模块）
- **问题**：字段名 `cupTimeFlag` 应为 `cpuTimeFlag`。getter/setter 方法名是对的（`getCpuTimeFlag`）。
- **修复方案**：改字段名。⚠️ 涉及 api 模块（对外契约），若 Dubbo 序列化按字段名可能影响兼容。需确认序列化方式（fastjson 按 getter 名，通常安全）。
- **风险**：中。api 模块是对外契约，需谨慎。
- **状态**：⬜

### F2. 拼写 `featureStorge` → `featureStorage`
- **位置**：[FeaturePlatformServer.java:16-23](mkt_push_engine_server_web/src/main/java/com/qunar/marketing/mkt/pub/alg/tfserving/push/engine/server/service/utils/feature/FeaturePlatformServer.java#L16)、[RedisConfig.java](mkt_push_engine_server_web/src/main/java/com/qunar/marketing/mkt/pub/alg/tfserving/push/engine/server/common/config/RedisConfig.java) bean 名
- **修复方案**：改名。⚠️ bean 名 `featureStorge` 被 `@Resource(name=...)` 引用，需同步改。
- **风险**：低（全在项目内），但改动点多。
- **状态**：⬜

### F3. 变量名 `vaule` → `value`
- **位置**：[TFPredictor.java:252](mkt_push_engine_server_web/src/main/java/com/qunar/marketing/mkt/pub/alg/tfserving/push/engine/server/service/utils/Predictors/tf/TFPredictor.java#L252)、264 行
- **修复方案**：局部变量改名。
- **风险**：无。
- **状态**：⬜

### F4. 方法名 `CheckModelConfig` 首字母大写
- **位置**：[TFPredictor.java:364](mkt_push_engine_server_web/src/main/java/com/qunar/marketing/mkt/pub/alg/tfserving/push/engine/server/service/utils/Predictors/tf/TFPredictor.java#L364)
- **修复方案**：改 `checkModelConfig`。
- **风险**：无（包内方法）。
- **状态**：⬜

### F5. 无用 import `java.awt.*`
- **位置**：[FeaturePlatformServer.java:8](mkt_push_engine_server_web/src/main/java/com/qunar/marketing/mkt/pub/alg/tfserving/push/engine/server/service/utils/feature/FeaturePlatformServer.java#L8)
- **修复方案**：删除 import。
- **风险**：无。
- **状态**：⬜

### F6. AWS3Service region 硬编码
- **位置**：[AWS3Service.java:55](mkt_push_engine_server_web/src/main/java/com/qunar/marketing/mkt/pub/alg/tfserving/push/engine/server/service/utils/aws/AWS3Service.java#L55) `Region.AP_SOUTHEAST_4`
- **修复方案**：从 oss.properties 读取 region。
- **风险**：低。
- **状态**：⬜

---

## G. 测试 / 运维代码混入主工程

### G1. TestDubboController 纯测试代码
- **位置**：[TestDubboController.java](mkt_push_engine_server_web/src/main/java/com/qunar/marketing/mkt/pub/alg/tfserving/push/engine/server/controller/TestDubboController.java)
- **问题**：8 个测试端点，硬编码 modelKey/deviceId，构建 request 重复 4 次。生产应移除。
- **修复方案**：移到 test 目录或加 profile 隔离；或在生产打包时排除。
- **风险**：低。需确认线上是否依赖这些端点排查。
- **状态**：⬜

### G2. RedisController 纯测试代码
- **位置**：[RedisController.java](mkt_push_engine_server_web/src/main/java/com/qunar/marketing/mkt/pub/alg/tfserving/push/engine/server/controller/RedisController.java)
- **问题**：插入硬编码 5 条用户画像，生产应移除。
- **修复方案**：同 G1。
- **风险**：低。
- **状态**：⬜

---

## H. 架构演进（长期，大改动）

### H1. ModelManager 职责拆分
- **内容**：拆为 ModelRegistry（存取）+ ModelLoader（S3下载解压加载）+ Bootstrap（编排）。
- **风险**：高。大重构，建议在 A-D 完成后做。
- **状态**：⬜

### H2. Model 去静态化 + 路径注入
- **内容**：`Model.getInstance` 改实例工厂，路径构造注入而非读 CommonConstants.MODEL_DIR。
- **风险**：高。
- **状态**：⬜

### H3. 统一配置 + profile
- **内容**：路径/bucket/redis/dubbo 全进 application.properties + profile，@ConfigurationProperties 类型化。
- **风险**：中。涉及部署方式。
- **状态**：⬜

### H4. Redis pipeline 批量取特征
- **内容**：item/useritem 特征用 pipeline 批量取，消除 N 次往返。
- **风险**：中。需验证 Sedis3 是否支持 pipeline。
- **状态**：⬜

### H5. 模型热更新
- **内容**：定时拉 dictInfo.list 对比版本，增量加载 + 优雅切换 + 旧版本释放（配合 B3）。
- **风险**：高。
- **状态**：⬜

### H6. 测试覆盖
- **内容**：为 predict 主链路、特征合并、tensor 转换补单测。
- **风险**：无（新增测试）。
- **状态**：⬜

---

## 推荐执行顺序

1. **先做无风险的清理**：D1-D7（删死代码）、F3-F5（局部改名）、E1-E2（加日志）、C1-C4（去重）—— 建立信心，不动行为。
2. **再做资源/安全**：B1（Tensor泄漏，重要）、B2（密钥，分两步）。
3. **最后做行为变更的 Bug**：A1-A5 —— 每个都需要线上验证手段，逐个评估。
4. **架构演进** H1-H6 视需求排期。

> 注：A 类（行为变更）建议在有回归验证手段后做；F1/F2 涉及契约和 bean 名，单独评估。
