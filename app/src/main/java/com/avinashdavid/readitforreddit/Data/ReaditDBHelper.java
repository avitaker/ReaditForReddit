package com.avinashdavid.readitforreddit.Data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by avinashdavid on 4/14/17.
 */

public class ReaditDBHelper extends SQLiteOpenHelper {
    private static final String TEXT_MARKER = " TEXT";
    private static final String COMMA_MARKER = ",";
    private static final String INTEGER_MARKER = " INTEGER";
    static final String REAL_MARKER = " REAL";

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "Readit.db";

    public ReaditDBHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    private static final String SQL_CREATE_REDDITLISTINGS =
            "CREATE TABLE " + ReaditContract.RedditListingEntry.TABLE_NAME + " (" +
                    ReaditContract.RedditListingEntry._ID + " INTEGER PRIMARY KEY " + COMMA_MARKER +
                    ReaditContract.RedditListingEntry.COLUMN_POST_ID + TEXT_MARKER + COMMA_MARKER +
                    ReaditContract.RedditListingEntry.COLUMN_TIMESTAMP + INTEGER_MARKER + COMMA_MARKER +
                    ReaditContract.RedditListingEntry.COLUMN_TITLE + TEXT_MARKER + COMMA_MARKER +
                    ReaditContract.RedditListingEntry.COLUMN_VOTE_COUNT + INTEGER_MARKER + COMMA_MARKER +
                    ReaditContract.RedditListingEntry.COLUMN_COMMENTS_COUNT + INTEGER_MARKER + COMMA_MARKER +
                    ReaditContract.RedditListingEntry.COLUMN_AUTHOR + TEXT_MARKER + COMMA_MARKER +
                    ReaditContract.RedditListingEntry.COLUMN_SUBREDDIT + TEXT_MARKER + COMMA_MARKER +
                    ReaditContract.RedditListingEntry.COLUMN_TIME_CREATED + INTEGER_MARKER + COMMA_MARKER +
                    ReaditContract.RedditListingEntry.COLUMN_SELFTEXT_HTML + TEXT_MARKER + COMMA_MARKER +
                    ReaditContract.RedditListingEntry.COLUMN_DOMAIN + TEXT_MARKER + COMMA_MARKER +
                    ReaditContract.RedditListingEntry.COLUMN_AFTER + TEXT_MARKER + COMMA_MARKER +
                    ReaditContract.RedditListingEntry.COLUMN_URL + TEXT_MARKER + COMMA_MARKER +
                    ReaditContract.RedditListingEntry.COLUMN_THUMBNAIL_URL + TEXT_MARKER+ ")";

    private static final String SQL_CREATE_COMMENTS =
            "CREATE TABLE " + ReaditContract.CommentEntry.TABLE_NAME + " (" +
                    ReaditContract.CommentEntry._ID + " INTEGER PRIMARY KEY " + COMMA_MARKER +
                    ReaditContract.CommentEntry.COLUMN_COMMENT_ID + TEXT_MARKER + COMMA_MARKER +
                    ReaditContract.CommentEntry.COLUMN_TIMESTAMP + INTEGER_MARKER + COMMA_MARKER +
                    ReaditContract.CommentEntry.COLUMN_POST_ID + TEXT_MARKER + COMMA_MARKER +
                    ReaditContract.CommentEntry.COLUMN_SCORE_HIDDEN + INTEGER_MARKER + COMMA_MARKER +
                    ReaditContract.CommentEntry.COLUMN_SCORE + INTEGER_MARKER + COMMA_MARKER +
                    ReaditContract.CommentEntry.COLUMN_COMMENT_AUTHOR + TEXT_MARKER + COMMA_MARKER +
                    ReaditContract.CommentEntry.COLUMN_BODY_HTML + TEXT_MARKER + COMMA_MARKER +
                    ReaditContract.CommentEntry.COLUMN_PARENT + TEXT_MARKER + COMMA_MARKER +
                    ReaditContract.CommentEntry.COLUMN_TIME_CREATED + INTEGER_MARKER + COMMA_MARKER +
                    ReaditContract.CommentEntry.COLUMN_DEPTH + INTEGER_MARKER + COMMA_MARKER +
                    ReaditContract.CommentEntry.COLUMN_HAS_REPLIES + INTEGER_MARKER + COMMA_MARKER +
                    ReaditContract.CommentEntry.COLUMN_AUTHOR_FLAIR_TEXT + TEXT_MARKER + ")";

    private static final String SQL_DELETE_REDDITLISTINGS =
            "DROP TABLE IF EXISTS " + ReaditContract.RedditListingEntry.TABLE_NAME;

    private static final String SQL_DELETE_COMMENTS =
            "DROP TABLE IF EXISTS " + ReaditContract.CommentEntry.TABLE_NAME;


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_REDDITLISTINGS);
        db.execSQL(SQL_CREATE_COMMENTS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_REDDITLISTINGS);
        db.execSQL(SQL_DELETE_COMMENTS);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
