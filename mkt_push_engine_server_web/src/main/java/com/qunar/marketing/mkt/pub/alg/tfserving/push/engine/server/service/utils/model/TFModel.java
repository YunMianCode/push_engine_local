package com.qunar.marketing.mkt.pub.alg.tfserving.push.engine.server.service.utils.model;

import org.tensorflow.SavedModelBundle;
import org.tensorflow.Session;
import org.tensorflow.Signature;
import org.tensorflow.Tensor;

import java.util.List;
import java.util.Map;

public class TFModel extends Model {
    SavedModelBundle model;
    Session session;
    List<Signature> signatures;
    String outputName =  "";
    Map<String, String> inputNames;

    public TFModel(String modelNameWithVersion, String version) {

    }

    public Tensor predict(Tensor input) {
        return null;
    }

}
