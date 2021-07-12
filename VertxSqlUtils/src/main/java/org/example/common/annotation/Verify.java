package org.example.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Verify {

    /**
     * 是否非空（包括null，空字符串，全部空格），默认不允许为空
     */
    boolean isNotBlank() default true;

    /**
     * 传入参数字符串最大长度
     * 默认为0，为0时不进行校验
     */
    int maxLength() default 0;
}
