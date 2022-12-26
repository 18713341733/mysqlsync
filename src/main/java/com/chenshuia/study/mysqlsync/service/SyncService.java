package com.chenshuia.study.mysqlsync.service;

import com.chenshuia.study.mysqlsync.bean.DBSyncInfo;
import com.chenshuia.study.mysqlsync.bean.SyncInfo;
import com.chenshuia.study.mysqlsync.bean.TableSyncInfo;

public interface SyncService {
    // 同步实例
    void syncInstance(SyncInfo syncInfo);
    // 同步库
    void syncDB(DBSyncInfo dbSyncInfo);
    // 同步表
    void syncTable(TableSyncInfo tableSyncInfo);
}
