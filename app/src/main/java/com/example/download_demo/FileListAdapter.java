package com.example.download_demo;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.entities.FileInfo;
import com.example.services.DownloadService;

import java.util.List;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

/**
 * 文件列表的适配器
 */

public class FileListAdapter extends BaseAdapter {

    private Context mContext=null;
    private List<FileInfo> mFileList=null;

    public FileListAdapter(Context mContext, List<FileInfo> mFileList) {
        this.mContext = mContext;
        this.mFileList = mFileList;
    }

    @Override
    public int getCount() {
        return mFileList.size();
    }

    @Override
    public Object getItem(int position) {
        return mFileList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        ViewHolder holder=null;
        if (view == null) {
            //加载视图
            view= LayoutInflater.from(mContext).inflate(R.layout.listitem,null);
            //获得布局中的控件
            holder=new ViewHolder();
            holder.bt_start=(Button)view.findViewById(R.id.bt_start);
            holder.bt_stop=(Button)view.findViewById(R.id.bt_stop);
            holder.tv_name=(TextView)view.findViewById(R.id.tv_name);
            holder.progress_bar=(ProgressBar) view.findViewById(R.id.progress_bar);
            view.setTag(holder);
        }else{
            holder=(ViewHolder)view.getTag();
        }

        //设置视图中的控件
        final FileInfo fileInfo=mFileList.get(position);
        holder.tv_name.setText(fileInfo.getFileName());
        holder.progress_bar.setMax(100);
        holder.progress_bar.setProgress(fileInfo.getFinished());

        holder.bt_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //通过intent将数据传递给DownloadService
                Intent intent=new Intent(mContext,DownloadService.class);
                intent.setAction(DownloadService.ACTION_START);
                intent.putExtra("fileInfo", fileInfo);//将创建的fileInfo对象传进去
                mContext.startService(intent);

            }
        });
        holder.bt_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //通过intent将数据传递给DownloadService
                Intent intent=new Intent(mContext, DownloadService.class);
                intent.setAction(DownloadService.ACTION_STOP);
                intent.putExtra("fileInfo", fileInfo);//将创建的fileInfo对象传进去
                mContext.startService(intent);

            }
        });
        return view;
    }

    /**
     * 更新列表项中的进度条
     */
    public void updateProgress(int id,int progress){
        FileInfo fileInfo=mFileList.get(id);
        fileInfo.setFinished(progress);
        notifyDataSetChanged();
    }

    static class ViewHolder{
        TextView tv_name;
        Button bt_stop,bt_start;
        ProgressBar progress_bar;
    }
}
