package com.avinashdavid.readitforreddit.Widget;

import com.orm.SugarRecord;

/**
 * Created by avinashdavid on 3/17/17.
 */

public class RedditPostWidget extends SugarRecord {
    public String mPostId;

    public String mTitle;
    public String voteCountString;

    public String commentsCountString;
    public String author;
    public String subreddit;
    public String timeElapsedString;

    public String domain;

    public RedditPostWidget() {
    }

    public RedditPostWidget(String postId, String title, String voteCountString, String commentsCountString, String author, String subreddit, String timeElapsedString, String domain) {
        mPostId = postId;
        mTitle = title;
        this.voteCountString = voteCountString;
        this.commentsCountString = commentsCountString;
        this.author = author;
        this.subreddit = subreddit;
        this.timeElapsedString = timeElapsedString;
        this.domain = domain;
    }
}
