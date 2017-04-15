package com.avinashdavid.readitforreddit.Data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.avinashdavid.readitforreddit.PostUtils.CommentRecord;

import java.util.List;

/**
 * Created by avinashdavid on 4/14/17.
 */

public class ReaditProvider extends ContentProvider {

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static final int CODE_LISTINGS_SUBREDDIT = 1;
    static final int CODE_LISTINGS_AFTER = 2;
    static final int CODE_LISTINGS_SORT = 3;
    static final int CODE_LISTINGS_SORT_AND_AFTER = 4;
    static final int CODE_COMMENTS = 5;
    static final int CODE_LISTINGS_ALL = 6;
    static final int CODE_COMMENTS_ALL = 7;

    public static final String[] COMMENT_CURSOR_COLUMNS = new String[]{ReaditContract.CommentEntry.COLUMN_TIMESTAMP,
            ReaditContract.CommentEntry.COLUMN_COMMENT_ID,
            ReaditContract.CommentEntry.COLUMN_POST_ID,
            ReaditContract.CommentEntry.COLUMN_SCORE_HIDDEN,
            ReaditContract.CommentEntry.COLUMN_SCORE,
            ReaditContract.CommentEntry.COLUMN_COMMENT_AUTHOR,
            ReaditContract.CommentEntry.COLUMN_BODY_HTML,
            ReaditContract.CommentEntry.COLUMN_PARENT,
            ReaditContract.CommentEntry.COLUMN_TIME_CREATED,
            ReaditContract.CommentEntry.COLUMN_DEPTH,
            ReaditContract.CommentEntry.COLUMN_HAS_REPLIES,
            ReaditContract.CommentEntry.COLUMN_AUTHOR_FLAIR_TEXT
    };

    private ReaditDBHelper mReaditDBHelper;
    private SQLiteDatabase db;

    static {
        sUriMatcher.addURI(ReaditContract.AUTHORITY, "*", CODE_LISTINGS_SUBREDDIT);
        sUriMatcher.addURI(ReaditContract.AUTHORITY, "listings/" +ReaditContract.PATH_AFTER + "/*", CODE_LISTINGS_AFTER);
        sUriMatcher.addURI(ReaditContract.AUTHORITY, "listings/" +ReaditContract.PATH_SORT + "/*", CODE_LISTINGS_SORT);
        sUriMatcher.addURI(ReaditContract.AUTHORITY, "listings/" +ReaditContract.PATH_AFTER + "/*/" + ReaditContract.PATH_SORT + "/*", CODE_LISTINGS_SORT_AND_AFTER);
        sUriMatcher.addURI(ReaditContract.AUTHORITY, "comments/" +ReaditContract.PATH_POST_ID + "/*", CODE_COMMENTS);
        sUriMatcher.addURI(ReaditContract.AUTHORITY, "comments", CODE_COMMENTS_ALL);
        sUriMatcher.addURI(ReaditContract.AUTHORITY, "listings", CODE_LISTINGS_ALL);
    }

    @Override
    public boolean onCreate() {
        return false;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        Cursor cursor;
        switch (sUriMatcher.match(uri)){
            case CODE_COMMENTS:
                List<CommentRecord> commentRecords = CommentRecord.listAll(CommentRecord.class);
                MatrixCursor matrixCursor = new MatrixCursor(COMMENT_CURSOR_COLUMNS);
                matrixCursor.addRow(commentRecords);
                return matrixCursor;
            case CODE_COMMENTS_ALL:
                List<CommentRecord> commentRecords1 = CommentRecord.listAll(CommentRecord.class);
                MatrixCursor matrixCursor1 = new MatrixCursor(COMMENT_CURSOR_COLUMNS);
                matrixCursor1.addRow(commentRecords1);
                return matrixCursor1;
            default:
                cursor = db.query(ReaditContract.RedditListingEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
        }
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        Uri retUri;
        switch (sUriMatcher.match(uri)){
            case CODE_COMMENTS:
                CommentRecord commentRecord = CommentRecord.makeCommentRecord(values);
                commentRecord.save();
                retUri = ReaditContract.CommentEntry.CONTENT_URI;
                break;
            case CODE_COMMENTS_ALL:
                CommentRecord commentRecord1 = CommentRecord.makeCommentRecord(values);
                commentRecord1.save();
                retUri = ReaditContract.CommentEntry.CONTENT_URI;
                break;
            default:
                db.insert(ReaditContract.RedditListingEntry.TABLE_NAME, null, values);
                retUri = ReaditContract.RedditListingEntry.CONTENT_URI;
                break;
        }
        return retUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        int retInt = 0;
        switch (sUriMatcher.match(uri)){
            case CODE_COMMENTS:
                retInt = db.delete(ReaditContract.CommentEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case CODE_COMMENTS_ALL:
                retInt = db.delete(ReaditContract.CommentEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                retInt = db.delete(ReaditContract.RedditListingEntry.TABLE_NAME, selection, selectionArgs);
                break;
        }
        return retInt;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }
}
