package com.avinashdavid.readitforreddit.Data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.avinashdavid.readitforreddit.PostUtils.CommentRecord;
import com.avinashdavid.readitforreddit.PostUtils.RedditListing;

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
    static final int CODE_COMMENT_ID = 8;
    static final int CODE_LISTING_ID = 9;

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

    public static final String[] LISTING_CURSOR_COLUMNS = new String[]{
            ReaditContract.RedditListingEntry.COLUMN_POST_ID,
            ReaditContract.RedditListingEntry.COLUMN_TIMESTAMP,
            ReaditContract.RedditListingEntry.COLUMN_TITLE,
            ReaditContract.RedditListingEntry.COLUMN_VOTE_COUNT,
            ReaditContract.RedditListingEntry.COLUMN_COMMENTS_COUNT,
            ReaditContract.RedditListingEntry.COLUMN_AUTHOR,
            ReaditContract.RedditListingEntry.COLUMN_SUBREDDIT,
            ReaditContract.RedditListingEntry.COLUMN_TIME_CREATED,
            ReaditContract.RedditListingEntry.COLUMN_SELFTEXT_HTML,
            ReaditContract.RedditListingEntry.COLUMN_DOMAIN,
            ReaditContract.RedditListingEntry.COLUMN_AFTER,
            ReaditContract.RedditListingEntry.COLUMN_URL,
            ReaditContract.RedditListingEntry.COLUMN_THUMBNAIL_URL
    };

    private ReaditDBHelper mReaditDBHelper;
    private SQLiteDatabase db;

    static {
        sUriMatcher.addURI(ReaditContract.AUTHORITY, "*", CODE_LISTINGS_SUBREDDIT);
        sUriMatcher.addURI(ReaditContract.AUTHORITY, "listings/" +ReaditContract.PATH_AFTER + "/*", CODE_LISTINGS_AFTER);
        sUriMatcher.addURI(ReaditContract.AUTHORITY, "listings/" +ReaditContract.PATH_SORT + "/*", CODE_LISTINGS_SORT);
        sUriMatcher.addURI(ReaditContract.AUTHORITY, "listings/" +ReaditContract.PATH_AFTER + "/*/" + ReaditContract.PATH_SORT + "/*", CODE_LISTINGS_SORT_AND_AFTER);
        sUriMatcher.addURI(ReaditContract.AUTHORITY, "listings/" + ReaditContract.PATH_POST_ID +"/*", CODE_LISTING_ID);
        sUriMatcher.addURI(ReaditContract.AUTHORITY, "comments/" +ReaditContract.PATH_POST_ID + "/*", CODE_COMMENTS);
        sUriMatcher.addURI(ReaditContract.AUTHORITY, "comments/" + ReaditContract.PATH_COMMENT_ID + "/*", CODE_COMMENT_ID);
        sUriMatcher.addURI(ReaditContract.AUTHORITY, "comments", CODE_COMMENTS_ALL);
        sUriMatcher.addURI(ReaditContract.AUTHORITY, "listings", CODE_LISTINGS_ALL);
    }

    @Override
    public boolean onCreate() {
        mReaditDBHelper = new ReaditDBHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        db = mReaditDBHelper.getWritableDatabase();
        Cursor cursor;
        String queryId;
        ContentValues cv;
        List<RedditListing> redditListings;
        List<CommentRecord> commentRecords;
        switch (sUriMatcher.match(uri)){
            case CODE_COMMENTS:
                queryId = uri.getPathSegments().get(2);
                commentRecords = CommentRecord.find(CommentRecord.class, "linkId = ?", queryId);
                db.delete(ReaditContract.RedditListingEntry.TABLE_NAME, null, null);
                for (int i=0; i< commentRecords.size(); i++){
                    CommentRecord record = commentRecords.get(i);
                    if (record==null){
                        continue;
                    }
                    cv = CommentRecord.makeContentValues(record);
                    db.insertOrThrow(ReaditContract.CommentEntry.TABLE_NAME, null, cv);
                }
                cursor = db.query(ReaditContract.CommentEntry.TABLE_NAME, COMMENT_CURSOR_COLUMNS, null, null, null, null, null);
                break;
            case CODE_COMMENT_ID:
                queryId = uri.getPathSegments().get(2);
                commentRecords = CommentRecord.find(CommentRecord.class, "commentId = ?", queryId);
                CommentRecord record = commentRecords.get(0);
                if (record==null){
                    return null;
                }
                db.delete(ReaditContract.CommentEntry.TABLE_NAME, null, null);
                cv = CommentRecord.makeContentValues(record);
                db.insertOrThrow(ReaditContract.CommentEntry.TABLE_NAME, null, cv);
                cursor = db.query(ReaditContract.CommentEntry.TABLE_NAME, COMMENT_CURSOR_COLUMNS, null, null, null, null, null);
                break;
            case CODE_COMMENTS_ALL:
                commentRecords = CommentRecord.listAll(CommentRecord.class);
                db.delete(ReaditContract.CommentEntry.TABLE_NAME, null, null);
                for (int i=0; i<commentRecords.size(); i++){
                    CommentRecord commentRecord = commentRecords.get(i);
                    if (commentRecord==null){
                        continue;
                    }
                    cv = CommentRecord.makeContentValues(commentRecord);
                    db.insertOrThrow(ReaditContract.CommentEntry.TABLE_NAME, null, cv);
                }
                cursor = db.query(ReaditContract.CommentEntry.TABLE_NAME, COMMENT_CURSOR_COLUMNS, null, null,null,null,null);
                break;
            case CODE_LISTING_ID:
                queryId = uri.getPathSegments().get(2);
                redditListings = RedditListing.find(RedditListing.class, "mPostId = ?", queryId);
                RedditListing listing = redditListings.get(0);
                if (listing==null){
                    return null;
                }
                db.delete(ReaditContract.RedditListingEntry.TABLE_NAME, null, null);
                cv = RedditListing.makeContentValues(listing);
                db.insertOrThrow(ReaditContract.RedditListingEntry.TABLE_NAME, null, cv);
                cursor = db.query(ReaditContract.RedditListingEntry.TABLE_NAME, LISTING_CURSOR_COLUMNS, null, null, null, null, null);
                break;
            case CODE_LISTINGS_ALL:
                redditListings = RedditListing.listAll(RedditListing.class);
                db.delete(ReaditContract.RedditListingEntry.TABLE_NAME, null, null);
                for (int i=0;i<redditListings.size();i++){
                    RedditListing listing1 = redditListings.get(i);
                    if (listing1 == null){
                        continue;
                    }
                    cv = RedditListing.makeContentValues(listing1);
                    db.insertOrThrow(ReaditContract.RedditListingEntry.TABLE_NAME, null, cv);
                }
                cursor = db.query(ReaditContract.RedditListingEntry.TABLE_NAME, LISTING_CURSOR_COLUMNS, null, null, null, null, null);
                break;
            default:
                cursor = null;
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
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }
}
