package com.databases.example.app;

import android.app.Application;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.databases.example.BuildConfig;

import io.fabric.sdk.android.Fabric;

/**
 * Created by kwelsh on 3/4/17.
 * Provide our own Application so we have an easy way to setup Fabric Crash Reporting
 */
public class FinanceApplication extends Application{
    @Override
    public void onCreate() {
        super.onCreate();

        // Set up Crashlytics, disabled for builds of the type "developer"
        Crashlytics crashlyticsKit = new Crashlytics.Builder()
                .core(new CrashlyticsCore.Builder().disabled(!BuildConfig.REPORT_CRASHES).build())
                .build();

        Fabric.with(this, crashlyticsKit);
    }
}
