package com.alon.spyapp.utils;

import android.app.Application;

public class MyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Converter.initHelper();
    }
}
