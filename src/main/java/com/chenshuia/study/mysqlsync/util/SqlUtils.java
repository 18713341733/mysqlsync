package com.chenshuia.study.mysqlsync.util;

import com.chenshuia.study.mysqlsync.bean.StatisticsDTO;

import java.util.Objects;

public class SqlUtils {
    // 列是否为空的处理
    public static String nullableSet(String nullable){
        if ("NO".equals(nullable)){
            return "not null";
        }
        return "null";
    }

    // 列，默认值的处理，（可能为空，null，0）
    public static String defaultSet(String defaultValue){
        if (Objects.isNull(defaultValue)){
            return "";
        }
        return "DEFAULT '"+defaultValue+"'";
    }

    // 列，备注的处理
    public static String commentSet(String comment){
        if (Objects.isNull(comment)){
            return "";
        }
        return "COMMENT '"+comment+"'";
    }

    //
    public static String indexTypeSet(StatisticsDTO dto){
        // 1、主键 index_name 为 PRIMARY
        // 2、唯一索引 unique = 0 并且 index_name != PRIMARY
        // 3、普通索引 unique = 1 并且 index_type = BTREE
        // 4、全文索引 unique = 1 并且 index_type = FULLTEXT

        if(("PRIMARY").equals(dto.getIndexName())){
            return "PRIMARY KEY";
        }
        if(dto.getNonUnique() == 0){
            return "UNIQUE";
        }
        if("BTREE".equals(dto.getIndexType())){
            return "INDEX";
        } else {
            return "FULLTEXT";
        }

    }
}
