package com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.feature;

import com.qunar.redis.storage.Sedis3;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.awt.*;
import java.util.*;
import java.util.List;

@Service
@Slf4j
public class FeaturePlatformServer {

   @Resource(name="featureStorge")
   private Sedis3 featureStorge;

    @Resource(name="featureStorge1")
    private Sedis3 featureStorge1;

    @Resource(name="featureStorge2")
    private Sedis3 featureStorge2;


    /**
     * 获取用户、物品及用户-物品交叉三类特征并合并为统一特征集
     * <p>补充说明：依次从 Redis 拉取 user_id 维度、item 维度、UserItem 交叉维度特征，分别记录耗时，最后调用 mergeFeatures 合并
     * @param featureskey 特征名映射，key 为分组名（user_id/item/UserItem），value 为特征名列表
     * @param featuresD 特征数据类型映射，key 与 featureskey 对应，value 为各特征的数据类型编码
     * @param user_id 用户ID
     * @param item_ids 物品ID列表
     * @return 合并后的 FeatureValues，包含全部三类特征
     */
    public FeatureValues getFeatureValues(Map<String, List<String>> featureskey, Map<String, List<Integer>> featuresD, String user_id, List<String> item_ids) {
        List<String> userFeatureName = featureskey.get("user_id");
        List<Integer> userFeatureD = featuresD.get("user_id");
        long start1 = System.currentTimeMillis();
        FeatureValues userFeatures = getUserFeature(userFeatureName, userFeatureD, user_id);
        log.debug("== userFeatures time: {}ms", System.currentTimeMillis() - start1);

        List<String> itemFeatureName = featureskey.get("item");
        List<Integer> itemFeatureD = featuresD.get("item");
        start1 = System.currentTimeMillis();
        List<FeatureValues> itemFeatures = getItemFeature(itemFeatureName, itemFeatureD, item_ids);
        log.debug("== itemFeatures time: {}ms", System.currentTimeMillis() - start1);

        List<String> userItemFeatureName = featureskey.get("UserItem");
        List<Integer> userItemFeatureD = featuresD.get("UserItem");
        start1 = System.currentTimeMillis();
        List<FeatureValues> userItemFeatures = getUserItemFeature(userItemFeatureName, userItemFeatureD, user_id, item_ids);
        log.debug("== userItemFeatures time: {}ms", System.currentTimeMillis() - start1);
        return mergeFeatures(userFeatures, itemFeatures, userItemFeatures, item_ids.size());
    }

    /**
     * 从 Redis 获取单个用户的特征值
     * <p>补充说明：以 "user:{user_id}" 为 key 批量 hmget 读取特征值，再调用 fillfeatureValues 按类型填充到 FeatureValues
     * @param feature_names 特征名列表
     * @param featuresD 与 feature_names 一一对应的数据类型编码列表
     * @param user_id 用户ID
     * @return 填充好特征值的 FeatureValues
     */
    public FeatureValues getUserFeature(List<String> feature_names, List<Integer> featuresD, String user_id) {
        FeatureValues featuserValues = new FeatureValues();
        long start1 = System.currentTimeMillis();
        List<String> featureValues = featureStorge.hmget("user:" + user_id, feature_names.toArray(new String[0]));
        log.debug("== redis time: {}ms", System.currentTimeMillis() - start1);
        fillfeatureValues(featuserValues, feature_names, featureValues, featuresD);
        return featuserValues;
    }

    /**
     * 批量获取多个物品的特征值
     * <p>补充说明：item_ids 为空时直接返回空列表；否则逐个物品以 "item:{item_id}" 为 key 调用 hmget 读取并填充特征
     * @param feature_names 特征名列表
     * @param featuresD 与 feature_names 一一对应的数据类型编码列表
     * @param item_ids 物品ID列表
     * @return 每个物品对应的 FeatureValues 列表，顺序与 item_ids 一致
     */
    public List<FeatureValues> getItemFeature(List<String> feature_names, List<Integer> featuresD, List<String> item_ids) {
        if (item_ids.isEmpty()) {
            return new ArrayList<>();
        }
        List<FeatureValues> itemFeatures = new ArrayList<>();
        for (String item_id : item_ids) {
            FeatureValues featureValues = new FeatureValues();
            List<String> featureValuesList = featureStorge1.hmget("item:" + item_id, feature_names.toArray(new String[0]));
            fillfeatureValues(featureValues, feature_names, featureValuesList, featuresD);
            itemFeatures.add(featureValues);
        }
        return itemFeatures;
    }

