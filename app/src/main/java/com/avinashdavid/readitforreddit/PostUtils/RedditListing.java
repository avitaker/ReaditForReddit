package com.avinashdavid.readitforreddit.PostUtils;

import android.content.ContentValues;
import android.support.annotation.NonNull;

import com.avinashdavid.readitforreddit.Data.ReaditContract;
import com.google.gson.annotations.SerializedName;
import com.orm.SugarRecord;

import org.json.JSONObject;

import java.util.Timer;

import timber.log.Timber;

/**
 * Created by avinashdavid on 3/17/17.
 * This class extends ORM storage object, and contains all the relevant information for a Reddit listing (post)
 */

public class RedditListing extends SugarRecord {
    public String mPostId;

    public long timestamp;

    @SerializedName("title")
    public String mTitle;

    @SerializedName("score")
    public int voteCount;

    @SerializedName("num_comments")
    public int commentsCount;

    public String author;

    @SerializedName("subreddit_name_prefixed")
    public String subreddit;

    @SerializedName("created_utc")
    public long timeCreated;

    public String selftext_html;

    public String domain;

    public String after;

    public String url;

    @SerializedName("thumbnail")
    public String thumbnailUrl;

    public boolean isGilded;

    @SerializedName("over_18")
    public boolean isNSFW;

    @SerializedName("likes")
    public String likes;

    public RedditListing() {
    }

    public RedditListing(String postId, long timestamp, String title, int voteCount, int commentsCount, String author, String subreddit, long timeCreated, String selftext_html, String domain, String after, String url, String thumbnailUrl, boolean isGilded, boolean isNSFW, String likes) {
        this.mPostId = postId;
        this.timestamp = timestamp;
        this.mTitle = title;
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
        this.isGilded = isGilded;
        this.isNSFW = isNSFW;
        this.likes = likes;
    }

    public RedditListing(JSONObject linkObject){
        try {
            this.isNSFW = Boolean.valueOf(linkObject.getString("over_18"));
            this.mPostId = linkObject.getString("id");
            this.mTitle = linkObject.getString("title");
            this.voteCount = linkObject.getInt("score");
            this.commentsCount = linkObject.getInt("num_comments");
            this.timeCreated = linkObject.getLong("created_utc");
            this.domain = linkObject.getString("domain");
            this.author = linkObject.getString("author");
            this.subreddit = linkObject.getString("subreddit");
            this.url = linkObject.getString("url");
            this.selftext_html = linkObject.getString("selftext_html");
            this.thumbnailUrl = linkObject.getString("thumbnail");
            this.isGilded = linkObject.getInt("gilded")>0;
            this.likes = linkObject.getString("likes");
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    public static ContentValues makeContentValues(@NonNull RedditListing redditListing){
        ContentValues cv = new ContentValues();
        cv.put(ReaditContract.RedditListingEntry.COLUMN_POST_ID, redditListing.mPostId);
        cv.put(ReaditContract.RedditListingEntry.COLUMN_TIMESTAMP, redditListing.timestamp);
        cv.put(ReaditContract.RedditListingEntry.COLUMN_TITLE, redditListing.mTitle);
        cv.put(ReaditContract.RedditListingEntry.COLUMN_VOTE_COUNT, redditListing.voteCount);
        cv.put(ReaditContract.RedditListingEntry.COLUMN_COMMENTS_COUNT, redditListing.commentsCount);
        cv.put(ReaditContract.RedditListingEntry.COLUMN_AUTHOR, redditListing.author);
        cv.put(ReaditContract.RedditListingEntry.COLUMN_SUBREDDIT, redditListing.subreddit);
        cv.put(ReaditContract.RedditListingEntry.COLUMN_TIME_CREATED, redditListing.timeCreated);
        cv.put(ReaditContract.RedditListingEntry.COLUMN_SELFTEXT_HTML, redditListing.selftext_html);
        cv.put(ReaditContract.RedditListingEntry.COLUMN_DOMAIN, redditListing.domain);
        cv.put(ReaditContract.RedditListingEntry.COLUMN_AFTER, redditListing.after);
        cv.put(ReaditContract.RedditListingEntry.COLUMN_URL, redditListing.url);
        cv.put(ReaditContract.RedditListingEntry.COLUMN_THUMBNAIL_URL, redditListing.thumbnailUrl);
        return cv;
    }
}
