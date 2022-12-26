package com.chenshuia.study.mysqlsync.bean;

import lombok.Builder;
import lombok.Data;

import java.util.List;

// 索引的实体类
@Data
@Builder
public class StatisticsDTO {
    private String tableSchema;
    private String tableName;
    private String indexName;
    private List<String> columns;
    private int nonUnique;
    private String indexType;


}
