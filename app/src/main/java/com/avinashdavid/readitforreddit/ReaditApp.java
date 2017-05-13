package com.avinashdavid.readitforreddit;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.orm.SugarApp;

import io.realm.Realm;
import timber.log.Timber;

/**
 * Created by avinashdavid on 3/6/17.
 * Application object extends SugarApp for SugarORM use, and sets up Timber tree and Google analytics tracker
 */

public class ReaditApp extends SugarApp {
    private Tracker mTracker;

    @Override
    public void onCreate() {
        super.onCreate();
        Timber.plant(new Timber.DebugTree());
        Realm.init(getApplicationContext());
    }

    synchronized public Tracker getDefaultTracker() {
        if (mTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
            mTracker = analytics.newTracker("UA-97160432-1");
        }
        return mTracker;
    }
}
