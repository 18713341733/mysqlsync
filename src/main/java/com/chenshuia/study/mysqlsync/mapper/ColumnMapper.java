package com.chenshuia.study.mysqlsync.mapper;

import com.chenshuia.study.mysqlsync.bean.ColumnDO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ColumnMapper extends BaseMapper{

    // 根据表的名称，查询表的列。一个列数，对应一条ColumnDo
    @Select("select table_schema,table_name,column_name,column_type,column_comment,data_type," +
            "column_default,is_nullable from information_schema.columns where table_schema = #{dbName} and table_name =#{tableName}" )
    List<ColumnDO> findByTable(@Param("dbName") String dbName, @Param("tableName") String tableName );



}
