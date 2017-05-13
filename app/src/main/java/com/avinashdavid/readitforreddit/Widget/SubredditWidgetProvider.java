package com.avinashdavid.readitforreddit.Widget;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.TaskStackBuilder;
import android.widget.RemoteViews;

import com.avinashdavid.readitforreddit.CommentsActivity;
import com.avinashdavid.readitforreddit.MainActivity;
import com.avinashdavid.readitforreddit.MiscUtils.Constants;
import com.avinashdavid.readitforreddit.NetworkUtils.GetListingsService;
import com.avinashdavid.readitforreddit.R;

import java.util.ArrayList;

/**
 * Created by avinashdavid on 3/16/17.
 * AppWidgetProvider implementation to do basic setup and set initial pending intent template of the homescreen widget
 */

public class SubredditWidgetProvider extends AppWidgetProvider {


    private ArrayList<String> displayStrings;
    BroadcastReceiver mBroadcastReceiver;
    IntentFilter mIntentFilter;

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if (Constants.BROADCAST_SUBREDDIT_WIDGET.equals(intent.getAction())) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, getClass()));
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_listview);
        }
    }

    @Override
    public void onUpdate(final Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        final int N = appWidgetIds.length;
        SharedPreferences mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());

        String subreddit = mSharedPreferences.getString(context.getString(R.string.pref_current_subreddit), "all");
        GetListingsService.loadListingsSubreddit(context, subreddit, null, 0, null, true);

        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int i=0; i<N; i++) {
            int appWidgetId = appWidgetIds[i];

            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_subreddit);
            Intent intent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
            remoteViews.setOnClickPendingIntent(R.id.widget_toolbar, pendingIntent);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH){
                setRemoteAdapter(context, remoteViews);
            } else {
                setRemoteAdapterV11(context, remoteViews);
            }

            boolean usingTabletLayout = mSharedPreferences.getBoolean(context.getString(R.string.pref_boolean_use_tablet_layout), false);

            Intent clickIntentTemplate = usingTabletLayout ? new Intent(context, MainActivity.class) : new Intent(context, CommentsActivity.class);
            PendingIntent clickPendingIntentTemplate = TaskStackBuilder.create(context)
                    .addNextIntentWithParentStack(clickIntentTemplate)
                    .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setEmptyView(R.id.widget_listview, R.id.error);
            remoteViews.setPendingIntentTemplate(R.id.widget_listview, clickPendingIntentTemplate);
            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);

        }
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void setRemoteAdapter(Context context, @NonNull final RemoteViews views){
        Intent intent = new Intent(context, SubredditWidgetRemoteViewsService.class);
        views.setRemoteAdapter(R.id.widget_listview, intent);
    }

    @SuppressWarnings("deprecation")
    private void setRemoteAdapterV11(Context context, @NonNull final RemoteViews views){
        Intent intent = new Intent(context, SubredditWidgetRemoteViewsService.class);
        views.setRemoteAdapter(0, R.id.widget_listview, intent);
    }
}
