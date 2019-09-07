package com.example.myapplication;

import android.app.Application;

import com.github.tcking.giraffecompressor.GiraffeCompressor;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        GiraffeCompressor.DEBUG = true;
        GiraffeCompressor.init(this);
    }
}