    /**
     * 批量获取用户与物品交叉维度的特征值
     * <p>补充说明：item_ids 为空时直接返回空列表；否则逐个物品以 "user_item:{user_id}_{item_id}" 为 key 调用 hmget 读取并填充特征
     * @param feature_names 特征名列表
     * @param featuresD 与 feature_names 一一对应的数据类型编码列表
     * @param user_id 用户ID
     * @param item_ids 物品ID列表
     * @return 每个物品对应的交叉特征 FeatureValues 列表，顺序与 item_ids 一致
     */
    public List<FeatureValues> getUserItemFeature(List<String> feature_names, List<Integer> featuresD, String user_id, List<String> item_ids) {
        if (item_ids.isEmpty()) {
            return new ArrayList<>();
        }
        List<FeatureValues> useritemFeatures = new ArrayList<>();
        for (String item_id : item_ids) {
            FeatureValues featureValues = new FeatureValues();
            List<String> featurevaues = featureStorge2.hmget("user_item:" + user_id + "_" + item_id, feature_names.toArray(new String[0]));
            fillfeatureValues(featureValues, feature_names, featurevaues, featuresD);
            useritemFeatures.add(featureValues);
        }
        return useritemFeatures;

    }

    /**
     * 将 Redis 读回的原始特征字符串按数据类型解析并填充到 FeatureValues
     * <p>补充说明：按 featuresD 中的类型编码分流：1=float 标量、2=int 标量、7=string 标量、11=float 数组、22=int 数组、77=string 数组；解析失败时该元素填 0.0f；最后分别设置 float/int/string 三类特征矩阵
     * @param featureValues 待填充的目标 FeatureValues
     * @param feature_names 特征名列表
     * @param featurevaues 与 feature_names 一一对应的特征原始字符串值
     * @param featuresD 与 feature_names 一一对应的数据类型编码列表
     */
    public static void fillfeatureValues(FeatureValues featureValues, List<String> feature_names, List<String> featurevaues,  List<Integer> featuresD) {
        Map<String, float[][]> floatFeatureValues = new HashMap<>();
        Map<String, int[][]> intFeatureValues= new HashMap<>();
        Map<String, String[][]>  stringFeatureValues = new HashMap<>();
        long start1 = System.currentTimeMillis();
        for (int kk = 0; kk < featurevaues.size(); kk++) {
            String feature_value = featurevaues.get(kk);
            String feature_name = feature_names.get(kk);
            Integer feature_dtype = featuresD.get(kk);
            if (feature_dtype == 1) {
                floatFeatureValues.put(feature_name, new float[][]{{ Float.parseFloat(feature_value) }});
            } else if (feature_dtype == 7) {
                stringFeatureValues.put(feature_name, new String[][]{{ feature_value }});
            } else if (feature_dtype == 2) {
                intFeatureValues.put(feature_name, new int[][]{{ Integer.parseInt(feature_value) }});
            } else if (feature_dtype == 11) {
                String[] values = feature_value.split(",");
                float[] result = new float[values.length];
                for (int i = 0; i < values.length; i++) {
                    String s = values[i];
                    try {
                        result[i] = (s == null || s.trim().isEmpty())
                                ? 0.0f
                                : Float.parseFloat(s.trim());
                    } catch (NumberFormatException e) {
                        result[i] = 0.0f;
                    }
                }
                float[][] results = new float[1][];
                results[0] = result;
                floatFeatureValues.put(feature_name, results);
            } else if (feature_dtype == 77) {
                String[] values = feature_value.split(",");
                String[][] results = new String[1][];
                results[0] = values;
                stringFeatureValues.put(feature_name, results);
            }  else if (feature_dtype == 22) {
                String[] values = feature_value.split(",");
                int[] result = Arrays.stream(values).mapToInt(Integer::parseInt).toArray();
                int[][] results = new int[1][];
                results[0] = result;
                intFeatureValues.put(feature_name, results);
            } else {
                log.error("-----------error dtype");
            }
        }
        log.debug("== fill feature time: {}ms", System.currentTimeMillis() - start1);
        featureValues.setFloatFeatureValues(floatFeatureValues);
        featureValues.setIntFeatureValues(intFeatureValues);
        featureValues.setStringFeatureValues(stringFeatureValues);
    }

