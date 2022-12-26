package com.chenshuia.study.mysqlsync.bean;

import com.chenshuia.study.mysqlsync.util.VerifyUtil;
import lombok.Data;

@Data
public class DBSyncInfo extends SyncInfo{
    private String dbName;

    @Override
    public void verify() {
        super.verify();
        VerifyUtil.verifyString(dbName,"dbName is not null");
    }

}
