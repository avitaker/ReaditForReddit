package com.avinashdavid.readitforreddit.NetworkUtils;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.avinashdavid.readitforreddit.User.UserThingsSingleton;

/**
 * Created by avinashdavid on 3/6/17.
 * This class generates all the HTTP urls that are called in the app
 */

public class UriGenerator {
    private static final String KEY_LIMIT = "limit";
    private static final String KEY_AFTER = "after";
    private static final String PATH_SEGMENT_COMMENTS = "comments";
    private static final String PATH_SEGMENT_USER = "user";
    private static final String PATH_SEGMENT_ABOUT = "about";
    private static final String PATH_SEGMENT_OVERVIEW = "overview";
    private static final String PATH_SEGMENT_SUBMITTED = "submitted";
    private static final String PATH_SUBREDDITS = "subreddits";
    private static final String PATH_USERS = "users";
    private static final String KEY_SORT = "sort";
    private static final String subredditMarkerString = "r";
    private static final String KEY_SHOW = "show";
    private static final String KEY_COMMENT_DEPTH = "depth";
    private static  final String KEY_COUNT = "count";

    private static final String QUERY_PARAM_COMMENT = "comment";
    private static final String QUERY_PARAM_CONTEXT = "context";

    private static final String KEY_RESTRICT_SR = "restrict_sr";

    private static final int DEFAULT_COMMENT_DEPTH = 10;
    public static final int DEFAULT_COMMENTS_LIMIT = 150;

    private static final Uri baseUnauthUrl = Uri.parse("https://api.reddit.com/");
    private static final Uri baseAuthUrl = Uri.parse("https://oauth.reddit.com");

    public static Uri getBaseUnauthUrl() {
        return baseUnauthUrl.buildUpon().appendQueryParameter(KEY_LIMIT, Integer.toString(20)).build();
    }

