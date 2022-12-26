package com.chenshuia.study.mysqlsync.mapper;

import com.chenshuia.study.mysqlsync.bean.TableDO;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface TableMapper extends BaseMapper{

    @Select("select TABLE_SCHEMA,TABLE_NAME,ENGINE from information_schema.tables where TABLE_SCHEMA= #{dbName}")
    List<TableDO> findByDBName(String dbName);


}
