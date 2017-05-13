package com.avinashdavid.readitforreddit.MiscUtils;

/**
 * Created by avinashdavid on 3/17/17.
 * This class contains constants for network-call result broadcast intents, JSON parsing and more
 */

public class Constants {
    public static final String BROADCAST_SUBREDDITS_LOADED = "com.avinashdavid.readitforreddit.BROADCAST_SUBREDDITS_LOADED";
    public static final String BROADCAST_COMMENTS_LOADED = "com.avinashdavid.readitforreddit.BROADCAST_COMMENTS";
    public static final String BROADCAST_POSTS_LOADED = "com.avinashdavid.readitforreddit.BROADCAST_POSTS";
    public static final String BROADCAST_RANDOM_SUBREDDIT_POSTS_LOADED = "com.avinashdavid.readitforreddit.BROADCAST_RANDOM_SUBREDDIT";
    public static final String BROADCAST_ERROR_WHILE_RETREIVING_POSTS = "com.avinashdavid.readitforreddit.BROADCAST_ERROR_POSTS";
    public static final String BROADCAST_SUBREDDIT_WIDGET = "com.avinashdavid.readitforreddit.BROADCAST_SUBREDDIT";
    public static final String BROADCAST_NO_SUCH_SUBREDDIT = "com.avinashdavid.readitforreddit.NO_SUCH_SUBREDDIT";
    public static final String BROADCAST_SUBREDDIT_BANNED = "com.avinashdavid.readitforreddit.SUBREDDIT_BANNED";
    public static final String BROADCAST_SUBREDDIT_ADDED = "com.avinashdavid.readitforreddit.SUBREDDIT_ADDED";
    public static final String BROADCAST_SUBREDDIT_PRESENT = "com.avinashdavid.readitforreddit.SUBREDDIT_PRESENT";
    public static final String BROADCAST_ERROR_WHILE_LOADING_COMMENTS = "com.avinashdavid.readitforreddit.ERROR_LOADING_COMMENTS";
    public static final String BROADCAST_SIDEBAR_ERROR = "com.avinashdavid.readitforreddit.SIDEBAR_ERROR";
    public static final String BROADCAST_SIDEBAR = "com.avinashdavid.readitforreddit.SIDEBAR_LOADED";

    public static final String KEY_NETWORK_REQUEST_ERROR = "netErr";

    public static final String BROADCAST_MORE_COMMENTS_LOADED = "com.avinashdavid.readitforreddit.BROADCAST_MORE_COMMENTS";
    public static final String BROADCAST_MORE_COMMENTS_ERROR = "com.avinashdavid.readitforreddit.BROADCAST_ERROR_MORE_COMMENTS";

    public static final String KEY_COMMENTS_FIRST_CHILD = "commentsScrollPos";
    public static final String KEY_COMMENTS_OFFSET = "commentsOffset";
    public static final String KEY_POSTS_SCROLL_POSITION = "postsScrollPos";
    public static final String KEY_POSTS_OFFSET = "postsOffset";

    public static final String KIND_KEY = "kind";
    public static final String LISTING_KIND = "Listing";
    public static final String DATA_KEY = "data";
    public static final String CHILDREN_KEY = "children";
    public static final String DEPTH_KEY = "depth";
    public static final String PARENT_KEY = "parent_id";
    public static final String LINK_KIND = "t3";
    public static final String COMMENT_KIND = "t1";
    public static final String ID_KEY = "id";
    public static final String SCORE_HIDDEN_KEY = "score_hidden";
    public static final String SCORE_KEY = "score";
    public static final String BODY_HTML = "body_html";
    public static final String AUTHOR_KEY = "author";
    public static final String REPLIES_KEY = "replies";
    public static final String TIME_CREATED_KEY = "created_utc";
    public static final String KEY_AUTHOR_FLAIR = "author_flair_text";
    public static final String GILDED = "gilded";
    public static final String EDITED = "edited";

    public static final String MORE_KIND = "more";
    public static final String COUNT_KEY = "count";
}
