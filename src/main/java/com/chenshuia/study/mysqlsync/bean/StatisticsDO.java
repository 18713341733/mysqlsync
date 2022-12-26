package com.chenshuia.study.mysqlsync.bean;

import lombok.Data;

// 索引的实体类
@Data
public class StatisticsDO {
    private String tableSchema;
    private String tableName;
    private String indexName;
    private int seqInIndex;
    private String columnName;
    private int nonUnique;
    private String indexType;

}
