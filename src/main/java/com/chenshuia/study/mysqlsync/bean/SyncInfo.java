package com.chenshuia.study.mysqlsync.bean;

import lombok.Data;

import java.util.Objects;

@Data
public class SyncInfo {
    /*
    进行同步的信息。2个数据库的链接方式，同步的库和表
     */

    private ConnectInfo src;
    private ConnectInfo dst;

    public void verify(){
        // 判断属性src与dst 是否为空
        Objects.requireNonNull(src);
        Objects.requireNonNull(dst);
        // 判断对象src的属性是否为空
        src.verify();
        dst.verify();
    }
}
