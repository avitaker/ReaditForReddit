package com.avinashdavid.readitforreddit.NetworkUtils;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import timber.log.Timber;

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
            String sort = sorting.toLowerCase();
            if (!sort.contains("top")) {
                builder.appendPath(sorting);
            } else {
                builder.appendPath("top");
                if (sorting.length()>3) {
                    String timeString;
                    int codeInt = Integer.parseInt(sorting.substring(sorting.length() - 1));
                    switch (codeInt){
                        case GetListingsService.SORT_TOP_ALLTIME:
                            timeString = "all";
                            break;
                        case GetListingsService.SORT_TOP_DAY:
                            timeString = "day";
                            break;
                        case GetListingsService.SORT_TOP_MONTH:
                            timeString = "month";
                            break;
                        case GetListingsService.SORT_TOP_WEEK:
                            timeString = "week";
                            break;
                        case GetListingsService.SORT_TOP_YEAR:
                            timeString = "year";
                            break;
                        default:
                            timeString = "all";
                            break;
                    }
                    builder.appendQueryParameter("sort","top").appendQueryParameter("t",timeString);
                }

            }
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

    public static Uri getUriSearch(@Nullable String subreddit, @Nullable String query, @Nullable String after, @Nullable String sorting, boolean restrictSr){
        Timber.d("calling search");
        Uri.Builder builder = baseListingUri.buildUpon();
        if (null!=subreddit){
            builder.appendPath(subredditMarkerString).appendPath(subreddit);
        }
        String sort = null;
        if (null!=sorting && query==null){
            sort = sorting.toLowerCase();
            if (!sort.contains("top")) {
                builder.appendPath(sorting);
            } else {
                builder.appendPath("top");
                if (sorting.length()>3) {
                    String timeString;
                    int codeInt = Integer.parseInt(sorting.substring(sorting.length() - 1));
                    switch (codeInt){
                        case GetListingsService.SORT_TOP_ALLTIME:
                            timeString = "all";
                            break;
                        case GetListingsService.SORT_TOP_DAY:
                            timeString = "day";
                            break;
                        case GetListingsService.SORT_TOP_MONTH:
                            timeString = "month";
                            break;
                        case GetListingsService.SORT_TOP_WEEK:
                            timeString = "week";
                            break;
                        case GetListingsService.SORT_TOP_YEAR:
                            timeString = "year";
                            break;
                        default:
                            timeString = "all";
                            break;
                    }
                    builder.appendQueryParameter("sort","top").appendQueryParameter("t",timeString);
                }

            }
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

    public static Uri getUriSubredditAbout(String subreddit){
        Uri.Builder builder = baseListingUri.buildUpon();
        builder.appendPath(subredditMarkerString).appendPath(subreddit).appendPath("about");
        return builder.build();
    }

    public static Uri getUriMoreComments(String articleId, String children){
        Uri.Builder builder = baseListingUri.buildUpon();
        builder.appendPath("api").appendPath("morechildren").appendQueryParameter("api_type", "json").appendQueryParameter("link_id", "t3_"+articleId);
        StringBuilder soFar = new StringBuilder(builder.build().toString());
        soFar.append("&children=" + children);
        return Uri.parse(soFar.toString());
    }

    public static Uri getShareableUriComments(@NonNull String postId){
        return Uri.parse("https://www.reddit.com/").buildUpon().appendPath(PATH_SEGMENT_COMMENTS).appendPath(postId).build();
    }
}
