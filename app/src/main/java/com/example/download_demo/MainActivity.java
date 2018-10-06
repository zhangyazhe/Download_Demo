package com.example.download_demo;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.entities.FileInfo;
import com.example.services.DownloadService;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity {
    private ListView mLvFile=null;
    private List<FileInfo> mFileList=null;
    private FileListAdapter mAdapter=null;
    FileInfo fileInfo0=null;
    FileInfo fileInfo1=null;
    FileInfo fileInfo2=null;
    FileInfo fileInfo3=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mLvFile=(ListView) findViewById(R.id.lv_file);
        //创建文件集合
        mFileList=new ArrayList<FileInfo>();
        //创建文件信息对象,下载炉石传说
        fileInfo0 =new FileInfo(0,"http://client02.pdl.wow.battlenet.com.cn/hs-pod/Tools/CN/hsbuild/Hearthstone_CN_Production.apk","Hearthstone_CN_Production.apk", 0,0);
        //下载biibili
        fileInfo1 =new FileInfo(1,"https://dl.hdslb.com/mobile/latest/iBiliPlayer-bili.apk","iBiliPlayer-bili.apk", 0,0);
        //下载微信阅读
        fileInfo2 =new FileInfo(2,"http://dldir1.qq.com/foxmail/weread_android_3.0.1.96_81.apk","weread_android_3.0.1.96_81.apk", 0,0);
        //下载taptap
        fileInfo3 =new FileInfo(3,"https://c.tapimg.com/pub2/201805/4151e80502df24107d7097bc30e3931d.apk?_upd=com.taptap_1.9.12.apk","taptap.apk", 0,0);
        mFileList.add(fileInfo0);
        mFileList.add(fileInfo1);
        mFileList.add(fileInfo2);
        mFileList.add(fileInfo3);
        //创建适配器
        mAdapter =new FileListAdapter(this,mFileList);
        //设置ListView
        mLvFile.setAdapter(mAdapter);

        //注册广播接收器
        IntentFilter filter =new IntentFilter();
        filter.addAction(DownloadService.ACTION_UPDATE);
        filter.addAction(DownloadService.ACTION_FINISHED);
        registerReceiver(mReceiver,filter);
    }



    protected void onDestroy(){
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    BroadcastReceiver mReceiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(DownloadService.ACTION_UPDATE.equals(intent.getAction())){
                int finished=intent.getIntExtra("finished",0);
                int id=intent.getIntExtra("id",0);
                mAdapter.updateProgress(id,finished);
            }else if(DownloadService.ACTION_FINISHED.equals(intent.getAction())){
                FileInfo fileInfo =(FileInfo)intent.getSerializableExtra("fileInfo");
                //更新进度为0并弹出提示
                mAdapter.updateProgress(fileInfo.getId(),0);
                Toast.makeText(MainActivity.this,mFileList.get(fileInfo.getId()).getFileName()+"下载完毕",Toast.LENGTH_SHORT).show();
            }
        }
    };
}
