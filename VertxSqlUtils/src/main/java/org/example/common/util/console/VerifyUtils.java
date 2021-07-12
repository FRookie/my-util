package org.example.common.util.console;

import io.vertx.core.MultiMap;

import org.example.common.annotation.Verify;
import org.example.common.util.db.SqlUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.utils.StringUtils;

import java.lang.reflect.Field;
import java.util.Collection;

public class VerifyUtils {

    private static Logger logger = LoggerFactory.getLogger(VerifyUtils.class);

    /**
     * 验证当前request请求中的参数是否符合注解设定的规则
     *
     * @param clz           类对象
     * @param requestParams 请求中的参数，通过request.params()取得
     * @param exceptParams  不需要验证的参数集合
     * @param <T>           泛型
     */
    public static <T> void verifyExceptParams(Class<T> clz, MultiMap requestParams, Collection<String> exceptParams) {
        Field[] fields = clz.getDeclaredFields();
        for (Field field : fields) {
            String name = field.getName();
            // 当前属性不是需要验证的属性,continue
            if (SqlUtils.isSerializableId(field) || exceptParams.contains(name)) {
                continue;
            }
            verifyOneParam(field, requestParams, name);
        }
    }

    /**
     * 验证验证当前请求中的参数是否符合注解设定的规则
     *
     * @param clz           类对象
     * @param requestParams 请求中的参数，通过request.params()取得
     * @param params        需要验证的参数集合
     * @param <T>           泛型
     */
    public static <T> void verifyParams(Class<T> clz, MultiMap requestParams, Collection<String> params) {
        Field[] fields = clz.getDeclaredFields();
        for (Field field : fields) {
            String name = field.getName();
            // 当前属性不是需要验证的属性,continue
            if (SqlUtils.isSerializableId(field) || !params.contains(name)) {
                continue;
            }
            verifyOneParam(field, requestParams, name);
        }
    }

    /**
     * 验证单个参数是否通过注解验证
     *
     * @param field    属性
     * @param multiMap 请求中的参数，通过request.params取得
     * @param name     待验证的参数名
     */
    private static void verifyOneParam(Field field, MultiMap multiMap, String name) {
        Verify annotation = field.getAnnotation(Verify.class);
        if (annotation != null) {
            // 是否非空
            boolean isNotBlank = annotation.isNotBlank();
            // 最大长度
            int maxLength = annotation.maxLength();
            // 非空为空 抛异常
            String param = multiMap.get(name);
            if (isNotBlank) {
                String msg = "parameter: " + name + " cannot not null or blank";
                if (StringUtils.isBlank(param)) {
                    throw new IllegalArgumentException(msg);
                }
            }
            // param非空，且注解最大长度不为0时，超过最大长度 抛异常
            if (param != null && maxLength != 0) {
                if (param.length() > maxLength) {
                    String msg = "parameter： " + name + " 's length can not exceed " + maxLength;
                    throw new IllegalArgumentException(msg);
                }
            }
        }
    }
}
