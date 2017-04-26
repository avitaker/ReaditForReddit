package com.avinashdavid.readitforreddit.NetworkUtils;

import android.net.Uri;
import android.support.annotation.Nullable;

/**
 * Created by avinashdavid on 3/6/17.
 */

public class UriGenerator {
    private static final String KEY_LIMIT = "limit";
    private static final String KEY_AFTER = "after";
    private static final String PATH_SEGMENT_COMMENTS = "comments";
    private static final String KEY_SORT = "sort";
    private static final String subredditMarkerString = "r";
    private static final String KEY_SUBREDDITS = "subreddits";
    private static final String KEY_COMMENT_DEPTH = "depth";

    private static final String KEY_RESTRICT_SR = "restrict_sr";

    private static final int DEFAULT_COMMENT_DEPTH = 10;
    public static final int DEFAULT_COMMENTS_LIMIT = 150;

    private static final Uri baseListingUri = Uri.parse("https://api.reddit.com/");

    public static Uri getBaseListingUri() {
        return baseListingUri.buildUpon().appendQueryParameter(KEY_LIMIT, Integer.toString(20)).build();
    }

    public static Uri getUriPosts(@Nullable String subreddit, @Nullable String sorting, int limit, @Nullable String after){
        Uri.Builder builder = baseListingUri.buildUpon();
        if (subreddit!=null) {
            builder.appendPath(subredditMarkerString).appendPath(subreddit);
        }
        if (sorting!=null){
            builder.appendPath(sorting);
        }
        if (limit != 0) {
            builder.appendQueryParameter(KEY_LIMIT, Integer.toString(limit));
        } else {
            builder.appendQueryParameter(KEY_LIMIT, Integer.toString(20));
        }
        if (after!=null){
            builder.appendQueryParameter(KEY_AFTER, after);
        }
        return builder.build();
    }

    public static Uri getUriSubredditList(@Nullable String where, @Nullable String mineWhere){
        Uri.Builder builder = baseListingUri.buildUpon().appendPath(KEY_SUBREDDITS);
        if (null != where){
            builder.appendPath(where);
            if (null!=mineWhere){
                builder.appendPath(mineWhere);
            }
        } else {
            builder.appendPath("default");
        }
        return builder.build();
    }

    public static Uri getUriCommentsForArticle(@Nullable String subreddit, String articleId, @Nullable String sortOrder){
        Uri.Builder builder = baseListingUri.buildUpon();
        if (null!=subreddit){
            builder.appendPath(subredditMarkerString).appendPath(subreddit);
        }
        builder.appendPath(PATH_SEGMENT_COMMENTS).appendPath(articleId);
        builder.appendPath(".json");
        if (null!=sortOrder){
            builder.appendQueryParameter(KEY_SORT, sortOrder);
        }
        builder.appendQueryParameter(KEY_COMMENT_DEPTH, Integer.toString(DEFAULT_COMMENT_DEPTH));
//        builder.appendQueryParameter(KEY_LIMIT, Integer.toString(DEFAULT_COMMENTS_LIMIT));
        return builder.build();
    }

    public static Uri getUriSearch(@Nullable String subreddit, @Nullable String query, @Nullable String after, @Nullable String sort, boolean restrictSr){
        Uri.Builder builder = baseListingUri.buildUpon();
        if (null!=subreddit){
            builder.appendPath(subredditMarkerString).appendPath(subreddit);
        }
        if (null!=sort && query==null){
            builder.appendPath(sort);
        }
        if (query!=null) {
            builder.appendPath("search");
            builder.appendQueryParameter("q", query);
            if (null!=sort){
                builder.appendQueryParameter(KEY_SORT, sort);
            }
        }
        if (null!=after){
            builder.appendQueryParameter(KEY_AFTER, after);
        }
        if (restrictSr){
            builder.appendQueryParameter(KEY_RESTRICT_SR, "on");
        }
        builder.appendQueryParameter(KEY_LIMIT, Integer.toString(20));
        return builder.build();
    }

    public static Uri getUriSubredditAbout(String subreddit){
        Uri.Builder builder = baseListingUri.buildUpon();
        builder.appendPath(subredditMarkerString).appendPath(subreddit).appendPath("about");
        return builder.build();
    }

    public static Uri getUriMoreComments(String articleId){
        Uri.Builder builder = baseListingUri.buildUpon();
        builder.appendPath(PATH_SEGMENT_COMMENTS).appendPath(articleId);
        builder.appendPath("api").appendPath("morechildren").appendQueryParameter("api_type", "json").appendQueryParameter("showmore","true").appendQueryParameter("link_id", "t3_"+articleId);
        return builder.build();
    }
}
