package com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.model;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.google.common.collect.Maps;
import com.qunar.flight.qmonitor.QMonitor;
import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.FileUtils;
import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.aws.AWS3Service;
import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.exception.ParamException;
import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.model.Model;
import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.model.ModelInfoSnapshot;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

//模型管理，负责模型的下载、解压、注册和查询
@Service
public class ModelManager {
    private static Logger LOGGER = LoggerFactory.getLogger(ModelManager.class);

    @Resource
    private AWS3Service awS3Service;

    public Map<String, Model> modelContainer = Maps.newConcurrentMap();
    /**
     * 模型名 -> 模型文件
     */
    private Map<String, Model> latestByName = Maps.newConcurrentMap();
    /**
     * 模型名 -> 模型最大版本号
     */
    private Map<String, Integer> latestVersionByName = Maps.newConcurrentMap();

    private String dictListName = "dictInfo.list";

    private String localModelDir = System.getProperty("model.local.dir", "/home/q/www/models");

    private List<ModelInfoSnapshot> modelInfoSnapshots;


    /**
     * 模型管理器初始化方法
     */
    @PostConstruct
    public void init() {
        try {
            FileUtils.createIfNoDir(localModelDir);
            awS3Service.downloadFile(dictListName, localModelDir + "/" + dictListName);
            String jsonContent = readJsonFile(localModelDir + "/" + dictListName);
            LOGGER.info("== Content from localModelDir: {}", jsonContent);
            modelInfoSnapshots = parseToListEntity(jsonContent);
            for (ModelInfoSnapshot modelInfoSnapshot : modelInfoSnapshots) {
                try {
                    String s3key = modelInfoSnapshot.modelNameWithVersion() + ".zip";
                    awS3Service.downloadFile(s3key, localModelDir + "/" + s3key);
                    awS3Service.unzipFile(localModelDir + "/" + s3key, localModelDir);
                    loadModel(modelInfoSnapshot, modelInfoSnapshot.modelNameWithVersion());
                } catch (Exception e) {
                    LOGGER.error("=== load model failed, name={} version={}", modelInfoSnapshot.getModelName(), modelInfoSnapshot.getVersion(), e);
                    // QMonitor.recordOne("model.load.failed");
                }
            }
            LOGGER.info("== model init done, loaded count={}", modelContainer.size());
        } catch (IOException e) {
            LOGGER.error("=== model init failed", e);
            // QMonitor.recordOne("model.init.failed");
        }
    }

    /**
     * 加载并注册单个模型，以 "模型名_version_版本号" 作为 key 放入 modelContainer 容器
     * @param modelInfoSnapshot      模型快照信息
     * @param verifyModelWithVersion 用于异常提示的模型版本字符串
     * @throws IllegalArgumentException 模型类型不支持或加载失败时抛出
     */
    public void loadModel(ModelInfoSnapshot modelInfoSnapshot, String verifyModelWithVersion) {
        try {
            LOGGER.info("== model register, name={} version={}", modelInfoSnapshot.getModelName(), modelInfoSnapshot.getVersion());
            // 1.加载模型
            String modelKey = modelInfoSnapshot.modelNameWithVersion();
            Model model = Model.getInstance(modelKey);
            // 2.模型预热
            model.warmUp();
            LOGGER.info("== model warmUp, name={} version={}", modelKey, modelInfoSnapshot.getVersion());
            modelContainer.put(modelKey, model);
            updateLatestByName(modelInfoSnapshot, model);
        } catch (Exception e) {
            LOGGER.error("=== model error, name={}", modelInfoSnapshot.getModelName());
            throw new IllegalArgumentException("Unsupported model name: " + modelInfoSnapshot.getModelName());
        }

    }

    /**
     * 维护同名模型的最大版本索引
     */
    private void updateLatestByName(ModelInfoSnapshot snapshot, Model model) {
        String modelName = snapshot.getModelName();
        Integer registered = latestVersionByName.get(modelName);
        if (registered == null || snapshot.getVersion() >= registered) {
            latestByName.put(modelName, model);
            latestVersionByName.put(modelName, snapshot.getVersion());
        }
    }

    /**
     * 根据模型 key 精确获取模型
     * @param modelKey 模型 key（模型名_version_版本号）
     * @return 匹配到的模型实例
     * @throws ParamException 模型 key 不存在时抛出
     */
    public Model getModelFromKey(String modelKey) {
        Model model = modelContainer.get(modelKey);
        if (model == null) {
            throw new ParamException(modelKey);
        }
        return model;
    }

    /**
     * 读取 JSON 文件内容为字符串
     * @param filePath 文件路径
     * @return 文件内容字符串
     * @throws IOException 读取失败时抛出，由调用方统一处理，避免返回 null 导致下游 NPE
     */
    private static String readJsonFile(String filePath) throws IOException {
        LOGGER.info("== read json file, path={}", filePath);
        StringBuilder content = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(Files.newInputStream(Paths.get(filePath)), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                content.append(line);
            }
        }
        return content.toString();
    }

    /**
     * 将 JSON 字符串解析为模型快照列表
     * @param jsonContent JSON 格式的字符串内容
     * @return 解析得到的模型快照列表
     */
    private static List<ModelInfoSnapshot> parseToListEntity(String jsonContent) {
        List<ModelInfoSnapshot> ModelLists = JSON.parseObject(jsonContent, new TypeReference<List<ModelInfoSnapshot>>() {
        });
        LOGGER.info("== parse dictInfo.list: count={}", ModelLists.size());
        for (ModelInfoSnapshot model : ModelLists) {
            LOGGER.info("== modelInfo: {}", model.toJsonString());
        }
        return ModelLists;
    }

}