    /**
     * 将单条用户特征按批次大小复制扩展为批量特征
     * <p>补充说明：batchSize<=1 时原样返回；否则把 float/int/string 三类特征的第一行各复制 batchSize 次组成新矩阵，行数与多物品特征（mergeItemFeatures 产出 batchSize 行）对齐后拼接
     * @param userFeatures 原始单条用户特征
     * @param batchSize 批次大小（物品数量）
     * @return 扩展后的批量用户特征 FeatureValues
     */
    public static FeatureValues fillUserFeatures(FeatureValues userFeatures, int batchSize) {
        if (batchSize <= 1) {
            return userFeatures;
        } else {
            FeatureValues featureValues = new FeatureValues();
            userFeatures.getFloatFeatureValues().forEach((k,v) ->{
                float[][] newValues = new float[batchSize][];
                Arrays.fill(newValues, v[0]);
                featureValues.getFloatFeatureValues().put(k, newValues);
            });
            userFeatures.getIntFeatureValues().forEach((k,v) ->{
                int[][] newValues = new int[batchSize][];
                Arrays.fill(newValues, v[0]);
                featureValues.getIntFeatureValues().put(k, newValues);
            });
            userFeatures.getStringFeatureValues().forEach((k,v) ->{
                String[][] newValues = new String[batchSize][];
                Arrays.fill(newValues, v[0]);
                featureValues.getStringFeatureValues().put(k, newValues);
            });
            return featureValues;
        }
    }

    /**
     * 将多个物品的特征按特征名维度合并为一个 FeatureValues
     * <p>补充说明：以第一个物品的特征名集合为基准，对 int/float/string 三类特征分别把每个物品该特征的第 0 行收集为数组，形成 [物品数] 维度的二维矩阵
     * @param itemFeatures 多个物品的特征列表
     * @return 合并后的 FeatureValues
     */
    public static FeatureValues mergeItemFeatures(List<FeatureValues> itemFeatures) {
        FeatureValues featureValues = new FeatureValues();
        Set<String> intFeatureNames = itemFeatures.get(0).getIntFeatureValues().keySet();
        Set<String> stringFeatureNames = itemFeatures.get(0).getStringFeatureValues().keySet();
        Set<String> floatFeatureNames = itemFeatures.get(0).getFloatFeatureValues().keySet();

        for (String feature_name : intFeatureNames) {
            int[][] values = new int[itemFeatures.size()][];
            for (int i = 0; i < itemFeatures.size(); i++) {
                int[] value = itemFeatures.get(i).getIntFeatureValues().get(feature_name)[0];
                values[i] = value;
            }
            featureValues.getIntFeatureValues().put(feature_name, values);
        }
        for (String feature_name : floatFeatureNames) {
            float[][] values = new float[itemFeatures.size()][];
            for (int i = 0; i < itemFeatures.size(); i++) {
                float[] value = itemFeatures.get(i).getFloatFeatureValues().get(feature_name)[0];
                values[i] = value;
            }
            featureValues.getFloatFeatureValues().put(feature_name, values);
        }
        for (String feature_name : stringFeatureNames) {
            String[][] values = new String[itemFeatures.size()][];
            for (int i = 0; i < itemFeatures.size(); i++) {
                String[] value = itemFeatures.get(i).getStringFeatureValues().get(feature_name)[0];
                values[i] = value;
            }
            featureValues.getStringFeatureValues().put(feature_name, values);
        }
        return featureValues;

    }

    /**
     * 将第二个特征集合并入第一个特征集并返回合并结果
     * <p>补充说明：将 featureValues2 的 int/float/string 三类特征逐条 put 进 featureValues1（同名键会被覆盖），直接修改并返回 featureValues1
     * @param featureValues1 合并目标，会被原地修改
     * @param featureValues2 待并入的特征集
     * @return 合并后的 featureValues1
     */
    public static FeatureValues merge2(FeatureValues featureValues1, FeatureValues featureValues2) {
        //FeatureValues featureValues = new FeatureValues();
        featureValues2.getIntFeatureValues().forEach((k,v) ->{
            featureValues1.getIntFeatureValues().put(k, v);
        });
        featureValues2.getFloatFeatureValues().forEach((k,v) ->{
            featureValues1.getFloatFeatureValues().put(k, v);
        });
        featureValues2.getStringFeatureValues().forEach((k,v) ->{
            featureValues1.getStringFeatureValues().put(k, v);
        });
        return featureValues1;
    }

    /**
     * 将三个特征集合并为一个
     * <p>补充说明：先合并前两个，再合并第三个，依赖 merge2 的原地合并语义
     * @param featureValues1 合并目标，会被原地修改
     * @param featureValues2 第一个待并入特征集
     * @param featureValues3 第二个待并入特征集
     * @return 合并后的 featureValues1
     */
    public static FeatureValues merge3(FeatureValues featureValues1, FeatureValues featureValues2, FeatureValues featureValues3) {
        return merge2(merge2(featureValues1, featureValues2), merge2(featureValues1, featureValues3));
    }

