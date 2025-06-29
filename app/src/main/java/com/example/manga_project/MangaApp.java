package com.example.manga_project;

import android.app.Application;
import com.example.manga_project.Api_cliente.ApiClient;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;

public class MangaApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        ApiClient.setContext(getApplicationContext());
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
    }
}
