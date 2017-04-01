package com.avinashdavid.readitforreddit.PostUtils;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by avinashdavid on 3/6/17.
 */

public class RedditPost extends RealmObject {
    @PrimaryKey
    public String mPostId;

    public long timestamp;

    public String mTitle;
    public int voteCount;

    public int commentsCount;
    public String author;
    public String subreddit;
    public float timeCreated;

    private String selftext_html;

    public String domain;

    public String after;
    private String url;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
    //    public RedditListing(String postId) {
//        mPostId = postId;
//    }
//
//    public RedditListing(String postId, String title) {
//        mPostId = postId;
//        mTitle = title;
//    }
//
//    public RedditListing(String postId, String title, int voteCount) {
//        mTitle = title;
//        mPostId = postId;
//        this.voteCount = voteCount;
//    }


    public String getPostId() {
        return mPostId;
    }

    public void setPostId(String postId) {
        mPostId = postId;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        mTitle = title;
    }

    public int getVoteCount() {
        return voteCount;
    }

    public void setVoteCount(int voteCount) {
        this.voteCount = voteCount;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getCommentsCount() {
        return commentsCount;
    }

    public void setCommentsCount(int commentsCount) {
        this.commentsCount = commentsCount;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getSubreddit() {
        return subreddit;
    }

    public void setSubreddit(String subreddit) {
        this.subreddit = subreddit;
    }

    public float getTimeCreated() {
        return timeCreated;
    }

    public void setTimeCreated(float timeCreated) {
        this.timeCreated = timeCreated;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getAfter() {
        return after;
    }

    public void setAfter(String after) {
        this.after = after;
    }

    public String getSelftext_html() {
        return selftext_html;
    }

    public void setSelftext_html(String selftext_html) {
        this.selftext_html = selftext_html;
    }
}
