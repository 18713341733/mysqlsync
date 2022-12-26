package com.chenshuia.study.mysqlsync.controller;

import com.chenshuia.study.mysqlsync.bean.ResultMsg;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PingController {

    @RequestMapping("/ping")
    public ResultMsg ping(){
        return ResultMsg.success();
    }
}
