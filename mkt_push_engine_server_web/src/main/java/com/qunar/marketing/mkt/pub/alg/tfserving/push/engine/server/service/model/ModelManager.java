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
    private String localModelDir = "./models";
    private String bucketName = "qunar";
    private List<ModelInfoSnapshot> modelInfoSnapshots;
    @Resource
    private AWS3Service awS3Service;

    /**
     * 模型管理器初始化
     * <p>在 Spring 容器启动时自动执行，加载模型列表并初始化模型实例到缓存
     * @throws IOException 模型初始化失败
     * @throws Exception OSS连接失败
     */
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

    /**
     * 加载模型并预热
     * @param modelInfoSnapshot 模型信息快照
     * @param verifyModelWithVersion 模型版本标识
     * @throws IllegalArgumentException 不支持的模型类型
     */
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

    /**
     * 模型预热
     * @param model 模型实例
     */
    private void warmUp(Model model) {
        model.warmUp();
    }

    /**
     * 根据模型名获取最新版本模型
     * @param modelName 模型名称
     * @return 模型实例
     * @throws ParamException 模型不存在
     */
    public Model getModel(String modelName) {return getModel(modelName, -1);}

    /**
     * 根据模型Key获取模型
     * @param modelKey 模型Key，格式：{modelName}_version_{version}
     * @return 模型实例
     * @throws ParamException 模型不存在
     */
    public Model getModelFromKey(String modelKey){
        Model model = modelContainer.get(modelKey);
        if (model == null) {
            throw new ParamException(modelKey);
        }
        return model;
    }

    /**
     * 根据模型名和版本获取模型
     * @param modelName 模型名称
     * @param version 模型版本，0或-1表示获取最新版本
     * @return 模型实例
     * @throws ParamException 模型不存在
     */
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

    /**
     * 根据模型名搜索模型（模糊匹配版本）
     * @param modelName 模型名称
     * @param version 模型版本
     * @return 模型实例
     * @throws ParamException 模型不存在
     */
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
