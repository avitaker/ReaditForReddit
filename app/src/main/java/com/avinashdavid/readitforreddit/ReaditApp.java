package com.avinashdavid.readitforreddit;

import android.app.Application;

import com.orm.SugarApp;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import timber.log.Timber;

/**
 * Created by avinashdavid on 3/6/17.
 */

public class ReaditApp extends SugarApp {
    @Override
    public void onCreate() {
        super.onCreate();
        Timber.plant(new Timber.DebugTree());
        Realm.init(getApplicationContext());
    }
}
