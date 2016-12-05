package com.caihaozhong.importer;

import android.os.Handler;
import android.os.Message;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by CaiHaozhong on 2016/11/29.
 */

public abstract class AsyncObjectImporter {
    private List<ImporterCallBack> mImporterCallBackList;
    private Handler mHandler;

    private static final int MSG_BEGIN = 1;
    private static final int MSG_END = 2;

    public AsyncObjectImporter(){
        mImporterCallBackList = new ArrayList<ImporterCallBack>();
        mHandler = new Handler(){
            public void handleMessage(Message msg){
                if(msg.what == MSG_BEGIN){
                    onBeginImport();
                }
                else if(msg.what == MSG_END){
                    onEndImport();
                }
            }
        };
    }
    public void addImporterCallBack(ImporterCallBack callBack){
        if(mImporterCallBackList != null)
            mImporterCallBackList.add(callBack);
    }

    protected void onBeginImport(){
        if(mImporterCallBackList != null){
            for(ImporterCallBack callBack : mImporterCallBackList){
                callBack.onImportBegin();
            }
        }
    }

    protected void onEndImport(){
        if(mImporterCallBackList != null){
            for(ImporterCallBack callBack : mImporterCallBackList){
                callBack.onImportEnd();
            }
        }
    }

    public void start(){
        mHandler.sendEmptyMessage(MSG_BEGIN);
        new Thread(new Runnable() {
            @Override
            public void run() {
                importData();
                mHandler.sendEmptyMessage(MSG_END);
            }
        }).start();
    }

    /** 真正执行导入数据的函数 **/
    protected abstract void importData();

//    protected abstract void prepareImport();
}
