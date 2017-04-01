package com.avinashdavid.readitforreddit.RedditSessionUtils;

import android.content.Context;

import com.avinashdavid.readitforreddit.R;


/**
 * Created by avinashdavid on 3/6/17.
 */

public class SubredditDisplayInfo {
    private Context mContext;
    private String subreddit;
    private String after;
    private Integer limit;
    private Integer count;
    private String sorting;

    public SubredditDisplayInfo(Context context, String subreddit) {
        this.subreddit = subreddit;
        this.mContext = context;
        this.limit = mContext.getResources().getInteger(R.integer.default_post_limit);
    }

    public SubredditDisplayInfo(Context context, String subreddit, String after) {
        this.subreddit = subreddit;
        this.after = after;
        this.mContext = context;
        this.limit = mContext.getResources().getInteger(R.integer.default_post_limit);
    }

    public String getSubreddit() {
        return subreddit;
    }

    public void setSubreddit(String subreddit) {
        this.subreddit = subreddit;
    }

    public String getAfter() {
        return after;
    }

    public void setAfter(String after) {
        this.after = after;
    }

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }
}
