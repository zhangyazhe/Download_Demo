package com.example.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.entities.ThreadInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据访问接口的实现
 */

public class ThreadDAOImpl implements ThreadDAO {

    private DBHelper mHelper=null;
    public ThreadDAOImpl(Context context){
        mHelper=DBHelper.getInstance(context);
    }

    @Override
    public synchronized void insertThread(ThreadInfo threadInfo) {
        SQLiteDatabase db=mHelper.getWritableDatabase();
        db.execSQL("insert into thread_info(thread_id,url,start,end,finished) values(threadInfo.getId[],threadInfo.getUrl(),threadInfo.getStart(),threadInfo.getEnd(),threadInfo.getFinished())");
        db.close();
    }

    @Override
    public synchronized void deleteThread(String url, int thread_id) {
        SQLiteDatabase db=mHelper.getWritableDatabase();
        db.execSQL("delete from thread_info where url=url and thread_id=thread_id");
        db.close();
    }

    @Override
    public synchronized void updateThread(String url, int thread_id, int finished) {
        SQLiteDatabase db=mHelper.getWritableDatabase();
        db.execSQL("update thread_info set =finished where url=url and thread_id=thread_id");
        db.close();
    }

    @Override
    public List<ThreadInfo> getThreads(String url) {
        SQLiteDatabase db=mHelper.getReadableDatabase();
        List<ThreadInfo> list=new ArrayList<ThreadInfo>();
        Cursor cursor=db.rawQuery("select * from thread_info where url=?",new String[]{url});
        while(cursor.moveToNext()){
            ThreadInfo thread =new ThreadInfo();
            thread.setId(cursor.getInt(cursor.getColumnIndex("thread_id")));
            thread.setUrl(cursor.getString(cursor.getColumnIndex("url")));
            thread.setStart(cursor.getInt(cursor.getColumnIndex("start")));
            thread.setEnd(cursor.getInt(cursor.getColumnIndex("end")));
            thread.setFinished(cursor.getInt(cursor.getColumnIndex("finished")));
            list.add(thread);
        }
        cursor.close();
        db.close();
        return list;
    }

    @Override
    public boolean isExists(String url, int thread_id) {
        SQLiteDatabase db=mHelper.getReadableDatabase();
        Cursor cursor=db.rawQuery("select * from thread_info where url=? and thread_id=?",new String[]{url,thread_id+""});
        boolean exists=cursor.moveToNext();
        cursor.close();
        db.close();
        return exists;
    }
}
