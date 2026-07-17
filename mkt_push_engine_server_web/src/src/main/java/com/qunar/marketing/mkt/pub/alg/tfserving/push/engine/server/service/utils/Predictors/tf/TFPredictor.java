package com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.Predictors.tf;

import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.Predictors.IPredictor;
import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.YmlConfigReader;
import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.domain.ModelConfig;
import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.domain.PredictParam;
import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.domain.PredictResult;
import lombok.extern.slf4j.Slf4j;
import org.tensorflow.SavedModelBundle;
import org.tensorflow.Session;
import org.tensorflow.Signature;
import org.tensorflow.Tensor;
import org.tensorflow.ndarray.*;
import org.tensorflow.proto.framework.DataType;
import org.tensorflow.types.TFloat32;
import org.tensorflow.types.TInt32;
import org.tensorflow.types.TString;

import java.io.File;
import java.util.*;

@Slf4j
public class TFPredictor implements IPredictor {
    private ModelConfig modelConfig;
    private SavedModelBundle model;
    private Session session;
    private List<Signature> signatures;

    /**
     * 初始化 TensorFlow 预测器
     * @param modelConfig 模型配置，包含模型路径等信息，例如 modelPath="/home/q/www/models/rta_model_version_1"
     * @throws RuntimeException 模型加载或配置校验失败时抛出
     */
    @Override
    public void init(ModelConfig modelConfig) {
        this.modelConfig = modelConfig;
        try {
            String modelPath = modelConfig.getModelPath() + "/saved_model";
            log.info("== modelPath={}", modelPath);
            model = SavedModelBundle.load(modelPath, "serve");
            session = model.session();
            signatures = model.signatures();
            initModelConfig();
            if (!checkModelConfig()) {
                throw new RuntimeException(
                        "Model config inconsistent with SavedModel signature, modelPath=" + modelPath);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize TensorFlow predictor", e);
        }
    }

    /**
     * 解析并填充模型配置
     * 依次调用 {@link #parseSignature()}、{@link #loadTensorYml()}、{@link #loadFeaturesYml()}，
     * 将签名信息与 tensor.yml/features.yml 配置全部回填到 modelConfig
     */
    private void initModelConfig() {
        parseSignature();
        loadTensorYml();
        loadFeaturesYml();
    }

    /**
     * 校验模型配置与签名的一致性
     *
     * @return 配置一致返回 true，否则返回 false
     */
    boolean checkModelConfig() {
        // 获取模型签名中定义的输入名称集合
        Map<String, long[]> inputShapes = this.modelConfig.getInputShapes();
        Set<String> modelInputNames = new HashSet<>(inputShapes.keySet());
        // 获取配置文件中定义的输入输出配置
        TensorModelConfig tensorConfig = this.modelConfig.getTensorModelConfig();

        // 校验每个配置文件中声明的输入是否存在于模型签名中
        for (TensorModelConfig.InputConfig inputTensorConfig : tensorConfig.getInputs()) {
            String inputName = inputTensorConfig.getName();
            if (!modelInputNames.contains(inputName)) {
                log.warn("== Input '{}' defined in tensor.yml not found in SavedModel signature", inputName);
                return false;
            }
        }
        // 校验配置文件中声明的输出是否存在于模型签名中
        Map<String, long[]> outputShapes = this.modelConfig.getOutputShapes();
        Set<String> modelOutputNames = new HashSet<>(outputShapes.keySet());
        String configOutputName = tensorConfig.getOutputs().getName();
        if (!modelOutputNames.contains(configOutputName)) {
            log.warn("== Output '{}' defined in tensor.yml not found in SavedModel signature", configOutputName);
            return false;
        }
        return true;
    }

    /**
     * 从 SavedModel 签名解析输入输出映射、数据类型与形状，回填到 modelConfig
     */
    private void parseSignature() {
        this.modelConfig.setInputMap((this.signatures).get(0).getInputs());
        this.modelConfig.setOutputMap((this.signatures).get(0).getOutputs());
        Map<String, DataType> dataTypes = new HashMap<>();
        Map<String, long[]> inputShapes = new HashMap<>();
        Map<String, long[]> outputShapes = new HashMap<>();

        for (Signature signature : this.signatures) {
            signature.getInputs().forEach((key, input) -> {
                DataType dataType = input.dataType;
                Shape inputShape = input.shape;
                String name = input.name;
                String[] names = name.split(":");
                inputShapes.put(names[0], inputShape.asArray());
                dataTypes.put(names[0], dataType);
            });
            signature.getOutputs().forEach((key, output) -> {
                DataType dataType = output.dataType;
                Shape outputShape = output.shape;
                String name = output.name;
                String[] names = name.split(":");
                outputShapes.put(names[0], outputShape.asArray());
                dataTypes.put(names[0], dataType);
            });
        }
        this.modelConfig.setDataTypes(dataTypes);
        this.modelConfig.setInputShapes(inputShapes);
        this.modelConfig.setOutputShapes(outputShapes);
    }

    /**
     * 读取 tensor.yml，解析输入到特征列表的映射及输出名，回填到 modelConfig
     */
    private void loadTensorYml() {
        TensorModelConfig tensorModelConfig = YmlConfigReader.readYamlAsObject(
                this.modelConfig.getModelPath() + File.separator + "tensor.yml", TensorModelConfig.class);
        this.modelConfig.setTensorModelConfig(tensorModelConfig);

        Map<String, List<String>> input2Features = new HashMap<>();
        tensorModelConfig.getInputs().forEach((k) -> {
            input2Features.put(k.getName(), k.getFeatures());
        });
        this.modelConfig.setInputToFeaturesMap(input2Features);

        List<String> outputNames = new ArrayList<>();
        outputNames.add(tensorModelConfig.getOutputs().getName());
        this.modelConfig.setOutputNames(outputNames);
    }

    /**
     * 读取 features.yml，解析特征组到特征名、数据类型的映射，回填到 modelConfig
     */
    private void loadFeaturesYml() {
        Map<String, List<String>> featuresKey = new HashMap<>();
        Map<String, List<Integer>> feturesDtype = new HashMap<>();
        FeatureGroupConfig featureGroupConfig = YmlConfigReader.readYamlAsObject(
                this.modelConfig.getModelPath() + File.separator + "features.yml", FeatureGroupConfig.class);
        featureGroupConfig.getFeatures().forEach((k) -> {
            featuresKey.put(k.getFeaturegroups(), k.getFeaturesnames());
            feturesDtype.put(k.getFeaturegroups(), k.getFeaturesdtype());
        });
        this.modelConfig.setFeaturesKey(featuresKey);
        this.modelConfig.setFeaturesDtype(feturesDtype);
        // 特征组为模型级常量，启动期打印一次即可，请求级不再重复打印
        log.info("== featuresKey: {}", featuresKey);
        log.info("== featuresDtype: {}", feturesDtype);
    }


    /**
     * 执行 TensorFlow 模型预测
     * @param param 预测输入参数，包含子矩阵及期望输出名列表，例如：
     *              subStringMatrices={ip:[["192.168.1.1"],["10.0.0.1"]], age:[["25"],["30"]]},
     *              subMatrices={diff_date:[[1.5],[2.0]]}, outputs=["StatefulPartitionedCall"]
     * @return 预测结果，含输出名到浮点数组的映射及预测耗时；推理异常时返回失败结果，
     *         例如 results={"StatefulPartitionedCall":[[0.8,0.2],[0.6,0.4]]}
     */
    @Override
    public PredictResult predict(PredictParam param) {
        PredictResult result = PredictResult.success();
        Map<String, Tensor> inputs = null;
        List<Tensor> outputs = null;
        try {
            long start = System.currentTimeMillis();
            long start1 = System.currentTimeMillis();
            // 把 param 转化为 TF可以接受的输入
            inputs = convertToTensor(param);
            log.debug("== conver2Tensor time ={}ms", System.currentTimeMillis() - start1);

            start1 = System.currentTimeMillis();
            Session.Runner runner = this.session.runner();
            log.debug("== session run time ={}ms", System.currentTimeMillis() - start1);

            start1 = System.currentTimeMillis();
            inputs.forEach(runner::feed);
            log.debug("== feed time ={}ms", System.currentTimeMillis() - start1);

            Map<String, float[][]> outputResults = new HashMap<>();
            List<String> outPutNames = param.getOutputs();
            start1 = System.currentTimeMillis();
            outPutNames.forEach(runner::fetch);
            log.debug("== fetch time ={}ms", System.currentTimeMillis() - start1);

            outputs = runner.run();
            try {
                for (int i = 0; i < outputs.size(); i++) {
                    outputResults.put(outPutNames.get(i), convertTensorFloatArray(outputs.get(i)));
                }
            } finally {
                // 释放输出 Tensor 的原生内存
                for (Tensor output : outputs) {
                    output.close();
                }
            }
            result.setResults(outputResults);
            log.debug("== results time ={}ms", System.currentTimeMillis() - start);
            result.addMetadata("predictTimeMs", System.currentTimeMillis() - start);
            return result;
        } catch (Exception e) {
            log.error("== TensorFlow prediction failed", e);
            return PredictResult.failure("TensorFlow prediction failed: " + e.getMessage());
        } finally {
            // 释放输入 Tensor 的原生内存
            if (inputs != null) {
                for (Tensor input : inputs.values()) {
                    input.close();
                }
            }
        }
    }

    /**
     * 将预测参数转换为 TensorFlow 输入张量
     * 遍历 inputToFeaturesMap，按签名声明的 dtype 决定类型分支：DT_FLOAT 走浮点子矩阵生成 TFloat32，
     * DT_STRING 走字符串子矩阵生成 TString，DT_INT32 走整型子矩阵生成 TInt32。特征缺失或不支持的
     * dtype 抛 IllegalArgumentException，由 predict 捕获转为 PredictResult.failure
     * @param param 预测输入参数，例如：
     *              subStringMatrices={ip:[["192.168.1.1"],["10.0.0.1"]], age:[["25"],["30"]]},
     *              subMatrices={diff_date:[[1.5],[2.0]]}
     * @return 输入名到张量的映射，例如：
     *         {serving_default_ip→TString([["192.168.1.1"],["10.0.0.1"]]),
     *          serving_default_diff_date→TFloat32([[1.5],[2.0]])}
     */
    private Map<String, Tensor> convertToTensor(PredictParam param) {
        Map<String, Tensor> outputs = new HashMap<>();
        Map<String, DataType> dataTypes = this.modelConfig.getDataTypes();

        this.modelConfig.getInputToFeaturesMap().forEach((inputName, featureNames) -> {
            DataType dataType = dataTypes.get(inputName);
            if (dataType == null) {
                throw new IllegalArgumentException(
                        "No dataType found in signature for input: " + inputName);
            }
            if (dataType == DataType.DT_FLOAT) {
                float[][] data = MatrixConcat.concatColumnsFloat(
                        collectSubMatrices(inputName, featureNames, param.getSubMatrices(), "float"));
                outputs.put(inputName, TensorBuilder.buildFloat(data));
            } else if (dataType == DataType.DT_STRING) {
                String[][] data = MatrixConcat.concatColumnsString(
                        collectSubMatrices(inputName, featureNames, param.getSubStringMatrices(), "string"));
                outputs.put(inputName, TensorBuilder.buildString(data));
            } else if (dataType == DataType.DT_INT32) {
                int[][] data = MatrixConcat.concatColumnsInt(
                        collectSubMatrices(inputName, featureNames, param.getSubIntMatrices(), "int"));
                outputs.put(inputName, TensorBuilder.buildInt(data));
            } else {
                throw new IllegalArgumentException(
                        "Unsupported input dtype: " + dataType + " for input: " + inputName);
            }
        });

        return outputs;
    }

    /**
     * 把"无序的散装特征表"转成"按签名固定顺序的特征列表"(hashmap存取无序)
     * @param inputName    输入张量名（用于错误信息定位），例如 "serving_default_user_profile"
     * @param featureNames 该输入所需的特征名列表，例如 ["age","gender"]
     * @param subMatrices  请求参数中对应类型的子矩阵表，例如 {age:[["25"],["30"]], gender:[["M"],["F"]]}
     * @param typeLabel    类型标签（"float"/"string"/"int"，用于错误信息），例如 "string"
     * @param <T>          子矩阵元素类型（float[][]/int[][]/String[][] 之一的二维数组类型）
     * @return 按特征顺序收集的二维数据列表，例如 [["25"],["30"]], [["M"],["F"]]]
     * @throws IllegalArgumentException 任一特征数据缺失时抛出
     */
    private static <T> List<T> collectSubMatrices(String inputName, List<String> featureNames,
                                                  Map<String, T> subMatrices, String typeLabel) {
        List<T> collected = new ArrayList<>(featureNames.size());
        for (String featureName : featureNames) {
            T matrix = subMatrices.get(featureName);
            if (matrix == null) {
                throw new IllegalArgumentException(String.format(
                        "Missing %s sub-matrix for feature '%s' of input '%s'", typeLabel, featureName, inputName));
            }
            collected.add(matrix);
        }
        return collected;
    }


    /**
     * 将输出张量转换为二维浮点数组
     * 取张量前两维形状作为批次与输出维度，通过 StdArrays.copyFrom 将 TFloat32 数据拷贝到二维数组
     * @param tensor TF 输出张量，例如 shape=[2,2] 的 TFloat32，内容 [[0.8,0.2],[0.6,0.4]]
     * @return 二维浮点数组，例如 [[0.8,0.2],[0.6,0.4]]
     */
    private float[][] convertTensorFloatArray(Tensor tensor) {
        long[] shape = tensor.shape().asArray();
        int batchSize = (int) shape[0];
        int outputDim = (int) shape[1];

        float[][] result = new float[batchSize][outputDim];
        // FloatBuffer floatBuffer = FloatBuffer.allocate(batchSize * outputDim);
        StdArrays.copyFrom((TFloat32) tensor, result);
        return result;
    }

    /**
     * 释放 TensorFlow 预测器资源
     * 依次关闭会话与 SavedModelBundle，防止底层原生内存泄漏
     */
    @Override
    public void close() {
        if (session != null) {
            session.close();
        }
        if (model != null) {
            model.close();
        }
    }

    /**
     * 二维数组按列拼接工具
     * 把多个列数可能不同的二维数组左右并排粘成一个宽矩阵，行数不变、列数相加。
     * 三个方法逻辑相同仅元素类型不同；因 Java 基本类型数组无法泛型统一（new T[][] 与 System.arraycopy
     * 都要求强类型），故各写一份自包含实现，避免泛型/反射/函数式接口带来的间接层，换取最高性能与可读性
     */
    private static final class MatrixConcat {
        /** 单元素列表直接返回原数组，避免无谓拷贝 */
        static float[][] concatColumnsFloat(List<float[][]> arrays) {
            if (arrays == null || arrays.isEmpty() || arrays.get(0) == null || arrays.get(0).length == 0) {
                throw new IllegalArgumentException("至少需要一个非空数组");
            }
            if (arrays.size() == 1) {
                return arrays.get(0);
            }
            int rowCount = arrays.get(0).length;
            int[] colsPerRow = computeFloatColumnsPerRow(arrays, rowCount);
            float[][] result = new float[rowCount][];
            for (int row = 0; row < rowCount; row++) {
                float[] dst = new float[colsPerRow[row]];
                int col = 0;
                for (float[][] arr : arrays) {
                    float[] src = arr[row];
                    if (src == null || src.length == 0) {
                        continue;
                    }
                    System.arraycopy(src, 0, dst, col, src.length);
                    col += src.length;
                }
                result[row] = dst;
            }
            return result;
        }

        /** 按列拼接多个二维整型数组，单元素列表直接返回原数组 */
        static int[][] concatColumnsInt(List<int[][]> arrays) {
            if (arrays == null || arrays.isEmpty() || arrays.get(0) == null || arrays.get(0).length == 0) {
                throw new IllegalArgumentException("至少需要一个非空数组");
            }
            if (arrays.size() == 1) {
                return arrays.get(0);
            }
            int rowCount = arrays.get(0).length;
            int[] colsPerRow = computeIntColumnsPerRow(arrays, rowCount);
            int[][] result = new int[rowCount][];
            for (int row = 0; row < rowCount; row++) {
                int[] dst = new int[colsPerRow[row]];
                int col = 0;
                for (int[][] arr : arrays) {
                    int[] src = arr[row];
                    if (src == null || src.length == 0) {
                        continue;
                    }
                    System.arraycopy(src, 0, dst, col, src.length);
                    col += src.length;
                }
                result[row] = dst;
            }
            return result;
        }

        /** 按列拼接多个二维字符串数组，单元素列表直接返回原数组 */
        static String[][] concatColumnsString(List<String[][]> arrays) {
            if (arrays == null || arrays.isEmpty() || arrays.get(0) == null || arrays.get(0).length == 0) {
                throw new IllegalArgumentException("至少需要一个非空数组");
            }
            if (arrays.size() == 1) {
                return arrays.get(0);
            }
            int rowCount = arrays.get(0).length;
            //每行的列数，用于创建定长数组
            int[] colsPerRow = computeStringColumnsPerRow(arrays, rowCount);
            String[][] result = new String[rowCount][];
            for (int row = 0; row < rowCount; row++) {
                String[] dst = new String[colsPerRow[row]];
                int col = 0;
                for (String[][] arr : arrays) {
                    String[] src = arr[row];
                    if (src == null || src.length == 0) {
                        continue;
                    }
                    System.arraycopy(src, 0, dst, col, src.length);
                    col += src.length;
                }
                result[row] = dst;
            }
            return result;
        }

        /** 计算每行总列数（所有数组该行列数之和，null 行算 0 列） */
        private static int[] computeFloatColumnsPerRow(List<float[][]> arrays, int rowCount) {
            int[] colsPerRow = new int[rowCount];
            for (int row = 0; row < rowCount; row++) {
                int sum = 0;
                for (float[][] arr : arrays) {
                    sum += (arr[row] != null) ? arr[row].length : 0;
                }
                colsPerRow[row] = sum;
            }
            return colsPerRow;
        }

        /** 计算每行总列数（所有数组该行列数之和，null 行算 0 列） */
        private static int[] computeIntColumnsPerRow(List<int[][]> arrays, int rowCount) {
            int[] colsPerRow = new int[rowCount];
            for (int row = 0; row < rowCount; row++) {
                int sum = 0;
                for (int[][] arr : arrays) {
                    sum += (arr[row] != null) ? arr[row].length : 0;
                }
                colsPerRow[row] = sum;
            }
            return colsPerRow;
        }

        /** 计算每行总列数（所有数组该行列数之和，null 行算 0 列） */
        private static int[] computeStringColumnsPerRow(List<String[][]> arrays, int rowCount) {
            int[] colsPerRow = new int[rowCount];
            for (int row = 0; row < rowCount; row++) {
                int sum = 0;
                for (String[][] arr : arrays) {
                    sum += (arr[row] != null) ? arr[row].length : 0;
                }
                colsPerRow[row] = sum;
            }
            return colsPerRow;
        }
    }

    /**
     * TensorFlow 模型预热
     * 用 MockData 生成假数据（按签名 dtype 分 float/int/string），经 TensorBuilder 构建张量后跑一次完整推理，
     * 触发 TF 引擎图编译与内存预分配，避免首请求冷启动慢。输出结果不消费，仅释放 Tensor 资源
     */
    @Override
    public void warmUp() {
        log.info("=== warmUp ===");
        Map<String, long[]> inputShapes = modelConfig.getInputShapes();
        Map<String, DataType> dataTypes = modelConfig.getDataTypes();
        Map<String, Tensor> inputTensor = new HashMap<>();

        for (Map.Entry<String, long[]> entry : inputShapes.entrySet()) {
            long[] shape = entry.getValue();
            String inputName = entry.getKey();
            DataType dataType = dataTypes.get(inputName);
            log.info("== inputName:{} , DataType:{} , shape:{}", inputName, dataType, shape);

            if (dataType == DataType.DT_FLOAT) {
                float[][] data = MockData.floats(shape);
                inputTensor.put(inputName, TensorBuilder.buildFloat(data));
            } else if (dataType == DataType.DT_INT32) {
                int[][] data = MockData.ints(shape);
                inputTensor.put(inputName, TensorBuilder.buildInt(data));
            } else if (dataType == DataType.DT_STRING) {
                String[][] data = MockData.strings(shape);
                inputTensor.put(inputName, TensorBuilder.buildString(data));
            }
        }

        List<String> outPutNames = new ArrayList<>(modelConfig.getOutputShapes().keySet());
        log.info("== outPutNames:{}", outPutNames);
        Session.Runner runner = this.session.runner();
        inputTensor.forEach(runner::feed);
        outPutNames.forEach(runner::fetch);
        List<Tensor> outputs = runner.run();
        try {
            // 仅触发图编译与内存预分配，结果不消费
        } finally {
            // 释放输入与输出 Tensor 的原生内存
            for (Tensor input : inputTensor.values()) {
                input.close();
            }
            for (Tensor output : outputs) {
                output.close();
            }
        }

    }

    /**
     * TensorFlow 输入张量构建工具
     */
    private static final class TensorBuilder {
        /**
         * 由二维浮点数组构建 TFloat32 张量
         * @param data 二维浮点数组，例如 [[1.5],[2.0]]
         */
        static TFloat32 buildFloat(float[][] data) {
            return TFloat32.tensorOf(StdArrays.shapeOf(data), t -> StdArrays.copyTo(data, t));
        }

        /**
         * 由二维整型数组构建 TInt32 张量
         * @param data 二维整型数组，例如 [[1],[2]]
         */
        static TInt32 buildInt(int[][] data) {
            return TInt32.tensorOf(StdArrays.shapeOf(data), t -> StdArrays.copyTo(data, t));
        }

        /**
         * 由二维字符串数组构建 TString 张量
         * 逐字保留原三步写法：先建 NdArray，再 copyTo，再 tensorOf。不得改为单步 tensorOf(shape, consumer)
         * @param data 二维字符串数组，例如 [["25","M"],["30","F"]]
         */
        static TString buildString(String[][] data) {
            NdArray<String> src = NdArrays.ofObjects(String.class, StdArrays.shapeOf(data));
            StdArrays.copyTo(data, src);
            return TString.tensorOf(src);
        }
    }

    /**
     * 预热用模拟数据生成工具
     */
    private static final class MockData {
        private static final int BATCH = 10;

        /**
         * 生成预热的浮点模拟数据，所有元素填充 0.0f
         * @param shape 张量形状，须为二维，例如 [-1,3] 或 [10,3]
         * @return 模拟二维浮点数组（BATCH×dim）；形状非法时返回 null
         */
        static float[][] floats(long[] shape) {
            if (shape == null || shape.length != 2) {
                return null;
            }
            int dim = (int) shape[1];
            float[][] result = new float[BATCH][dim];
            for (int i = 0; i < BATCH; i++) {
                for (int j = 0; j < dim; j++) {
                    result[i][j] = 0.f;
                }
            }
            return result;
        }

        /**
         * 生成预热的整型模拟数据，所有元素填充 0
         * @param shape 张量形状，须为二维，例如 [-1,2] 或 [10,2]
         * @return 模拟二维整型数组（BATCH×dim）；形状非法时返回 null
         */
        static int[][] ints(long[] shape) {
            if (shape == null || shape.length != 2) {
                return null;
            }
            int dim = (int) shape[1];
            int[][] result = new int[BATCH][dim];
            for (int i = 0; i < BATCH; i++) {
                for (int j = 0; j < dim; j++) {
                    result[i][j] = 0;
                }
            }
            return result;
        }

        /**
         * 生成预热的字符串模拟数据，所有元素填充 "NAN"
         * @param shape 张量形状，须为二维，例如 [-1,1] 或 [10,1]
         * @return 模拟二维字符串数组（BATCH×dim）；形状非法时返回 null
         */
        static String[][] strings(long[] shape) {
            if (shape == null || shape.length != 2) {
                return null;
            }
            int dim = (int) shape[1];
            String[][] result = new String[BATCH][dim];
            for (int i = 0; i < BATCH; i++) {
                for (int j = 0; j < dim; j++) {
                    result[i][j] = "NAN";
                }
            }
            return result;
        }
    }

}
