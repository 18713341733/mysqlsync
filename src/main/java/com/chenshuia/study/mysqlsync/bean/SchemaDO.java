package com.chenshuia.study.mysqlsync.bean;

import lombok.Data;

@Data
public class SchemaDO {
    // 数据库名
    private String schemaName;
    // 默认字符集
    private String defaultCharacterSetName;

}
