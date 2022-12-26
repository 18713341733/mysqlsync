package com.chenshuia.study.mysqlsync.util;

import org.apache.logging.log4j.util.Strings;

public class VerifyUtil {
    public static void verifyString(String data,String msg){
        if (Strings.isEmpty(data)){
            throw new IllegalArgumentException(msg);
        }
    }

    public static void verifyString(String... datas){
        for (String data:datas){
            verifyString(data,"data is not null");
        }
    }


}
