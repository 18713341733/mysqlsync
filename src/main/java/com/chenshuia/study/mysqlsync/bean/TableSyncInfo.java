package com.chenshuia.study.mysqlsync.bean;

import com.chenshuia.study.mysqlsync.util.VerifyUtil;
import lombok.Data;

@Data
public class TableSyncInfo extends DBSyncInfo{
    private String tableName;

    @Override
    public void verify() {
        super.verify();
        VerifyUtil.verifyString(tableName,"tableName is not null");
    }

}
