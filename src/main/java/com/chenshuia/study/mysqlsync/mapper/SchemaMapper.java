package com.chenshuia.study.mysqlsync.mapper;

import com.chenshuia.study.mysqlsync.bean.SchemaDO;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface SchemaMapper extends BaseMapper{
    @Select("select SCHEMA_NAME,DEFAULT_CHARACTER_SET_NAME from information_schema.SCHEMATA")
    List<SchemaDO> findAll();

    @Select("select SCHEMA_NAME,DEFAULT_CHARACTER_SET_NAME from information_schema.SCHEMATA where schema_name = #{dbName}")
    SchemaDO findByDBName(String dbName);
}
