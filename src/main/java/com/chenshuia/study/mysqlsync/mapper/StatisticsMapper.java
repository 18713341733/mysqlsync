package com.chenshuia.study.mysqlsync.mapper;

import com.chenshuia.study.mysqlsync.bean.StatisticsDO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface StatisticsMapper extends BaseMapper{

    @Select("select TABLE_SCHEMA,TABLE_NAME,INDEX_NAME,SEQ_IN_INDEX,COLUMN_NAME,NON_UNIQUE,INDEX_TYPE from information_schema.statistics " +
            "where table_schema = #{dbName} and table_name = #{tableName}")
    List<StatisticsDO> findByTable(@Param("dbName") String dbName, @Param("tableName") String tableName);

}