    /**
     * 根据物品及交叉特征的数量情况选择合适策略合并用户、物品、交叉特征
     * <p>补充说明：分支处理——物品特征为空时直接返回用户特征；仅一个物品且无交叉特征时 merge2；仅一个物品且有交叉特征时 merge3；多物品时先 fillUserFeatures 扩展用户特征再 mergeItemFeatures 合并物品（及交叉）特征，最后 merge2/merge3 汇总
     * @param userFeatures 用户特征
     * @param itemFeatures 物品特征列表
     * @param userItemFeatures 用户-物品交叉特征列表
     * @param batchSize 批次大小（物品数量）
     * @return 合并后的 FeatureValues
     */
    public static FeatureValues mergeFeatures(FeatureValues userFeatures, List<FeatureValues> itemFeatures, List<FeatureValues> userItemFeatures, int batchSize) {
        long start1 = System.currentTimeMillis();
        if (itemFeatures.isEmpty()) {
            log.debug("== merge features time1: {}ms", System.currentTimeMillis() - start1);
            return userFeatures;
        }
        if (itemFeatures.size() == 1 && userItemFeatures.isEmpty()) {
            log.debug("== merge features time2: {}ms", System.currentTimeMillis() - start1);
            return merge2(userFeatures, itemFeatures.get(0));
        }

        if (itemFeatures.size() == 1 && !userItemFeatures.isEmpty()) {
            log.debug("== merge features time3: {}ms", System.currentTimeMillis() - start1);
            return merge3(userFeatures, itemFeatures.get(0), userItemFeatures.get(0));
        }
        if (userItemFeatures.isEmpty()) {
            userFeatures =  fillUserFeatures(userFeatures, batchSize);
            FeatureValues itemMergeFeatures = mergeItemFeatures(itemFeatures);
            log.debug("== merge features time4: {}ms", System.currentTimeMillis() - start1);
            return merge2(userFeatures, itemMergeFeatures);
        } else {
            userFeatures =  fillUserFeatures(userFeatures, batchSize);
            FeatureValues itemMergeFeatures = mergeItemFeatures(itemFeatures);
            FeatureValues userItemMergeFeatures = mergeItemFeatures(userItemFeatures);
            log.debug("== merge features time5: {}ms", System.currentTimeMillis() - start1);
            return merge3(userFeatures, itemMergeFeatures, userItemMergeFeatures);
        }
    }

    /**
     * 将 float 二维数组格式化为可读字符串
     * <p>补充说明：逐行拼接，行内元素以逗号分隔，行外用方括号包裹
     * @param data 待格式化的 float 二维数组
     * @return 格式化后的字符串
     */
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

    /**
     * 将 int 二维数组格式化为可读字符串
     * <p>补充说明：逐行拼接，行内元素以逗号分隔，行外用方括号包裹
     * @param data 待格式化的 int 二维数组
     * @return 格式化后的字符串
     */
    public static String printInt(int[][] data) {
        StringBuilder sb = new StringBuilder();
        for (int[] row : data) {
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

    /**
     * 将 String 二维数组格式化为可读字符串
     * <p>补充说明：逐行拼接，行内元素以逗号分隔，行外用方括号包裹
     * @param data 待格式化的 String 二维数组
     * @return 格式化后的字符串
     */
    public static String printString(String[][] data) {
        StringBuilder sb = new StringBuilder();
        for (String[] row : data) {
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

    /**
     * 将 float 特征哈希表格式化为可读字符串
     * <p>补充说明：逐条以 "特征名:printFloat(值)" 形式拼接，每条换行
     * @param data 特征名到 float 二维数组的映射
     * @return 格式化后的字符串
     */
    public static String printHashFloat(Map<String, float[][]> data) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, float[][]> entry : data.entrySet()) {
            sb.append(entry.getKey() + ":");
            sb.append(printFloat(entry.getValue()));
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * 将 int 特征哈希表格式化为可读字符串
     * <p>补充说明：逐条以 "特征名:printInt(值)" 形式拼接，每条换行
     * @param data 特征名到 int 二维数组的映射
     * @return 格式化后的字符串
     */
    public static String printHashInt(Map<String, int[][]> data) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, int[][]> entry : data.entrySet()) {
            sb.append(entry.getKey() + ":");
            sb.append(printInt(entry.getValue()));
            sb.append("\n");
        }
        return sb.toString();
    }
    /**
     * 将 String 特征哈希表格式化为可读字符串
     * <p>补充说明：逐条以 "特征名:printString(值)" 形式拼接，每条换行
     * @param data 特征名到 String 二维数组的映射
     * @return 格式化后的字符串
     */
    public static String printHashString(Map<String, String[][]> data) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String[][]> entry : data.entrySet()) {
            sb.append(entry.getKey() + ":");
            sb.append(printString(entry.getValue()));
            sb.append("\n");
        }
        return sb.toString();
    }


}
