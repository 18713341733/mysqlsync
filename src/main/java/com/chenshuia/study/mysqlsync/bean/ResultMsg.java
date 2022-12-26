package com.chenshuia.study.mysqlsync.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.omg.CORBA.PUBLIC_MEMBER;

@Data
@AllArgsConstructor
public class ResultMsg<T> {
    private String code;
    private String msg;
    private T data;

    public static final String SUCCESS_CODE = "200";
    public static final String FAILED_CODE = "999";
    public static final String SUCCESS_MSG = "success";
    public static final String FAILED_MSG = "failed";

    public ResultMsg(String code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public static <T> ResultMsg<T> success(T t){
        return new ResultMsg<>(SUCCESS_CODE,SUCCESS_MSG,t);
    }

    public static ResultMsg success(){
        return new ResultMsg<>(SUCCESS_CODE,SUCCESS_MSG);
    }

    public static ResultMsg fail(){
        return new ResultMsg<>(FAILED_CODE,FAILED_MSG);
    }

    public static ResultMsg fail(String code,String msg){
        return new ResultMsg<>(code,msg);
    }
}
