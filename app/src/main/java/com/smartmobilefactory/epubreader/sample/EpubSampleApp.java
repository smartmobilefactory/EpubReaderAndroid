package com.smartmobilefactory.epubreader.sample;

import android.app.Application;

import com.facebook.stetho.Stetho;
import com.facebook.stetho.dumpapp.plugins.FilesDumperPlugin;

public class EpubSampleApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Stetho.initialize(Stetho.newInitializerBuilder(this)
                .enableDumpapp(() -> new Stetho.DefaultDumperPluginsBuilder(this)
                        .provide(new FilesDumperPlugin(this))
                        .finish())
                .enableWebKitInspector(Stetho.defaultInspectorModulesProvider(this))
                .build());

    }
}
