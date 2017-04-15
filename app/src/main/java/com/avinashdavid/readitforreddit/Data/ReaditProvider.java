package com.avinashdavid.readitforreddit.Data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

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
                cursor = db.query(ReaditContract.CommentEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case CODE_COMMENTS_ALL:
                cursor = db.query(ReaditContract.CommentEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
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
                db.insert(ReaditContract.CommentEntry.TABLE_NAME, null, values);
                retUri = ReaditContract.CommentEntry.CONTENT_URI;
                break;
            case CODE_COMMENTS_ALL:
                db.insert(ReaditContract.CommentEntry.TABLE_NAME, null, values);
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
