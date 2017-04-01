package com.avinashdavid.readitforreddit.NetworkUtils;

import android.content.Context;

import com.avinashdavid.readitforreddit.RedditSessionUtils.SubredditDisplayInfo;

/**
 * Created by avinashdavid on 3/6/17.
 */

public class CurrentSessionInfo {
    private static CurrentSessionInfo sCurrentSessionInfo;
    private Context mContext;

    private SubredditDisplayInfo mSubredditDisplayInfo;

    private CurrentSessionInfo(Context context) {
        mContext = context.getApplicationContext();
    }

    public static CurrentSessionInfo getCurrentSessionInfo(Context context){
        if (sCurrentSessionInfo == null){
            sCurrentSessionInfo = new CurrentSessionInfo(context);
        }
        else if (!sCurrentSessionInfo.mContext.equals(context.getApplicationContext())){
            sCurrentSessionInfo.setContext(context);
        }
        return sCurrentSessionInfo;
    }

    public void setContext(Context context) {
        mContext = context.getApplicationContext();
    }

    public SubredditDisplayInfo getSubredditDisplayInfo() {
        return sCurrentSessionInfo.mSubredditDisplayInfo;
    }

    public void setSubredditDisplayInfo(SubredditDisplayInfo subredditDisplayInfo) {
        sCurrentSessionInfo.mSubredditDisplayInfo = subredditDisplayInfo;
    }
}
