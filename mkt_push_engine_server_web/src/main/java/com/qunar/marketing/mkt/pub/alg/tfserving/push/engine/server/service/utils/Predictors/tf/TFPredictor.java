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

    @Override
    public void init(ModelConfig modelConfig) {

        this.modelConfig = modelConfig;
        try {
            String modelPath = modelConfig.getModelPath() + "/saved_model";
            log.info("modelPath={}", modelPath);
            model = SavedModelBundle.load(modelPath, "serve");
            session = model.session();
            signatures = model.signatures();
            initModelConfig();
            if (!CheckModelConfig()) {
                log.error("Model config error");
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize TensorFlow predictor", e);
        }
    }

    @Override
    public PredictResult predict(PredictParam param) {
        try {
            //log.info("Predict param={}", param.toString());
            PredictResult result = PredictResult.success();
            //log.info("======22222222");
            long start = System.currentTimeMillis();

            // 把 param 转化为 TF可以接受的输入
            long start1 = System.currentTimeMillis();
            Map<String, Tensor> inputs = convertToTensor(param);
            log.info("converTensor time ={}", System.currentTimeMillis() - start1);
            //log.info("======33333333");

            start1 = System.currentTimeMillis();
            Session.Runner runner = this.session.runner();
            log.info("session run time ={}", System.currentTimeMillis() - start1);
            //log.info("======77777777");

            start1 = System.currentTimeMillis();
            inputs.forEach(runner::feed);
            log.info("feed time ={}", System.currentTimeMillis() - start1);
            //log.info("======4444444");

            Map<String, float[][]> outputResults = new HashMap<>();

//            String outPutName = getOutputName(this.modelConfig);
//            Tensor output = (TFloat32) (runner.fetch(outPutName).run().get(0));
//            float[][] results = convertTensorFloatArray(output);
//            outputResults.put(outPutName, results);

            //log.info("======555555");
            List<String> outPutNames = getOutputNames(param);
            //log.info("outputNames={}", outPutNames.toString());

            start1 = System.currentTimeMillis();
            outPutNames.forEach(runner::fetch);
            log.info("fetch time ={}", System.currentTimeMillis() - start1);
            List<Tensor> outputs = runner.run();
            for(int i = 0; i < outputs.size(); i++) {
                outputResults.put(outPutNames.get(i), convertTensorFloatArray(outputs.get(i)));
            }

//            log.info("Predictor outputs size={}", outputResults.size());
//            log.info("Predictor outputs value={}", printHashFloat(outputResults));

            result.setResults(outputResults);
            log.info("results time ={}", System.currentTimeMillis() - start);
            result.addMetadata("predictTimeMs", System.currentTimeMillis() - start);
            return result;
        } catch (Exception e) {
            log.info("Error:", e);
            log.info("TensorFlow prediction failed:");
            return PredictResult.failure("TensorFlow prediction failed: " + e.getMessage());
        }
    }

    public static String printFloat(float[][] data) {
        StringBuilder sb = new StringBuilder();
        for (float[] row : data) {
            sb.append("[");
            for (int j = 0; j < row.length; j++) {
                sb.append(row[j]);
                if (j != row.length - 1) {
                    sb.append(", "); // 最后一个元素后不加逗号
                }
            }
            sb.append("]");
        }
        return sb.toString();
    }
    public static String printHashFloat(Map<String, float[][]> data) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, float[][]> entry : data.entrySet()) {
            sb.append(entry.getKey() + ":");
            sb.append(printFloat(entry.getValue()));
            sb.append("\n");
        }
        return sb.toString();
    }

    private float[][] convertTensorFloatArray(Tensor tensor) {
        long[] shape = tensor.shape().asArray();
        int batchSize = (int)shape[0];
        int outputDim = (int) shape[1];

        float[][] result = new float[batchSize][outputDim];
        //FloatBuffer floatBuffer = FloatBuffer.allocate(batchSize * outputDim);
        StdArrays.copyFrom((TFloat32)tensor, result);
        return result;
    }

    @Override
    public void close() {
        if (session != null) {
            session.close();
        }
        if (model != null) {
            model.close();
        }
    }

    private Map<String, Tensor> convertToTensor(PredictParam param) {
        Map<String, Tensor> outputs = new HashMap<>();

//        this.modelConfig.getInputToFeaturesMap().forEach((k,v) -> {
//            String v0 = v.get(0);
//            if (param.getSubMatrices().containsKey(v0)) {
//                List<float[][]> subMatrices = new ArrayList<>();
//                v.forEach((kk) -> {
//                    subMatrices.add(param.getSubMatrices().get(kk));
//                });
//                float[][] data = concatColumnsfloat(subMatrices);
////                NdArray<Float> src_values = NdArrays.ofObjects(Float.class, StdArrays.shapeOf(values));
////                StdArrays.copyTo(values, (FloatNdArray) src_values);
//                TFloat32 value =
//                        TFloat32.tensorOf(StdArrays.shapeOf(data), t -> StdArrays.copyTo(data, t));
//                outputs.put(k, value);
//            } else if (param.getSubStringMatrices().containsKey(v0)) {
//                List<String[][]> subMatrices = new ArrayList<>();
//                v.forEach((kk) -> {
//                    subMatrices.add(param.getSubStringMatrices().get(kk));
//                });
//                String[][] data = concatColumnsString(subMatrices);
//                NdArray<String> src = NdArrays.ofObjects(String.class, StdArrays.shapeOf(data));
//                StdArrays.copyTo(data, src);
//                TString vaule = TString.tensorOf(src);
//                outputs.put(k, vaule);
//            } else {
//                List<int[][]> subMatrices = new ArrayList<>();
//                v.forEach((kk) -> {
//                    subMatrices.add(param.getSubIntMatrices().get(kk));
//                });
//                int[][] data = concatColumnsInt(subMatrices);
//                TInt32 value = TInt32.tensorOf(StdArrays.shapeOf(data), t -> StdArrays.copyTo(data, t));
//                outputs.put(k, value);
//            }
//        });
        this.modelConfig.getInputToFeaturesMap().forEach((k,v) -> {
            String v0 = v.get(0);
//            log.info("k={}", k);
//            log.info("v0={}", v0);
            if (param.getSubMatrices().containsKey(v0)) {
                List<float[][]> subMatrices = new ArrayList<>();
                v.forEach((kk) -> {
                    subMatrices.add(param.getSubMatrices().get(kk));
                });
                //log.info("xxxx5555555subMatrices size={}", subMatrices.size());
                float[][] data = concatColumnsfloat(subMatrices);
//                NdArray<Float> src_values = NdArrays.ofObjects(Float.class, StdArrays.shapeOf(values));
//                StdArrays.copyTo(values, (FloatNdArray) src_values);
                TFloat32 value =
                        TFloat32.tensorOf(StdArrays.shapeOf(data), t -> StdArrays.copyTo(data, t));
                //log.info("xxxxx=========555555555");
                outputs.put(k, value);
            } else if (param.getSubStringMatrices().containsKey(v0)) {
                List<String[][]> subMatrices = new ArrayList<>();
                v.forEach((kk) -> {
                    subMatrices.add(param.getSubStringMatrices().get(kk));
                });
                //log.info("xxxx6666666subMatrices size={}", subMatrices.size());
                String[][] data = concatColumnsString(subMatrices);
                NdArray<String> src = NdArrays.ofObjects(String.class, StdArrays.shapeOf(data));
                StdArrays.copyTo(data, src);
                TString vaule = TString.tensorOf(src);
                //log.info("xxxxx=========6666666");
                outputs.put(k, vaule);
            } else {
                List<int[][]> subMatrices = new ArrayList<>();
                v.forEach((kk) -> {
                    subMatrices.add(param.getSubIntMatrices().get(kk));
                });
                //log.info("xxxx7777777subMatrices size={}", subMatrices.size());
                int[][] data = concatColumnsInt(subMatrices);
                TInt32 value = TInt32.tensorOf(StdArrays.shapeOf(data), t -> StdArrays.copyTo(data, t));
                //log.info("xxxx=========7777777");
                outputs.put(k, value);
            }
        });

        //log.info("xxxxxx=============11111");

        return outputs;
    }

    private List<String> getOutputNames(PredictParam param) {
        return param.getOutputs();
    }

    private String getOutputName(PredictParam param) {
        return param.getOutputs().get(0);
    }

    private void initModelConfig() {
        this.modelConfig.setInputMap((this.signatures).get(0).getInputs());
        this.modelConfig.setOutputMap((this.signatures).get(0).getOutputs());
        Map<String, DataType> dataTypes = new HashMap<>();
        Map<String, long[]> inputShapes = new HashMap<>();
        Map<String, long[]> outputShapes = new HashMap<>();

        for(Signature signature : this.signatures) {
            signature.getInputs().forEach((key, input) -> {
                DataType dataType = input.dataType;
                Shape  inputShape = input.shape;
                String name = input.name;
                String[] names = name.split(":");
                inputShapes.put(names[0], inputShape.asArray());
                dataTypes.put(names[0], dataType);
            });
            signature.getOutputs().forEach((key, output) -> {
                DataType dataType = output.dataType;
                Shape  outputShape = output.shape;
                String name = output.name;
                String[] names = name.split(":");
                outputShapes.put(names[0], outputShape.asArray());
                dataTypes.put(names[0], dataType);
            });
        }
//        log.info("inputShapes: {}", inputShapes.toString());
//        log.info("outputShapes: {}", outputShapes.toString());
        this.modelConfig.setDataTypes(dataTypes);
        this.modelConfig.setInputShapes(inputShapes);
        this.modelConfig.setOutputShapes(outputShapes);

        // input tensor -> features;
        //log.info("tensor path {}", this.modelConfig.getModelPath() + File.separator + "tensor.yml");
        TensorModelConfig tensorModelConfig = YmlConfigReader.readYamlAsObject(this.modelConfig.getModelPath() + File.separator + "tensor.yml", TensorModelConfig.class);

        this.modelConfig.setTensorModelConfig(tensorModelConfig);
        Map<String, List<String>> input2Features = new HashMap<>();
        tensorModelConfig.getInputs().forEach((k) -> {
            input2Features.put(k.getName(), k.getFeatures());
        });
        this.modelConfig.setInputToFeaturesMap(input2Features);

        List<String> outputNames = new ArrayList<>();
        outputNames.add(tensorModelConfig.getOutputs().getName());
        this.modelConfig.setOutputNames(outputNames);


        // redis feature: featuregroup-> features
        //log.info("features path {}", this.modelConfig.getModelPath() + File.separator + "features.yml");
        Map<String, List<String>> featureskey = new HashMap<>();
        Map<String, List<Integer>> feturesDtype = new HashMap<>();
        FeatureGroupConfig featureGroupConfig = YmlConfigReader.readYamlAsObject(this.modelConfig.getModelPath() + File.separator + "features.yml", FeatureGroupConfig.class);
        featureGroupConfig.getFeatures().forEach((k) -> {
            featureskey.put(k.getFeaturegroups(), k.getFeaturesnames());
            feturesDtype.put(k.getFeaturegroups(), k.getFeaturesdtype());
        });
        this.modelConfig.setFeatureskey(featureskey);
        this.modelConfig.setFeaturesDtype(feturesDtype);

        //log.info("====modelConfig= {}", this.modelConfig.toString());


    }

    boolean CheckModelConfig() {
        Map<String, long[]> inputShapes = this.modelConfig.getInputShapes();
        HashSet<String> names = new HashSet<>(inputShapes.keySet());
        TensorModelConfig tensorModelConfig = this.modelConfig.getTensorModelConfig();
        HashSet<String> tensorNames = new HashSet<>();
        for (TensorModelConfig.InputConfig intputConfig : tensorModelConfig.getInputs()) {
            tensorNames.add(intputConfig.getName());
            if (!names.contains(intputConfig.getName())) {
                log.warn("Input name not found: " + intputConfig.getName());
                return false;
            }
        }
        String outPutName = tensorModelConfig.getOutputs().getName();
        Map<String, long[]> outputShapes = this.modelConfig.getOutputShapes();
        HashSet<String> outputNames = new HashSet<>(outputShapes.keySet());
        if (!outputNames.contains(outPutName)) {
            log.warn("Output name not found: " + outPutName);
            return false;
        }
        return true;
    }

    public static float[][] concatColumnsfloat(List<float[][]> arrays) {
        // 基础参数校验
        if (arrays == null || arrays.get(0).length == 0) {
            throw new IllegalArgumentException("至少需要一个非空数组");
        }

        if (arrays.size() == 1) {
            return arrays.get(0);
        }

        // 获取总行数（以第一个数组为准）
        int rowCount = arrays.get(0).length;

        // 校验所有数组行数是否一致
        for (int i = 1; i < arrays.size(); i++) {
            if (arrays.get(i) == null) {
                throw new IllegalArgumentException("第" + (i + 1) + "个数组为null");
            }
            if (arrays.get(i).length != rowCount) {
                throw new IllegalArgumentException(
                        String.format("数组行数不匹配，第1个数组行数：%d，第%d个数组行数：%d",
                                rowCount, i + 1, arrays.get(i).length));
            }
        }

        // 预计算每行的总列数（关键优化点：减少重复计算）
        int[] totalColumnsPerRow = new int[rowCount];
        for (int row = 0; row < rowCount; row++) {
            int sum = 0;
            for (float[][] arr : arrays) {
                // 处理可能的null行（视为0列）
                sum += (arr[row] != null) ? arr[row].length : 0;
            }
            totalColumnsPerRow[row] = sum;
        }

        // 初始化结果数组
        float[][] result = new float[rowCount][];

        // 逐行拼接（外层循环行，内层循环数组）
        for (int row = 0; row < rowCount; row++) {
            int currentCol = 0;
            int totalCols = totalColumnsPerRow[row];
            result[row] = new float[totalCols]; // 一次性分配足够空间

            // 依次拼接每个数组的当前行
            for (float[][] arr : arrays) {
                float[] currentRow = arr[row];
                if (currentRow == null || currentRow.length == 0) {
                    continue; // 跳过空行
                }

                // 高效批量复制（使用native方法）
                System.arraycopy(
                        currentRow, 0,       // 源数组及起始位置
                        result[row], currentCol,  // 目标数组及起始位置
                        currentRow.length    // 复制长度
                );
                currentCol += currentRow.length;
            }
        }
        return result;
    }

    public static int[][] concatColumnsInt(List<int[][]> arrays) {
        // 基础参数校验
        if (arrays == null || arrays.get(0).length == 0) {
            throw new IllegalArgumentException("至少需要一个非空数组");
        }
        if (arrays.size() == 1) {
            return arrays.get(0);
        }
        // 获取总行数（以第一个数组为准）
        int rowCount = arrays.get(0).length;

        // 校验所有数组行数是否一致
        for (int i = 1; i < arrays.size(); i++) {
            if (arrays.get(i) == null) {
                throw new IllegalArgumentException("第" + (i + 1) + "个数组为null");
            }
            if (arrays.get(i).length != rowCount) {
                throw new IllegalArgumentException(
                        String.format("数组行数不匹配，第1个数组行数：%d，第%d个数组行数：%d",
                                rowCount, i + 1, arrays.get(i).length));
            }
        }

        // 预计算每行的总列数（关键优化点：减少重复计算）
        int[] totalColumnsPerRow = new int[rowCount];
        for (int row = 0; row < rowCount; row++) {
            int sum = 0;
            for (int[][] arr : arrays) {
                // 处理可能的null行（视为0列）
                sum += (arr[row] != null) ? arr[row].length : 0;
            }
            totalColumnsPerRow[row] = sum;
        }

        // 初始化结果数组
        int[][] result = new int[rowCount][];

        // 逐行拼接（外层循环行，内层循环数组）
        for (int row = 0; row < rowCount; row++) {
            int currentCol = 0;
            int totalCols = totalColumnsPerRow[row];
            result[row] = new int[totalCols]; // 一次性分配足够空间

            // 依次拼接每个数组的当前行
            for (int[][] arr : arrays) {
                int[] currentRow = arr[row];
                if (currentRow == null || currentRow.length == 0) {
                    continue; // 跳过空行
                }

                // 高效批量复制（使用native方法）
                System.arraycopy(
                        currentRow, 0,       // 源数组及起始位置
                        result[row], currentCol,  // 目标数组及起始位置
                        currentRow.length    // 复制长度
                );
                currentCol += currentRow.length;
            }
        }
        return result;
    }

    public static String[][] concatColumnsString(List<String[][]> arrays) {
        // 基础参数校验
        if (arrays == null || arrays.get(0).length == 0) {
            throw new IllegalArgumentException("至少需要一个非空数组");
        }
        if (arrays.size() == 1) {
            return arrays.get(0);
        }
        // 获取总行数（以第一个数组为准）
        int rowCount = arrays.get(0).length;

        // 校验所有数组行数是否一致
        for (int i = 1; i < arrays.size(); i++) {
            if (arrays.get(i) == null) {
                throw new IllegalArgumentException("第" + (i + 1) + "个数组为null");
            }
            if (arrays.get(i).length != rowCount) {
                throw new IllegalArgumentException(
                        String.format("数组行数不匹配，第1个数组行数：%d，第%d个数组行数：%d",
                                rowCount, i + 1,arrays.get(i).length));
            }
        }

        // 预计算每行的总列数（关键优化：减少重复计算）
        int[] totalColsPerRow = new int[rowCount];
        for (int row = 0; row < rowCount; row++) {
            int sum = 0;
            for (String[][] arr : arrays) {
                // 处理可能的null行（视为0列）
                sum += (arr[row] != null) ? arr[row].length : 0;
            }
            totalColsPerRow[row] = sum;
        }

        // 初始化结果数组
        String[][] result = new String[rowCount][];

        // 逐行拼接（外层循环行，内层循环数组）
        for (int row = 0; row < rowCount; row++) {
            int currentCol = 0;
            int totalCols = totalColsPerRow[row];
            result[row] = new String[totalCols]; // 一次性分配足够空间

            // 依次拼接每个数组的当前行
            for (String[][] arr : arrays) {
                String[] currentRow = arr[row];
                if (currentRow == null || currentRow.length == 0) {
                    continue; // 跳过空行
                }

                // 高效批量复制（使用System.arraycopy优化字符串引用复制）
                System.arraycopy(
                        currentRow, 0,        // 源数组及起始位置
                        result[row], currentCol, // 目标数组及起始位置
                        currentRow.length     // 复制长度
                );
                currentCol += currentRow.length;
            }
        }

        return result;
    }

    @Override
    public void warmUp() {
        log.info("warmUp=========");
        Map<String, long[]> inputShapes = modelConfig.getInputShapes();
        Map<String, DataType> dataTypes = modelConfig.getDataTypes();
        Map<String, Tensor> inputTensor = new HashMap<>();

        for (Map.Entry<String, long[]> entry : inputShapes.entrySet()) {
            long[] shape = entry.getValue();
            String inputName = entry.getKey();
            DataType dataType = dataTypes.get(inputName);
            log.info("inputName:{} , DataType:{} , shape:{}", inputName, dataType, shape);

            if (dataType == DataType.DT_FLOAT) {
                float[][] data = mockFloat(shape);
//                NdArray<Float> src_values = NdArrays.ofObjects(Float.class, StdArrays.shapeOf(values));
//                StdArrays.copyTo(values, (FloatNdArray) src_values);
                TFloat32 value =
                        TFloat32.tensorOf(StdArrays.shapeOf(data), t -> StdArrays.copyTo(data, t));
                inputTensor.put(inputName, value);
            } else if (dataType == DataType.DT_INT32) {
                int[][] data = mockInt(shape);
                TInt32 value =
                        TInt32.tensorOf(StdArrays.shapeOf(data), t -> StdArrays.copyTo(data, t));
                inputTensor.put(inputName, value);
            } else if (dataType == DataType.DT_STRING) {
                String[][] data = mockString(shape);
                NdArray<String> src = NdArrays.ofObjects(String.class, StdArrays.shapeOf(data));
                StdArrays.copyTo(data, src);
                TString value = TString.tensorOf(src);
                inputTensor.put(inputName, value);
            }
        }

        List<String> outPutNames = new ArrayList<>(modelConfig.getOutputShapes().keySet());
        log.info("outPutNames:{}", outPutNames.toString());
        Session.Runner runner = this.session.runner();
        inputTensor.forEach(runner::feed);
        outPutNames.forEach(runner::fetch);
        List<Tensor> outputs = runner.run();
        for(int i = 0; i < outputs.size(); i++) {
            outputs.get(i);
        }

    }

    private float[][] mockFloat(long[] shape) {
        if (shape == null || shape.length != 2) {
            return null;
        }
        int batch = 10;
        int dim = (int) shape[1];
        float[][] result = new float[batch][dim];
        for (int i = 0; i < batch; i++)
            for (int j = 0; j < dim; j++){
            result[i][j] = 0.f;
        }
        return result;
    }

    private int[][] mockInt(long[] shape) {
        if (shape == null || shape.length != 2) {
            return null;
        }
        int batch = 10;
        int dim = (int) shape[1];
        int[][] result = new int[batch][dim];
        for (int i = 0; i < batch; i++)
            for (int j = 0; j < dim; j++){
                result[i][j] = 0;
            }
        return result;
    }

    private String[][] mockString(long[] shape) {
        if (shape == null || shape.length != 2) {
            return null;
        }
        int batch = 10;
        int dim = (int) shape[1];
        String[][] result = new String[batch][dim];
        for (int i = 0; i < batch; i++)
            for (int j = 0; j < dim; j++){
                result[i][j] = "NAN";
            }
        return result;
    }

}
