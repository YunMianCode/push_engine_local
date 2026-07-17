package com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.model;

import com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.domain.ModelType;
import lombok.Data;

/**
 * property.json 配置的强类型映射
 */
@Data
public class PropertyConfig {

    /** 模型类型字符串（property.json 中小写形式：onnx/pytorch/tensorflow），通过 toModelType() 转为枚举 */
    private String modelType;

    /**
     * 将字符串 modelType 转为 ModelType 枚举
     * <p>大小写不敏感匹配；无法识别时返回 ModelType.UNKNOWN
     * @return 对应的 ModelType 枚举
     */
    public ModelType toModelType() {
        if (modelType == null) {
            return ModelType.UNKNOWN;
        }
        switch (modelType.toLowerCase()) {
            case "onnx":
                return ModelType.ONNX;
            case "pytorch":
                return ModelType.PYTORCH;
            case "tensorflow":
                return ModelType.TENSORFLOW;
            default:
                return ModelType.UNKNOWN;
        }
    }
}
