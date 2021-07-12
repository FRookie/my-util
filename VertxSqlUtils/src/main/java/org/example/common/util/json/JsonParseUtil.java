package org.example.common.util.json;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;

/**
 * @date 2021/2/2 13:32.
 */
public class JsonParseUtil {

    /**
     * 解析指定地址,返回JsonObject
     *
     * @param path 文件地址
     */
    public static Future<JsonObject> parse(String path) {
        Promise<JsonObject> promise = Promise.promise();
        ConfigStoreOptions jsonOption = new ConfigStoreOptions()
            .setType("file").setFormat("json")
            .setConfig(new JsonObject().put("path", path));
        ConfigRetrieverOptions option = new ConfigRetrieverOptions().addStore(jsonOption);
        ConfigRetriever retriever = ConfigRetriever.create(Vertx.currentContext().owner(), option);
        retriever.getConfig(configRes -> {
            if (configRes.succeeded()) {
                JsonObject jsonConfig = configRes.result();
                promise.complete(jsonConfig);
            } else {
                promise.fail(configRes.cause());
            }
        });
        return promise.future();
    }
}