    public static Uri getUriPosts(@Nullable String subreddit, @Nullable String sorting, int limit, @Nullable String after){
        Uri.Builder builder = baseUnauthUrl.buildUpon();
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
        Uri.Builder builder = baseUnauthUrl.buildUpon();
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
            if (null!=sorting){
                sort = sorting.toLowerCase();

                if (sort.contains("top")){
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
                else {
                    builder.appendQueryParameter(KEY_SORT, sort);
                }
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

    public static Uri getUriSubredditList(@Nullable String where, boolean ofLoggedInUser){
        Uri.Builder builder = baseUnauthUrl.buildUpon().appendPath(PATH_SUBREDDITS);
        if (ofLoggedInUser){
            builder = baseAuthUrl.buildUpon().appendPath(PATH_SUBREDDITS);
            builder.appendPath("mine").appendPath("subscriber").appendQueryParameter(KEY_LIMIT, Integer.toString(Integer.MAX_VALUE)).appendQueryParameter(KEY_SHOW, "all");
        } else if (where != null) {
            builder.appendPath(where);
        } else {
            builder.appendPath("default");
        }
        return builder.build();
    }

    public static Uri getUriCommentsForArticle(@Nullable String subreddit, String articleId, @Nullable String sortOrder){
        Uri.Builder builder = baseUnauthUrl.buildUpon();
        if (articleId.contains("t3_")) articleId = articleId.substring(3);
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
        Uri.Builder builder = baseUnauthUrl.buildUpon();
        builder.appendPath(subredditMarkerString).appendPath(subreddit).appendPath("about");
        return builder.build();
    }

    public static Uri getUriMoreComments(String articleId, String children){
        Uri.Builder builder = baseUnauthUrl.buildUpon();
        builder.appendPath("api").appendPath("morechildren").appendQueryParameter("api_type", "json").appendQueryParameter("link_id", "t3_"+articleId);
        StringBuilder soFar = new StringBuilder(builder.build().toString());
        soFar.append("&children=" + children);
        return Uri.parse(soFar.toString());
    }

    public static Uri getShareableUriComments(@NonNull String postId){
        return Uri.parse("https://www.reddit.com/").buildUpon().appendPath(PATH_SEGMENT_COMMENTS).appendPath(postId).build();
    }

    public static String getUriUserComments(@NonNull String userId) {
        Uri.Builder builder = baseUnauthUrl.buildUpon();
        builder.appendPath(PATH_SEGMENT_USER).appendPath(userId).appendPath(PATH_SEGMENT_COMMENTS);
        builder.appendQueryParameter(KEY_LIMIT, "20");
        return builder.build().toString() + ".json";
    }

    public static String getUriUserComments(@NonNull String userId, boolean loadMore) {
        Uri.Builder builder = baseUnauthUrl.buildUpon();
        builder.appendPath(PATH_SEGMENT_USER).appendPath(userId).appendPath(PATH_SEGMENT_COMMENTS);
        builder.appendQueryParameter(KEY_LIMIT, "20");
        if (loadMore) builder.appendQueryParameter(KEY_AFTER, UserThingsSingleton.INSTANCE.getLastCommentFullName());
        String toReturn = builder.build().toString();
        return toReturn;
    }

    public static String getUriUserOverview(@NonNull String userId) {
        Uri.Builder builder = baseUnauthUrl.buildUpon();
        builder.appendPath(PATH_SEGMENT_USER).appendPath(userId).appendPath(PATH_SEGMENT_OVERVIEW);
        builder.appendQueryParameter(KEY_LIMIT, "20");
        return builder.build().toString();
    }

    public static String getUriUserOverview(@NonNull String userId, boolean loadMore) {
        Uri.Builder builder = baseUnauthUrl.buildUpon();
        builder.appendPath(PATH_SEGMENT_USER).appendPath(userId).appendPath(PATH_SEGMENT_OVERVIEW);
        builder.appendQueryParameter(KEY_LIMIT, "20");
        if (loadMore) {
            String after = UserThingsSingleton.INSTANCE.getLastThingFullName();
            builder.appendQueryParameter(KEY_AFTER, after);
        }
        String toReturn = builder.build().toString();
        return toReturn;
    }

    public static String getUriUserSubmitted(@NonNull String userId){
        Uri.Builder builder = baseUnauthUrl.buildUpon();
        builder.appendPath(PATH_SEGMENT_USER).appendPath(userId).appendPath(PATH_SEGMENT_SUBMITTED);
        builder.appendQueryParameter(KEY_LIMIT, "20");
        return builder.build().toString();
    }

    public static String getUriUserSubmitted(@NonNull String userId, boolean loadMore) {
        Uri.Builder builder = baseUnauthUrl.buildUpon();
        builder.appendPath(PATH_SEGMENT_USER).appendPath(userId).appendPath(PATH_SEGMENT_SUBMITTED);
        builder.appendQueryParameter(KEY_LIMIT, "20");
        if (loadMore) builder.appendQueryParameter(KEY_AFTER, UserThingsSingleton.INSTANCE.getLastSubmittedFullName());
        String toReturn = builder.build().toString();
        return toReturn;
    }

    public static String getUriUserAbout(@NonNull String userId) {
        Uri.Builder builder = baseUnauthUrl.buildUpon();
        builder.appendPath(PATH_SEGMENT_USER).appendPath(userId).appendPath(PATH_SEGMENT_ABOUT);
        return builder.build().toString();
    }

    public static Uri getUriCommentThread(@NonNull String postId, @NonNull String commentId, int numberOfParents) {
        Uri.Builder builder = baseUnauthUrl.buildUpon();
        builder.appendPath(PATH_SEGMENT_COMMENTS).appendPath(postId);
        builder.appendQueryParameter("comment", commentId);
        if (numberOfParents > 0 && numberOfParents <= 8){
            builder.appendQueryParameter("context", Integer.toString(numberOfParents));
        }
        return builder.build();
    }
}
