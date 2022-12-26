package com.chenshuia.study.mysqlsync.service.impl;

import com.chenshuia.study.mysqlsync.bean.*;
import com.chenshuia.study.mysqlsync.common.LocalSqlSessionFactory;
import com.chenshuia.study.mysqlsync.constant.IndexType;
import com.chenshuia.study.mysqlsync.constant.SqlConstant;
import com.chenshuia.study.mysqlsync.dao.DaoFacade;
import com.chenshuia.study.mysqlsync.mapper.ColumnMapper;
import com.chenshuia.study.mysqlsync.mapper.SchemaMapper;
import com.chenshuia.study.mysqlsync.mapper.StatisticsMapper;
import com.chenshuia.study.mysqlsync.mapper.TableMapper;
import com.chenshuia.study.mysqlsync.service.SyncService;
import com.chenshuia.study.mysqlsync.util.SqlUtils;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.ibatis.session.SqlSession;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SyncServiceImpl implements SyncService {

    // 同步实例
    @Override
    public void syncInstance(SyncInfo syncInfo) {


        ConnectInfo srcInfo = syncInfo.getSrc();
        ConnectInfo dstInfo = syncInfo.getDst();
        // 获取所有库
        List<SchemaDO> srcDBs = DaoFacade.ofMapper(srcInfo, SchemaMapper.class, m -> m.findAll());
        List<SchemaDO> dstDBs = DaoFacade.ofMapper(dstInfo, SchemaMapper.class, m -> m.findAll());
        // diff (新增）
        // src 有， dst没有的库，属于新增，我们需要先新建库，然后再去同步表
        // src 有，dst 也有的库，我们直接去同步库里的表
        // addDBs 1、先去创建没有的库
        // 2、然后对所有的库srcDbs都进行同步
        Set<SchemaDO> addDbs = Sets.difference(new HashSet<>(srcDBs), new HashSet<>(dstDBs)).immutableCopy();
        // 新建库
        addDbs.stream()
                .filter(db -> !SqlConstant.MYSQL_SYS_DBS.contains(db.getSchemaName()))
                .forEach(db ->{
                    String sql = DaoFacade.getDBSql(srcInfo,db.getSchemaName());
                    DaoFacade.executeSql(dstInfo,sql);
                });
        // 同步所有的库
        srcDBs.stream()
                .filter(db -> !SqlConstant.MYSQL_SYS_DBS.contains(db.getSchemaName()))
                .forEach(db->{
                    DBSyncInfo dbSyncInfo = new DBSyncInfo();
                    dbSyncInfo.setSrc(srcInfo);
                    dbSyncInfo.setDst(dstInfo);
                    dbSyncInfo.setDbName(db.getSchemaName());
                    // 进行同步库
                    syncDB(dbSyncInfo);
                });


    }


    // 同步库
    @Override
    public void syncDB(DBSyncInfo dbSyncInfo) {
        // 库的连接信息
        ConnectInfo srcInfo = dbSyncInfo.getSrc();
        ConnectInfo dstInfo = dbSyncInfo.getDst();
        String dbName = dbSyncInfo.getDbName();
        // 同步库，就是同步这个库下所有的表

        // 查询库下所有的表
        List<TableDO> srcTables = DaoFacade.ofMapper(srcInfo, TableMapper.class,m-> m.findByDBName(dbName));
        List<TableDO> dstTables = DaoFacade.ofMapper(srcInfo, TableMapper.class,m-> m.findByDBName(dbName));

        // 新增的表（求差集）
        Set<TableDO> addTables = Sets.difference(new HashSet<>(srcTables), new HashSet<>(dstTables)).immutableCopy();
        createTable(srcInfo,dstInfo,addTables);
        // 非新增，需要进行同步的表（求交集）
        Set<TableDO> syncTables = Sets.intersection(new HashSet<>(srcTables), new HashSet<>(dstTables)).immutableCopy();




        // 具体对表进行同步
        syncTables.stream().forEach(tableDO -> {
            TableSyncInfo tableSyncInfo = new TableSyncInfo();
            tableSyncInfo.setSrc(srcInfo);
            tableSyncInfo.setDst(dstInfo);
            tableSyncInfo.setDbName(dbName);
            tableSyncInfo.setTableName(tableDO.getTableName());
            syncTable(tableSyncInfo);
        });




    }

    // 创建新表
    private void createTable(ConnectInfo srcInfo, ConnectInfo dstInfo, Set<TableDO> addTables) {
        addTables.stream().forEach(tableDO -> {
            String tableSql = DaoFacade.getTableSql(srcInfo, tableDO.getTableSchema(), tableDO.getTableName());
            String useDBSql = "use "+ tableDO.getTableSchema();
            DaoFacade.executeSql(dstInfo, Lists.newArrayList(useDBSql,tableSql));
        });
    }

    @Override
    public void syncTable(TableSyncInfo tableSyncInfo) {
        // 数据库链接信息
        ConnectInfo srcInfo = tableSyncInfo.getSrc();
        ConnectInfo dstInfo = tableSyncInfo.getDst();
        // 数据库名称
        String dbName = tableSyncInfo.getDbName();
        // 表的名称
        String tableName = tableSyncInfo.getTableName();
        // 同步列
        syncColumn(srcInfo,dstInfo,dbName,tableName);
        // 同步索引
        syncStatistics(srcInfo,dstInfo,dbName,tableName);

    }

    // 同步索引
    private void syncStatistics(ConnectInfo srcInfo, ConnectInfo dstInfo, String dbName, String tableName) {
        // 1、获取，在src原 数据库实例下库的表的结构（字段+索引）
        List<StatisticsDO> srcStatisticDos = DaoFacade.ofMapper(srcInfo, StatisticsMapper.class, m -> m.findByTable(dbName, tableName));
        // 2、获取，在dst目标 数据库实例下库的表的结构（字段+索引）
        List<StatisticsDO> dstStatisticDos = DaoFacade.ofMapper(dstInfo, StatisticsMapper.class, m -> m.findByTable(dbName, tableName));
        // 3、diff 差异
        Map<Boolean, List<StatisticsDTO>> diffMap = diffStatistics(srcStatisticDos, dstStatisticDos);
        // 4、基于差异，生成sql
        List<String> addSqls = generateAddIndex(diffMap.get(true));
        // 删除的索引
        List<String> modifyDropSqls = generateDropIndex(diffMap.get(false));
        // 再增加
        List<String> modifyCreateSqls = generateAddIndex(diffMap.get(false));
        // 5、执行sql
        DaoFacade.executeSql(dstInfo,addSqls);
        DaoFacade.executeSql(dstInfo,modifyDropSqls);
        DaoFacade.executeSql(dstInfo,modifyCreateSqls);



    }

    // 生成新增索引的sql
    private List<String> generateAddIndex(List<StatisticsDTO> statisticsDTOS){
        // "ALTER TABLE %s.%s ADD %s INDEX %s (%s)";
        List<String> collect = statisticsDTOS.stream()
                .map(dto -> String.format(SqlConstant.ADD_INDEX,
                        dto.getTableSchema(),
                        dto.getTableName(),
                        SqlUtils.indexTypeSet(dto),
                        dto.getIndexName().equals(IndexType.PRIMARY)?"":dto.getIndexName(),
                        // dto.getColumns().stream().collect(Collectors.joining(","))
                        Joiner.on(",").join(dto.getColumns())
                ))
                .collect(Collectors.toList());

        return collect;

    }

    // 删除索引
    private List<String> generateDropIndex(List<StatisticsDTO> statisticsDTOS){
        List<String> collect = statisticsDTOS.stream()
                .map(statisticsDTO -> String.format(SqlConstant.DROP_INDEX,
                        statisticsDTO.getTableSchema(),
                        statisticsDTO.getTableName(),
                        statisticsDTO.getIndexName()))
                .collect(Collectors.toList());
        return collect;

    }

    // diff 索引的差异
    private Map<Boolean, List<StatisticsDTO>> diffStatistics(List<StatisticsDO> srcStatisticDos, List<StatisticsDO> dstStatisticDos){
        // 将do 转成dto
        List<StatisticsDTO> srcDtos = fromStatisticsDOToDTO(srcStatisticDos);
        List<StatisticsDTO> dstDtos = fromStatisticsDOToDTO(dstStatisticDos);
        // diff，diffStatisticsDTOs 是包含新增与修改的，所有的DTO实例
        Set<StatisticsDTO> diffStatisticsDTOs = Sets.difference(new HashSet<>(srcDtos), new HashSet<>(dstDtos)).immutableCopy();
        // 区分哪些索引是新增的，哪些索引是变动的
        // 将src 源集合中的，IndexName所有索引名称，组成一个集合
        Set<String> srcNames = srcDtos.stream().map(sdto -> sdto.getIndexName()).collect(Collectors.toSet());
        // 将dst 源集合中的，IndexName所有索引名称，组成一个集合
        Set<String> dstNames = dstDtos.stream().map(sdto -> sdto.getIndexName()).collect(Collectors.toSet());
        // 在diffNames  中的实例，都是添加的。
        Set<String> diffNames = Sets.difference(srcNames, dstNames).immutableCopy();

        Map<Boolean, List<StatisticsDTO>> collect = diffStatisticsDTOs.stream()
                // partitioning 分区。只能分两个区，true与false
                .collect(Collectors.partitioningBy(statisticsDTO -> diffNames.contains(statisticsDTO)));
        // Map，为true则是所有新增的。为false，是所有修改的。
        return collect;


    }

    // 将StatisticsDo 转成 StatisticsDTO
    private List<StatisticsDTO> fromStatisticsDOToDTO(List<StatisticsDO> statisticDos){
        // 按照索引的名称进行分组 根据IndexName 对List<StatisticsDo> 分组。因为联合索引的原因。
        // 联合索引，索引名称一样，但是对应的列和SeqInIndex的值不一样
        Map<String, List<StatisticsDO>> dos = statisticDos.stream().collect(Collectors.groupingBy(s -> s.getIndexName()));

        List<StatisticsDTO> collect = dos.entrySet().stream()
                .map(entry -> {
                    //
                    StatisticsDO sdo = entry.getValue().get(0);
                    List<String> columns = entry.getValue().stream()
                            .sorted((x,y)->x.getSeqInIndex() - y.getSeqInIndex())
                            .map(s -> s.getColumnName()).collect(Collectors.toList());
                    return StatisticsDTO.builder()
                            .tableSchema(sdo.getTableSchema())
                            .tableName(sdo.getTableName())
                            .indexName(sdo.getIndexName())
                            .nonUnique(sdo.getNonUnique())
                            .indexType(sdo.getIndexType())
                            .columns(columns)
                            .build();
                })
                .collect(Collectors.toList());
        return collect;

    }

    // 同步列
    private void syncColumn(ConnectInfo srcInfo,ConnectInfo dstInfo,String dbName,String tableName){
        // 1、获取，在src原 数据库实例下库的表的结构（字段+索引）
        SqlSession sqlSession = LocalSqlSessionFactory.of().getSqlSession(srcInfo);
        ColumnMapper mapper = sqlSession.getMapper(ColumnMapper.class);
        List<ColumnDO> srcColumns = mapper.findByTable(dbName, tableName);
        // 2、获取，在dst目标 数据库实例下库的表的结构（字段+索引）
        List<ColumnDO> dstColumnDOS = DaoFacade.ofMapper(dstInfo, ColumnMapper.class, m -> m.findByTable(dbName, tableName));
        // 1与2 实现的功能是一摸一样的，只不过2这里又封装了一下。

        // 3、diff 差异
        List<ColumnDO> columnDOS = diffColumn(srcColumns, dstColumnDOS);
        // 4、基于差异，生成sql
        List<String> sqls = generateSql(columnDOS);
        // 5、执行sql
        DaoFacade.executeSql(dstInfo,sqls);


    }

    // 做差集，使用第三方包guava
    private List<ColumnDO> diffColumn(List<ColumnDO> srcColumns , List<ColumnDO> dstColumnDOS){
        // 1、区分列的是实体类，是新增的还是修改的
        // 如何判断是新增的：列的名字不一致，就是新增的

        // 将List转成Set集合,然后求差值
        Set<ColumnDO> diffColumns = Sets.difference(new HashSet<>(srcColumns), new HashSet<>(dstColumnDOS)).immutableCopy();

        // 将src 列的集合，每个实体的类的名字组合成一个集合
        Set<String> srcNames = srcColumns.stream().map(columnDO -> columnDO.getColumnName()).collect(Collectors.toSet());
        // 将src 列的集合，每个实体的类的名字组合成一个集合
        Set<String> dstNames = dstColumnDOS.stream().map(columnDO -> columnDO.getColumnName()).collect(Collectors.toSet());
        // 将src 列名字的集合与dst列名字的集合求差值.判断哪些列是新增的
        Set<String> addNames = Sets.difference(new HashSet<>(srcNames),new HashSet<>(dstNames)).immutableCopy();
        // 给Column 设置 isAdd。
        List<ColumnDO> collects = diffColumns.stream()
                .peek(columnDO -> {
                    if (addNames.contains(columnDO.getColumnName())){
                        columnDO.setAdd(true);
                    }
                })
                .collect(Collectors.toList());


        return collects;

    }

    private List<String> generateSql(List<ColumnDO> columnDOS){
        // ALTER TABLE Student MODIFY COLUMN id VARCHAR(32) NOT NULL DEFAULT "000" COMMENT '备注';
        // ALTER TABLE %s.%s MODIFY COLUMN %s %s %s %s %s;
        // 将ColumnDo 的list 转成 String 语句的list，转换型，使用map
        List<String> sqls = columnDOS.stream()
                .map(columnDO -> {
//                    String sqlModel;
//                    if (columnDo.isAdd()){
//                        sqlModel = SqlModel.ADD_COLUMN;
//                    } else {
//                        sqlModel = SqlModel.MODIFY_COLUMN;
//                    }
                    String sql = String.format(columnDO.isAdd()? SqlConstant.ADD_COLUMN: SqlConstant.MODIFY_COLUMN,
                            columnDO.getTableSchema(), //库名
                            columnDO.getTableName(), // 表名
                            columnDO.getColumnName(),// 列名
                            columnDO.getColumnType(),// 列的类型
                            SqlUtils.nullableSet(columnDO.getIsNullable()),// 列是否为空
                            SqlUtils.defaultSet(columnDO.getColumnDefault()),// 列的默认值设置
                            SqlUtils.commentSet(columnDO.getColumnComment())// 设置列的备注
                    );
                    return sql;
                })
                .collect(Collectors.toList());
        return sqls;
    }


}
