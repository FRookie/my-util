package org.example.common.sql;

public interface On {

    /**
     * 连表的条件，默认左边是左表，右边是右边，可重复多个，但必须成对出现
     * @param keyPairs
     * @return
     */
    BaseStatement on(String... keyPairs);
}
