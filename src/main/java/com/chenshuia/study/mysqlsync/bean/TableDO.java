package com.chenshuia.study.mysqlsync.bean;

import lombok.Data;

@Data
public class TableDO {
    private String tableSchema;
    private String tableName;
    // engine 引擎
    private String engine;
}
