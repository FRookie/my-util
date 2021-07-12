package org.example.common.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 字符串工具类
 *
 * @date 2021/2/6
 */
public class StringUtils {

    // 正则匹配 "${word}" word只能由字母数字下划线组成
    private static final String REGEX = "\\$\\{\\w+\\}";

    public static String processTemplate(String template, Map<String, Object> params) {
        if (params == null) {
            params = new HashMap<>();
        }
        Matcher m = Pattern.compile(REGEX).matcher(template);
        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String param = m.group();
            Object value = params.get(param.substring(2, param.length() - 1));
            m.appendReplacement(sb, value == null ? "" : value.toString());
        }
        m.appendTail(sb);
        return sb.toString();
    }


    /**
     * double 如果小数点后面太多0会导致转成科学计算7.5E-4
     */
    public static String doubleToString(double value) {
        return BigDecimal.valueOf(value)
            .setScale(8, RoundingMode.HALF_UP)
            .toPlainString();
    }

    public static String nullToBlank(Object str) {
        return (str == null) ? "" : str.toString();
    }
}
