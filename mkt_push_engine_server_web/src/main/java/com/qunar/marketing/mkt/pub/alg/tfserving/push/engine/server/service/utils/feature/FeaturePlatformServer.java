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


    public FeatureValues getFeatureValues(Map<String, List<String>> featureskey, Map<String, List<Integer>> featuresD, String user_id, List<String> item_ids) {
        List<String> userFeatureName = featureskey.get("user_id");
        List<Integer> userFeatureD = featuresD.get("user_id");
        long start1 = System.currentTimeMillis();
        FeatureValues userFeatures = getUserFeature(userFeatureName, userFeatureD, user_id);
        log.info("FeaturePlatformServer userFeatures: " + (System.currentTimeMillis() - start1) + "ms");

        List<String> itemFeatureName = featureskey.get("item");
        List<Integer> itemFeatureD = featuresD.get("item");
        start1 = System.currentTimeMillis();
        List<FeatureValues> itemFeatures = getItemFeature(itemFeatureName, itemFeatureD, item_ids);
        log.info("FeaturePlatformServer itemFeatures: " + (System.currentTimeMillis() - start1) + "ms");

        List<String> userItemFeatureName = featureskey.get("UserItem");
        List<Integer> userItemFeatureD = featuresD.get("UserItem");
        start1 = System.currentTimeMillis();
        List<FeatureValues> userItemFeatures = getUserItemFeature(userItemFeatureName, userItemFeatureD, user_id, item_ids);
        log.info("FeaturePlatformServer userItemFeatures: " + (System.currentTimeMillis() - start1) + "ms");
        return mergeFeatures(userFeatures, itemFeatures, userItemFeatures, item_ids.size());
    }

    public FeatureValues getUserFeature(List<String> feature_names, List<Integer> featuresD, String user_id) {
        FeatureValues featuserValues = new FeatureValues();
//        log.info("feature_names: " + feature_names);
//        log.info("featuresD: " + featuresD);
//        log.info("user_id: " + user_id);
        long start1 = System.currentTimeMillis();
        List<String> featurevaues = featureStorge.hmget("user:" + user_id, feature_names.toArray(new String[0]));
        log.info("redis time: {} ", System.currentTimeMillis() - start1);
//        log.info("featurevaues: " + featurevaues.toString());
        fillfeatureValues(featuserValues, feature_names, featurevaues, featuresD);
        return featuserValues;
    }

    public List<FeatureValues> getItemFeature(List<String> feature_names, List<Integer> featuresD, List<String> item_ids) {
        if (item_ids.isEmpty()) {
            return new ArrayList<>();
        }

        List<FeatureValues> itemFeatures = new ArrayList<>();
        for (String item_id : item_ids) {
            FeatureValues featureValues = new FeatureValues();
            List<String> featurevaues = featureStorge1.hmget("item:" + item_id, feature_names.toArray(new String[0]));
            fillfeatureValues(featureValues, feature_names, featurevaues, featuresD);
            itemFeatures.add(featureValues);
        }
        return itemFeatures;
    }

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
        log.info("fill feature time: {} ", System.currentTimeMillis() - start1);
//        log.info("fillfeatureValues float: " + printHashFloat(floatFeatureValues));
//        log.info("fillfeatureValues int: " + printHashInt(intFeatureValues));
//        log.info("fillfeatureValues string: " + printHashString(stringFeatureValues));
        featureValues.setFloatFeatureValues(floatFeatureValues);
        featureValues.setIntFeatureValues(intFeatureValues);
        featureValues.setStringFeatureValues(stringFeatureValues);
    }

    public static FeatureValues fillUserFeatures(FeatureValues userFeatures, int batchSize) {
        if (batchSize <= 1) {
            return userFeatures;
        } else {
            FeatureValues featureValues = new FeatureValues();
            userFeatures.getFloatFeatureValues().forEach((k,v) ->{
                float[][] newValues = new float[batchSize + 1][];
                for (int i = 0; i < batchSize + 1; i++) {
                    newValues[i] = v[0];
                }
                featureValues.getFloatFeatureValues().put(k, newValues);
            });
            userFeatures.getStringFeatureValues().forEach((k,v) ->{
                String[][] newValues = new String[batchSize + 1][];
                for (int i = 0; i < batchSize + 1; i++) {
                    newValues[i] = v[0];
                }
                featureValues.getStringFeatureValues().put(k, newValues);
            });
            userFeatures.getFloatFeatureValues().forEach((k,v) ->{
                float[][] newValues = new float[batchSize + 1][];
                for (int i = 0; i < batchSize + 1; i++) {
                    newValues[i] = v[0];
                }
                featureValues.getFloatFeatureValues().put(k, newValues);
            });
            return featureValues;
        }
    }

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

    public static FeatureValues merge3(FeatureValues featureValues1, FeatureValues featureValues2, FeatureValues featureValues3) {
        return merge2(merge2(featureValues1, featureValues2), merge2(featureValues1, featureValues3));
    }

    public static FeatureValues mergeFeatures(FeatureValues userFeatures, List<FeatureValues> itemFeatures, List<FeatureValues> userItemFeatures, int batchSize) {
        long start1 = System.currentTimeMillis();
        if (itemFeatures.isEmpty()) {
            log.info("merge features time1: {}", System.currentTimeMillis() - start1);
            return userFeatures;
        }
        if (itemFeatures.size() == 1 && userItemFeatures.isEmpty()) {
            log.info("merge features time2: {}", System.currentTimeMillis() - start1);
            return merge2(userFeatures, itemFeatures.get(0));
        }

        if (itemFeatures.size() == 1 && !userItemFeatures.isEmpty()) {
            log.info("merge features time3: {}", System.currentTimeMillis() - start1);
            return merge3(userFeatures, itemFeatures.get(0), userItemFeatures.get(0));
        }
        if (userItemFeatures.isEmpty()) {
            userFeatures =  fillUserFeatures(userFeatures, batchSize);
            FeatureValues itemMergeFeatures = mergeItemFeatures(itemFeatures);
            log.info("merge features time4: {}", System.currentTimeMillis() - start1);
            return merge2(userFeatures, itemMergeFeatures);
        } else {
            userFeatures =  fillUserFeatures(userFeatures, batchSize);
            FeatureValues itemMergeFeatures = mergeItemFeatures(itemFeatures);
            FeatureValues userItemMergeFeatures = mergeItemFeatures(userItemFeatures);
            log.info("merge features time5: {}", System.currentTimeMillis() - start1);
            return merge3(userFeatures, itemMergeFeatures, userItemMergeFeatures);
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

    public static String printHashFloat(Map<String, float[][]> data) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, float[][]> entry : data.entrySet()) {
            sb.append(entry.getKey() + ":");
            sb.append(printFloat(entry.getValue()));
            sb.append("\n");
        }
        return sb.toString();
    }

    public static String printHashInt(Map<String, int[][]> data) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, int[][]> entry : data.entrySet()) {
            sb.append(entry.getKey() + ":");
            sb.append(printInt(entry.getValue()));
            sb.append("\n");
        }
        return sb.toString();
    }
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
