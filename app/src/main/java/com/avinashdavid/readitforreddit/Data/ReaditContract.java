package com.avinashdavid.readitforreddit.Data;

import android.net.Uri;
import android.provider.BaseColumns;
import android.support.annotation.Nullable;

/**
 * Created by avinashdavid on 4/14/17.
 */

public class ReaditContract {
    public static final String AUTHORITY = "com.avinashdavid.readitforreddit";
    public static final String PATH_AFTER = "after";
    public static final String PATH_SORT = "sort";
    public static final String PATH_POST_ID = "postId";

    private ReaditContract() {
    }

    public static class RedditListingEntry implements BaseColumns {
        public static final String TABLE_NAME = "redditListingTable";
        public static final String COLUMN_POST_ID = "postId";
        public static final String COLUMN_TIMESTAMP = "timestamp";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_VOTE_COUNT = "voteCount";
        public static final String COLUMN_COMMENTS_COUNT = "commentsCount";
        public static final String COLUMN_AUTHOR = "author";
        public static final String COLUMN_SUBREDDIT = "subreddit";
        public static final String COLUMN_TIME_CREATED = "timeCreated";
        public static final String COLUMN_SELFTEXT_HTML = "selftext";
        public static final String COLUMN_DOMAIN = "domain";
        public static final String COLUMN_AFTER = "after";
        public static final String COLUMN_URL = "url";
        public static final String COLUMN_THUMBNAIL_URL = "thumbnailUrl";

        public static Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/listings");

        public static Uri getUriSubreddit(String subredditName){
            return CONTENT_URI.buildUpon().appendPath(subredditName).build();
        }

        public static Uri getUriAfter(String after){
            return CONTENT_URI.buildUpon().appendPath(PATH_AFTER).appendPath(after).build();
        }

        public static Uri getUriSort(String sort, @Nullable String after){
            if (after!=null){
                return getUriAfter(after).buildUpon().appendPath(PATH_SORT).appendPath(sort).build();
            } else {
                return CONTENT_URI.buildUpon().appendPath(PATH_SORT).appendPath(sort).build();
            }
        }
    }

    public static class CommentEntry implements BaseColumns {
        public static final String TABLE_NAME = "commentTable";
        public static final String COLUMN_COMMENT_ID = "commentId";
        public static final String COLUMN_TIMESTAMP = "timestamp";
        public static final String COLUMN_POST_ID = "linkId";
        public static final String COLUMN_SCORE_HIDDEN = "scoreHidden";
        public static final String COLUMN_SCORE = "score";
        public static final String COLUMN_COMMENT_AUTHOR = "author";
        public static final String COLUMN_BODY_HTML = "bodyHtml";
        public static final String COLUMN_PARENT = "parent";
        public static final String COLUMN_TIME_CREATED = "timeCreated";
        public static final String COLUMN_DEPTH = "depth";
        public static final String COLUMN_HAS_REPLIES = "hasReplies";
        public static final String COLUMN_AUTHOR_FLAIR_TEXT = "authorFlairText";

        public static Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY+"/comments");

        public static Uri getUriComments(String postId){
            return CONTENT_URI.buildUpon().appendPath(PATH_POST_ID).appendPath(postId).build();
        }
    }
}
