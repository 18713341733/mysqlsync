package com.chenshuia.study.mysqlsync.dao;

import com.chenshuia.study.mysqlsync.bean.ConnectInfo;
import com.chenshuia.study.mysqlsync.common.LocalSqlSessionFactory;
import com.chenshuia.study.mysqlsync.mapper.BaseMapper;
import org.apache.ibatis.session.SqlSession;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.function.Function;

public class DaoFacade {
    public static <R,M extends BaseMapper> R ofMapper(ConnectInfo connectInfo, Class<M> clazz, Function<M,R> function){
        try(SqlSession sqlSession = LocalSqlSessionFactory.of().getSqlSession(connectInfo)){
            M mapper = sqlSession.getMapper(clazz);
            R result = function.apply(mapper);
            return result;
        } catch (Exception e){
            e.printStackTrace();
            throw new IllegalStateException("mapper failed");
        }
    }


    public static void executeSql(ConnectInfo connectInfo,String sql){
        try (SqlSession sqlSession = LocalSqlSessionFactory.of().getSqlSession(connectInfo);
            Connection connection = sqlSession.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);){
            ps.executeUpdate();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void executeSql(ConnectInfo connectInfo, List<String> sqls){
        // 方式一
//        try (SqlSession sqlSession = LocalSqlSessionFactory.of().getSqlSession(connectInfo);
//             Connection connection = sqlSession.getConnection()){
//            sqls.stream().forEach(sql ->{
//                try(PreparedStatement ps = connection.prepareStatement(sql)) {
//                    ps.executeUpdate();
//                } catch (Exception e){
//                    e.printStackTrace();
//                }
//            });
//        } catch (Exception e){
//            e.printStackTrace();
//        }

        // 方式二
        try (SqlSession sqlSession = LocalSqlSessionFactory.of().getSqlSession(connectInfo);
             Connection connection = sqlSession.getConnection();
             Statement st = connection.createStatement()){
            sqls.stream().forEach(sql ->{
                try {
                    st.executeUpdate(sql);
                } catch (Exception e){
                    e.printStackTrace();
                }
            });
        } catch (Exception e){
            e.printStackTrace();
        }

    }

    // 获取创建表/创建库的sql
    public static String getInfo(ConnectInfo connectInfo,String sql,String columnName){
        try(SqlSession sqlSession = LocalSqlSessionFactory.of().getSqlSession(connectInfo);
            Connection connection = sqlSession.getConnection();
            Statement st = connection.createStatement();
            ResultSet rs = st.executeQuery(sql);){
            if (rs.next()){
                return rs.getString(columnName);
            }
            throw new IllegalArgumentException();

        } catch (Exception e){
            e.printStackTrace();
            throw new IllegalArgumentException();
        }

    }

    // 获取创建表的sql
    public static String getTableSql(ConnectInfo connectInfo,String dbName,String tableName){
        String sql = String.format("show create table %s.%s",dbName,tableName);
        String columnName = "Create Table";
        return getInfo(connectInfo,sql,columnName);

    }

    // 获取创建库的sql
    public static String getDBSql(ConnectInfo connectInfo,String dbName){
        String sql = String.format("show create database %s",dbName);
        String columnName = "Create Database";
        return getInfo(connectInfo,sql,columnName);

    }

}
