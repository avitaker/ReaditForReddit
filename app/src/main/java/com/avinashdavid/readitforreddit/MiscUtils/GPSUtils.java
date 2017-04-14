package com.avinashdavid.readitforreddit.MiscUtils;

import android.app.Activity;
import android.content.Context;

import com.avinashdavid.readitforreddit.ReaditApp;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import timber.log.Timber;

/**
 * Created by avinashdavid on 4/13/17.
 */

public class GPSUtils {
    public static void setScreenName(Context context, String name){
        Tracker tracker;
        try {
            ReaditApp app = (ReaditApp)((Activity)context).getApplication();
            tracker = app.getDefaultTracker();
            tracker.setScreenName(name);
            tracker.send(new HitBuilders.ScreenViewBuilder().build());
        } catch (Exception e){
            Timber.e(e);
        }
    }
}
