package com.avinashdavid.readitforreddit.PostUtils;

import com.orm.SugarRecord;

/**
 * Created by avinashdavid on 3/17/17.
 */

public class RedditListing extends SugarRecord {
    public String mPostId;

    public long timestamp;

    public String mTitle;
    public int voteCount;

    public int commentsCount;
    public String author;
    public String subreddit;
    public float timeCreated;

    public String selftext_html;

    public String domain;

    public String after;
    public String url;

    public String thumbnailUrl;

    public RedditListing() {
    }

    public RedditListing(String postId, long timestamp, String title, int voteCount, int commentsCount, String author, String subreddit, float timeCreated, String selftext_html, String domain, String after, String url, String thumbnailUrl) {
        mPostId = postId;
        this.timestamp = timestamp;
        mTitle = title;
        this.voteCount = voteCount;
        this.commentsCount = commentsCount;
        this.author = author;
        this.subreddit = subreddit;
        this.timeCreated = timeCreated;
        this.selftext_html = selftext_html;
        this.domain = domain;
        this.after = after;
        this.url = url;
        this.thumbnailUrl = thumbnailUrl;
    }
}
