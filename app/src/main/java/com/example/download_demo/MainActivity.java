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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.entities.FileInfo;
import com.example.services.DownloadService;

import java.io.File;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends AppCompatActivity {
    private TextView mTvName=null;
    private ProgressBar mPbProgress=null;
    private Button mBtStart=null;
    private Button mBtStop=null;
    FileInfo fileInfo=null;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode){
            case 1:
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    start();
                }else{
                    Toast.makeText(this,"获取权限失败，下载无法进行",Toast.LENGTH_SHORT).show();
                }
                break;
            default:
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTvName=(TextView) findViewById(R.id.tv_name);
        mPbProgress=(ProgressBar) findViewById(R.id.progress_bar);
        mBtStart=(Button) findViewById(R.id.bt_start);
        mBtStop=(Button)findViewById(R.id.bt_stop);
        mPbProgress.setMax(100);
        //创建文件信息对象,下载炉石传说
        fileInfo =new FileInfo(0,"http://client02.pdl.wow.battlenet.com.cn/hs-pod/Tools/CN/hsbuild/Hearthstone_CN_Production.apk","Hearthstone_CN_Production.apk", 0,0);
        //添加事件监听
        mBtStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //申请运行时权限
                if(ContextCompat.checkSelfPermission(MainActivity.this,WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(MainActivity.this,new String[]{WRITE_EXTERNAL_STORAGE},1);
                }else{
                    start();
                }
            }
        });




        mBtStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //通过intent将数据传递给DownloadService
                Intent intent=new Intent(MainActivity.this, DownloadService.class);
                intent.setAction(DownloadService.ACTION_STOP);
                intent.putExtra("fileInfo", fileInfo);//将创建的fileInfo对象传进去
                startService(intent);
            }
        });
        //注册广播接收器
        IntentFilter filter =new IntentFilter();
        filter.addAction(DownloadService.ACTION_UPDATE);
        registerReceiver(mReceiver,filter);
    }

    private void start(){
        //通过intent将数据传递给DownloadService
        Intent intent=new Intent(MainActivity.this,DownloadService.class);
        intent.setAction(DownloadService.ACTION_START);
        intent.putExtra("fileInfo", fileInfo);//将创建的fileInfo对象传进去
        startService(intent);
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
                mPbProgress.setProgress(finished);
            }
        }
    };
}
