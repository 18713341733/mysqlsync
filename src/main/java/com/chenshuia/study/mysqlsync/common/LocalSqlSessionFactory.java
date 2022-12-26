package com.chenshuia.study.mysqlsync.common;

import com.chenshuia.study.mysqlsync.bean.ConnectInfo;
import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;

public class LocalSqlSessionFactory {


    private LocalSqlSessionFactory(){}
    // 构造器，私有，不能被new

    // ClassHolder属于静态内部类，在加载类Demo03的时候，只会加载内部类ClassHolder，
    // 但是不会把内部类的属性加载出来
    private static class ClassHolder{
        // 这里执行类加载，是jvm来执行类加载，它一定是单例的，不存在线程安全问题
        // 这里不是调用，是类加载，是成员变量
        private static final LocalSqlSessionFactory holder =new LocalSqlSessionFactory();

    }

    public static LocalSqlSessionFactory of(){
        //第一次调用getInstance()的时候赋值
        return ClassHolder.holder;
        }


    public SqlSession getSqlSession(ConnectInfo connectInfo){
        // 这里我们要做的就是通过数据库的配置，对数据库进行连接。拿到sqlSession
        Configuration configuration = new Configuration();
        // 驼峰映射
        configuration.setMapUnderscoreToCamelCase(true);
        // Mapper的包名
        configuration.addMappers("com.chenshuia.study.mysqlsync.mapper");
        //
        Environment environment = new Environment.Builder("development")
                .transactionFactory(new JdbcTransactionFactory())
                .dataSource(LocalDataSourceFactory.of().getPoolDataSource(connectInfo))
                .build();
        configuration.setEnvironment(environment);
        SqlSessionFactory build = new SqlSessionFactoryBuilder().build(configuration);
        SqlSession sqlSession = build.openSession(true);
        return sqlSession;

    }



}
