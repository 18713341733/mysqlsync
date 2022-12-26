package com.chenshuia.study.mysqlsync.bean;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(exclude = {"isAdd"})
public class ColumnDO {

    private String tableSchema;
    private String tableName;
    private String columnName;
    private String columnType;
    private String columnComment;
    private String dataType;
    private String columnDefault;
    private String isNullable;

    // 自己添加的属性，这个列是否为新增（还是修改）
    private boolean isAdd;

    /**
     * 注意，isAdd 这个属性是我们自己添加的，不是列自带的属性
     * 所以我们判断两个列的实体类时，不应该用isAdd去判断相等
     * 判断相等，用的是equals和hashcode。所以生成hashcode时，应该去除isAdd这个属性
     */




}
