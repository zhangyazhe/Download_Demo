package com.example.services;

import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

import com.example.entities.FileInfo;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

import static com.example.services.DownloadService.DOWNLOAD_PATH;
import static com.example.services.DownloadService.MSG_INIT;

public class DownloadService extends Service {

    public static final String DOWNLOAD_PATH=
            Environment.getExternalStorageDirectory().getAbsolutePath()+
                    "/downloads/";
    public static final String ACTION_START="ACTION_START";
    public static final String ACTION_UPDATE="ACTION_UPDATE";
    public static final String ACTION_STOP="ACTION_STOP";
    public static final int MSG_INIT=0;
    private DownloadTask mTask=null;

    public DownloadService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //获得Activity传来的参数
        if(ACTION_START.equals(intent.getAction())){
            FileInfo fileInfo=(FileInfo)intent.getSerializableExtra("fileinfo");
            //启动初始化线程
            new InitThread(fileInfo).start();
        }else if(ACTION_STOP.equals(intent.getAction())){
            FileInfo fileInfo=(FileInfo)intent.getSerializableExtra("fileinfo");
            if(mTask!=null){
                mTask.isPause=true;
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    Handler mHandler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch(msg.what){
                case MSG_INIT:
                    FileInfo fileInfo=(FileInfo)msg.obj;
                    //启动下载任务
                    mTask=new DownloadTask(DownloadService.this,fileInfo);
                    mTask.download();
                    break;
            }
        }
    };
}

/**
 * 进行网络操作必须在子线程中进行
 * 进行初始化子线程的操作
 */
    class InitThread extends Thread{
        private FileInfo mFileInfo=null;

    public InitThread(FileInfo mFileInfo) {
        this.mFileInfo = mFileInfo;
    }

    @Override
    public void run() {
        HttpURLConnection conn=null;
        RandomAccessFile raf=null;
        try{
            //连接网络文件
            URL url=new URL(mFileInfo.getUrl());
            conn=(HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(3000);
            conn.setRequestMethod("GET");
            int length=-1;
            if(conn.getResponseCode()==200){
                //获得文件长度
                length=conn.getContentLength();
            }
            if(length<=0){
                return;
            }
            //在本地创建文件
            File dir=new File(DOWNLOAD_PATH);
            if(!dir.exists()){
                dir.mkdir();
            }
            File file=new File(dir,mFileInfo.getFileName());
            raf=new RandomAccessFile(file,"rwd");//创建随机进入文件对象，实现断点续传功能
            //设置文件长度
            raf.setLength(length);
            mFileInfo.setLength(length);
            mHandler.obtainMessage(MSG_INIT,mFileInfo).sendToTarget();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            try {
                conn.disconnect();
                raf.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
