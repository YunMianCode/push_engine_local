package com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.model;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.google.common.collect.Maps;
import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.FileUtils;
import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.aws.AWS3Service;
import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.domain.ModelConfig;
import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.domain.PredictParam;
import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.exception.ParamException;
import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.model.Model;
import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.model.ModelInfoSnapshot;
import org.bouncycastle.math.raw.Mod;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;


@Service
public class ModelManager {
    private static Logger LOGGER = LoggerFactory.getLogger(ModelManager.class);
    public Map<String, Model> modelContainer = Maps.newConcurrentMap();
    private Map<String, Integer> modellist = Maps.newConcurrentMap();
    private String dictListName = "dictInfo.list";
    private String localModelDir = "/home/q/www/models";
    private String bucketName = "qunar";
    private List<ModelInfoSnapshot> modelInfoSnapshots;
    @Resource
    private AWS3Service awS3Service;

    @PostConstruct
    public void init() {
        try {
            FileUtils.createIfNoDir(localModelDir);
            awS3Service.downloadFile(dictListName, localModelDir + "/" + dictListName);
            String contentJson = readJsonFile(localModelDir + "/" + dictListName);
            LOGGER.info("Content from localModelDir: " + contentJson);
            modelInfoSnapshots = parseToListEntity(contentJson);

            for (ModelInfoSnapshot modelInfoSnapshot : modelInfoSnapshots) {
                // 下载模型
                String s3key = modelInfoSnapshot.modelNameWithVersion() + ".zip";
                awS3Service.downloadFile(s3key, localModelDir + "/" + s3key);
                awS3Service.unzipFile(localModelDir + "/" + s3key, localModelDir);
                // 加载模型
                loadModel(modelInfoSnapshot, modelInfoSnapshot.getVersionString());
            }
        } catch (IOException e) {

        }
    }

    public void loadModel(ModelInfoSnapshot modelInfoSnapshot, String verifyModelWithVersion) {
        try {
            LOGGER.info("=== load model register, name={} version={}", modelInfoSnapshot.getModelName(),modelInfoSnapshot.getVersion());
            // 1.加载模型
            Model model = Model.getInstance(modelInfoSnapshot.getModelNameWithVersion());
            // 2. 预热
            warmUp(model);
            // 验证模型

            //
            String modelKey = modelInfoSnapshot.modelNameWithVersion();
            LOGGER.info("=== load model, name={} version={}", modelKey, modelInfoSnapshot.getVersion());
            modelContainer.put(modelKey, model);
        } catch (Exception e) {
            LOGGER.info(e.getMessage());
            e.printStackTrace();
            throw new IllegalArgumentException("Unsupported model type: " + verifyModelWithVersion);
        }

    }

    private void warmUp(Model model) {
        model.warmUp();
    }

    public Model getModel(String modelName) {return getModel(modelName, -1);}

    public Model getModelFromKey(String modelKey){
        Model model = modelContainer.get(modelKey);
        if (model == null) {
            throw new ParamException(modelKey);
        }
        return model;
    }

    public Model getModel(String modelName, int version) {

        if (version == 0 || version == -1) {
            return searchModelByName(modelName, version);
        }

        String modelKey = ModelInfoSnapshot.modelNameWithVersion(modelName, version);

        Model model = modelContainer.get(modelKey);
        if (model == null) {
            throw new ParamException(modelNotFound(modelName, version));
        }
        return searchModelByName(modelName, version);

    }

    private Model searchModelByName(String modelName, int version) {
        return modelContainer.entrySet().stream()
                .filter(entry->entry.getKey().startsWith(modelName + "_version"))
                .findFirst()
                .orElseThrow(() -> new ParamException(modelNotFound(modelName, version)))
                .getValue();
    }

    private String modelNotFound(String modelName, int version) {
        return String.format("Model with name '%s' and version '%d' not found", modelName, version);
    }

    private static String readJsonFile (String filePath) {
        LOGGER.info("=== read json file, path={}", filePath);
        StringBuilder content = new StringBuilder ();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader (
                        new FileInputStream(filePath),
                        StandardCharsets.UTF_8 // 指定编码，避免中文乱码
                ))) {
            String line;
            while ((line = br.readLine ()) != null) {
                content.append (line);
            }
        } catch (IOException e) {
            e.printStackTrace ();
            return null;
        }
        return content.toString ();
    }

    private static List<ModelInfoSnapshot> parseToListEntity(String jsonContent) {
        List<ModelInfoSnapshot> ModelLists = JSON.parseObject(
                jsonContent,
                new TypeReference<List<ModelInfoSnapshot>>() {}
        );

        LOGGER.info("\n===== 解析为 List<User> 结果 =====");
        for (ModelInfoSnapshot model : ModelLists) {
           LOGGER.info("Model Info: " + model.toJsonString());
        }
        return ModelLists;
    }

}
