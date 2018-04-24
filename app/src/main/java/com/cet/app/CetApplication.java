package com.cet.app;

import android.app.Application;
import android.content.Context;

/**
 * Created by hyc on 2018/4/23 10:47
 */

public class CetApplication extends Application{

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        this.context = getApplicationContext();
    }

    public static Context getContext() {
        return context;
    }
}
