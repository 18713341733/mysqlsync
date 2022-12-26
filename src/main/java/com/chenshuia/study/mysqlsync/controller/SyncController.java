package com.chenshuia.study.mysqlsync.controller;

import com.chenshuia.study.mysqlsync.bean.DBSyncInfo;
import com.chenshuia.study.mysqlsync.bean.ResultMsg;
import com.chenshuia.study.mysqlsync.bean.SyncInfo;
import com.chenshuia.study.mysqlsync.bean.TableSyncInfo;
import com.chenshuia.study.mysqlsync.service.SyncService;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;

@RequestMapping("/sync")
@RestController
public class SyncController {

    @Autowired
    private SyncService syncService;

    // 同步实例
    @RequestMapping(value = "/instance",method = RequestMethod.POST)
    public ResultMsg sync(@RequestBody SyncInfo syncInfo){
        // 参数校验
        try {
            syncInfo.verify();
            syncService.syncInstance(syncInfo);
            return ResultMsg.success();
        } catch (Exception e){
            e.printStackTrace();
            return ResultMsg.fail(ResultMsg.FAILED_CODE,e.getMessage());
        }

    }

    // 同步库
    @RequestMapping(value = "/db",method = RequestMethod.POST)
    public ResultMsg sync(@RequestBody DBSyncInfo dbSyncInfo){
        try {
            dbSyncInfo.verify();
            syncService.syncDB(dbSyncInfo);
            return ResultMsg.success();

        } catch (Exception e){
            e.printStackTrace();
            return ResultMsg.fail(ResultMsg.FAILED_CODE,e.getMessage());
        }

    }

    // 同步表
    @RequestMapping(value = "/table",method = RequestMethod.POST)
    public ResultMsg sync(@RequestBody TableSyncInfo tableSyncInfo){
        try {
            // 校验参数
            tableSyncInfo.verify();
            // 调用业务，处理数据
            syncService.syncTable(tableSyncInfo);
            return ResultMsg.success();
        } catch (Exception e){
            e.printStackTrace();
            return ResultMsg.fail(ResultMsg.FAILED_CODE,e.getMessage());
        }

    }

}
