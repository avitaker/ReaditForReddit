package com.avinashdavid.readitforreddit;

import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;

import com.avinashdavid.readitforreddit.MiscUtils.GeneralUtils;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.orm.SugarApp;
import java.net.URL;

import io.realm.Realm;
import timber.log.Timber;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

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
//        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
//
//        GeneralUtils.setFont(sp.getBoolean(getString(R.string.pref_key_use_custom_font), false), sp.getString(getString(R.string.pref_key_select_font), getString(R.string.font_calibri)));

        GeneralUtils.setFont(this);

        //TODO: set up the client
//        UserAgent userAgent = UserAgent.of("Android app", "com.avinashdavid.readitforreddit.paid", "v0.1", "avitaker");
//        redditClient = new RedditClient(userAgent);
//        Credentials credentials = Credentials.installedApp("avitaker", "http://avinashdavid.com");
//        OAuthHelper oAuthHelper = redditClient.getOAuthHelper();
//        URL url = oAuthHelper.getAuthorizationUrl(credentials, false, true, null);
//        try {
//            oAuthHelper.onUserChallenge("hyah", credentials);
//        } catch (OAuthException e){
//            Timber.e(e);
//        }
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
