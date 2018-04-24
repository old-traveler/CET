package com.cet.utils;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.cet.app.CetApplication;
import com.cet.bean.Record;
import com.cet.db.MyDatabaseHelper;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by hyc on 2018/4/23 10:44
 */

public class DbUtil {

    private MyDatabaseHelper myDatabaseHelper;

    private static DbUtil dbUtil;

    private SQLiteDatabase db;

    public static DbUtil getDbUtil(){
        if (dbUtil == null){
            dbUtil = new DbUtil();
        }
        return dbUtil;
    }

    private DbUtil(){
        myDatabaseHelper = new MyDatabaseHelper(CetApplication.getContext(),null);
        db = myDatabaseHelper.getWritableDatabase();
    }

    private String getString(Cursor cursor,String cname){
        return cursor.getString(cursor.getColumnIndex(cname));
    }

    /**
     * 添加一条查询单词记录
     * @param record 记录
     */
    public long addRecord(Record record){
        ContentValues values = new ContentValues();
        values.put("word",record.getWord());
        values.put("translation",record.getTranslation());
        values.put("speak_url",record.getSpeakUrl());
        values.put("t_speak_url",record.gettSpeakUrl());
        return db.insert("Word",null,values);
    }



    public void updateSpeck(String word,String url){
        ContentValues values = new ContentValues();
        if (!TextUtils.isEmpty(url)){
            values.put("speak_url",url);
        }
        db.update("Word",values,"word = ?",new String[]{word});
    }

    public void updateToSpeck(String word,String url){
        ContentValues values = new ContentValues();
        if (!TextUtils.isEmpty(url)){
            values.put("t_speak_url",url);
        }
        db.update("Word",values,"word = ?",new String[]{word});
    }

    /**
     * 查询单个单词记录
     * @param word 单词
     * @return 记录信息
     */
    public Record queryRecord(String word){
        Cursor cursor = db.query("Word",null
                ,"word = ?",new String[]{word}
                ,null,null,null,"1");
        Record record = new Record();
        if(cursor.moveToFirst()){
           record =  getRecord(cursor);
        }
        cursor.close();
        return record;
    }

    /**
     * 通过游标获取一条记录实例
     * @param cursor 游标
     * @return 记录实例信息
     */
    private Record getRecord(Cursor cursor){
        Record record = new Record();
        record.setWord(getString(cursor,"word"));
        record.setTranslation(getString(cursor,"translation"));
        record.setSpeakUrl(getString(cursor,"speak_url"));
        record.settSpeakUrl(getString(cursor,"t_speak_url"));
        return record;
    }

    /**
     * 获取所有查询记录
     * @return 所有的查询记录
     */
    public List<Record> getAllRecord(){
        Cursor cursor = db.query("Word",null
                ,null,null
                ,null,null,null);
        List<Record> records = new ArrayList<>();
        if(cursor.moveToFirst()){
            do {
                records.add(getRecord(cursor));
            }while (cursor.moveToNext());
        }
        cursor.close();
        return records;
    }

    public void deleteRecord(String word){
        db.delete("Word","word = ?",new String[]{word});
    }


}
