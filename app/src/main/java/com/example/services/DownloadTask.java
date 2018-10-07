package com.example.services;

import android.content.Context;
import android.content.Intent;

import com.example.db.ThreadDAO;
import com.example.db.ThreadDAOImpl;
import com.example.entities.FileInfo;
import com.example.entities.ThreadInfo;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class DownloadTask {
    private Context mContext=null;
    private FileInfo mFileInfo=null;
    private ThreadDAO mDao=null;
    private int mFinished=0;
    public boolean isPause=false;
    private int mThreadCount=1;//线程数量
    private List<DownloadThread> mThreadList=null;//线程的集合，方便对线程进行管理

    public DownloadTask(Context mContext, FileInfo mFileInfo,int mThreadCount) {
        this.mContext = mContext;
        this.mFileInfo = mFileInfo;
        this.mThreadCount=mThreadCount;
        mDao=new ThreadDAOImpl(mContext);
    }

    public void download(){
        //读取数据库的线程信息
        List<ThreadInfo> threadInfos=mDao.getThreads(mFileInfo.getUrl());
        if(threadInfos.size()==0){
            //获得每个线程的下载长度
            int length=mFileInfo.getLength()/mThreadCount;
            for(int i=0;i<mThreadCount;i++){
                ThreadInfo threadInfo=new ThreadInfo(i,mFileInfo.getUrl(),length*i,(i+1)*length-1,0);
                if(i==mThreadCount-1){//最后一个线程可能存在除不尽的情况
                    threadInfo.setEnd(mFileInfo.getLength());
                }
                //将线程添加到线程信息集合中
                threadInfos.add(threadInfo);
                //向数据库插入线程信息
                    mDao.insertThread(threadInfo);

            }
        }


        mThreadList=new ArrayList<DownloadThread>();
        //启动多个线程进行下载
        for(ThreadInfo info:threadInfos){
            DownloadThread thread=new DownloadThread(info);
            thread.start();
            //添加线程到集合中
            mThreadList.add(thread);
        }
    }

    /**
     * 判断是否所有线程都执行完毕
     */
    private synchronized void checkAllThreadsFinished(){
        boolean allFinished=true;
        //遍历线程集合，判断线程是否都执行完毕
        for(DownloadThread thread:mThreadList){
            if(!thread.isFinished){
                allFinished=false;
                break;
            }
        }
        if(allFinished){
            //删除线程信息
            mDao.deleteThread(mFileInfo.getUrl());
            //发送广播，通知UI下载任务结束
            Intent intent=new Intent(DownloadService.ACTION_FINISHED);
            intent.putExtra("fileInfo",mFileInfo);
            intent.putExtra("id",mFileInfo.getId());
            mContext.sendBroadcast(intent);
        }
    }

    class DownloadThread extends Thread{
        private ThreadInfo mThreadInfo=null;
        public boolean isFinished=false;//线程是否执行完毕

        public DownloadThread(ThreadInfo mTheadInfo) {
            this.mThreadInfo = mTheadInfo;
        }

        public void run(){

            HttpURLConnection conn=null;
            RandomAccessFile raf=null;
            InputStream input=null;
            try {
                URL url=new URL(mThreadInfo.getUrl());
                conn=(HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(3000);
                conn.setRequestMethod("GET");
                //设置下载位置
                int start=mThreadInfo.getStart()+mThreadInfo.getFinished();
                conn.setRequestProperty("Range","bytes="+start+"-"+mThreadInfo.getEnd());
                //设置文件写入位置
                File file=new File(DownloadService.DOWNLOAD_PATH,mFileInfo.getFileName());
                raf=new RandomAccessFile(file,"rwd");
                raf.seek(start);
                Intent intent =new Intent(DownloadService.ACTION_UPDATE);
                mFinished+=mThreadInfo.getFinished();
                //开始下载
                if(conn.getResponseCode()==206){
                    //读取数据
                    input=conn.getInputStream();
                    byte[] buffer=new byte[1024*4];
                    int len=-1;
                    long time =System.currentTimeMillis();
                    while((len=input.read(buffer))!=-1){
                        //写入文件
                        raf.write(buffer,0,len);
                        //把下载进度发送广播给Activity
                        //累加整个文件完成的进度
                        mFinished+=len;
                        //累加每个线程完成的进度
                        mThreadInfo.setFinished(mThreadInfo.getFinished()+len);
                        if(System.currentTimeMillis()-time>1000){
                            time =System.currentTimeMillis();
                            intent.putExtra("finished",mFinished*100/mFileInfo.getLength());
                            mContext.sendBroadcast(intent);
                        }
                        //暂停时保存下载进度
                        if(isPause){
                            mDao.updateThread(mThreadInfo.getUrl(),mThreadInfo.getId(),mThreadInfo.getFinished());
                            return;
                        }
                    }
                }
                //标识线程执行完毕
                isFinished=true;

                //检查下载任务是否都执行完毕
                checkAllThreadsFinished();

            } catch (Exception e) {
                e.printStackTrace();
            }finally {
                try {
                    conn.disconnect();
                    input.close();
                    raf.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


        }
    }
}
