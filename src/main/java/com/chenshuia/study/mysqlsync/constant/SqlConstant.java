package com.chenshuia.study.mysqlsync.constant;

import com.google.common.collect.Lists;

import java.util.List;

public interface SqlConstant {
    // 修改列的sql模版
    String MODIFY_COLUMN = "ALTER TABLE %s.%s MODIFY COLUMN %s %s %s %s %s";
    // 新增列的sql模版
    String ADD_COLUMN = "ALTER TABLE %s.%s ADD COLUMN %s %s %s %s %s";

    // 添加索引的模版（区分唯一/不唯一索引）
    // ALTER TABLE %s.%s ADD %s INDEX %s (列名)
    // ALTER TABLE %s.%s ADD PRIMARY KEY (id);
    // ALTER TABLE %s.%s ADD FULLTEXT xxx(NAME);
    String ADD_INDEX = "ALTER TABLE %s.%s ADD %s %s (%s)";

    // 索引没有修改，只能先删除，再添加
    String DROP_INDEX = "ALTER TABLE %s.%s DROP INDEX %s";

    // mysql系统自带库，是不需要进行同步的。
    List<String> MYSQL_SYS_DBS = Lists.newArrayList("information_schema","performance_schema","sys","mysql","test");

}
