package com.cet.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by hyc on 2018/4/23 10:34
 */

public class MyDatabaseHelper extends SQLiteOpenHelper {


    public static final String CREATE_WORD="create table if not exists Word(" +
            "word text primary key,translation text,speak_url text,t_speak_url text)";

    public static final String DATABASE_NAME = "Cet.db";

    public static final int VERSION = 1;

    public MyDatabaseHelper(Context context, SQLiteDatabase.CursorFactory factory) {
        super(context, DATABASE_NAME, factory, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_WORD);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
