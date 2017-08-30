package com.avinashdavid.readitforreddit.PostUtils;

import android.content.ContentValues;
import android.support.annotation.NonNull;

import com.avinashdavid.readitforreddit.Data.ReaditContract;
import com.orm.SugarRecord;


/**
 * Created by avinashdavid on 3/18/17.
 * This class extends ORM storage object, and contains all the relevant information for a comment
 */

public class CommentRecord extends SugarRecord {

    public long timestamp;
    public String commentId;

    public String linkId;

    public boolean scoreHidden;

    public int score;

    public String author;

    public String bodyHtml;

    public String parent;

    public long createdTime;

    public int depth;

    public boolean hasReplies;
    public String authorFlairText;

    public boolean isGilded;
    public boolean isEdited;

    public CommentRecord() {
    }

    public CommentRecord(long timestamp, String commentId, String linkId, boolean scoreHidden, int score, String author, String bodyHtml, String parent, long createdTime, int depth, boolean hasReplies, String authorFlairText, boolean isGilded, boolean isEdited) {
        this.timestamp = timestamp;
        this.commentId = commentId;
        this.linkId = linkId;
        this.scoreHidden = scoreHidden;
        this.score = score;
        this.author = author;
        this.bodyHtml = bodyHtml;
        this.parent = parent;
        this.createdTime = createdTime;
        this.depth = depth;
        this.hasReplies = hasReplies;
        this.authorFlairText = authorFlairText;
        this.isGilded = isGilded;
        this.isEdited = isEdited;
    }

    public static ContentValues makeContentValues(@NonNull CommentRecord commentRecord){
        ContentValues cv = new ContentValues();
        cv.put(ReaditContract.CommentEntry.COLUMN_COMMENT_ID, commentRecord.commentId);
        cv.put(ReaditContract.CommentEntry.COLUMN_TIMESTAMP, commentRecord.timestamp);
        cv.put(ReaditContract.CommentEntry.COLUMN_POST_ID, commentRecord.linkId);
        cv.put(ReaditContract.CommentEntry.COLUMN_SCORE_HIDDEN, commentRecord.scoreHidden);
        cv.put(ReaditContract.CommentEntry.COLUMN_SCORE, commentRecord.score);
        cv.put(ReaditContract.CommentEntry.COLUMN_COMMENT_AUTHOR, commentRecord.author);
        cv.put(ReaditContract.CommentEntry.COLUMN_BODY_HTML, commentRecord.bodyHtml);
        cv.put(ReaditContract.CommentEntry.COLUMN_PARENT, commentRecord.parent);
        cv.put(ReaditContract.CommentEntry.COLUMN_TIME_CREATED, commentRecord.createdTime);
        cv.put(ReaditContract.CommentEntry.COLUMN_DEPTH, commentRecord.depth);
        cv.put(ReaditContract.CommentEntry.COLUMN_HAS_REPLIES, commentRecord.hasReplies);
        cv.put(ReaditContract.CommentEntry.COLUMN_AUTHOR_FLAIR_TEXT, commentRecord.authorFlairText);
        return cv;
    }

//    public static CommentRecord makeCommentRecord(@NonNull ContentValues cv){
//        String id = cv.getAsString(ReaditContract.CommentEntry.COLUMN_COMMENT_ID);
//        long created_utc = cv.getAsLong(ReaditContract.CommentEntry.COLUMN_TIMESTAMP);
//        String postId = cv.getAsString(ReaditContract.CommentEntry.COLUMN_POST_ID);
//        boolean scoreHidden = cv.getAsBoolean(ReaditContract.CommentEntry.COLUMN_SCORE_HIDDEN);
//        int score = cv.getAsInteger(ReaditContract.CommentEntry.COLUMN_SCORE);
//        String author = cv.getAsString(ReaditContract.CommentEntry.COLUMN_COMMENT_AUTHOR);
//        String bodyHtml = cv.getAsString(ReaditContract.CommentEntry.COLUMN_BODY_HTML);
//        String parent = cv.getAsString(ReaditContract.CommentEntry.COLUMN_PARENT);
//        float timeCreated = cv.getAsFloat(ReaditContract.CommentEntry.COLUMN_TIME_CREATED);
//        int depth = cv.getAsInteger(ReaditContract.CommentEntry.COLUMN_DEPTH);
//        boolean hasReplies = cv.getAsBoolean(ReaditContract.CommentEntry.COLUMN_HAS_REPLIES);
//        String flairText = cv.getAsString(ReaditContract.CommentEntry.COLUMN_AUTHOR_FLAIR_TEXT);
//        return new CommentRecord(created_utc, id, postId, scoreHidden, score, author, bodyHtml, parent, timeCreated, depth, hasReplies, flairText);
//    }
//
//    public static CommentRecord makeCommentRecord(@NonNull Cursor c){
//        String id = c.getString(ReaditProvider.COLUMN_COMMENT_ID);
//        long created_utc = c.getLong(ReaditProvider.COLUMN_TIMESTAMP);
//        String postId = c.getString(ReaditProvider.COLUMN_POST_ID);
//        int scoreHiddenInt = c.getInt(ReaditProvider.COLUMN_SCORE_HIDDEN);
//        boolean scoreHidden = scoreHiddenInt!=0;
//        int score = c.getInt(ReaditProvider.COLUMN_SCORE);
//        String author = c.getString(ReaditProvider.COLUMN_COMMENT_AUTHOR);
//        String bodyHtml = c.getString(ReaditProvider.COLUMN_BODY_HTML);
//        String parent = c.getString(ReaditProvider.COLUMN_PARENT);
//        float timeCreated = c.getFloat(ReaditProvider.COLUMN_TIME_CREATED);
//        int depth = c.getInt(ReaditProvider.COLUMN_DEPTH);
//        int hasRepliesInt = c.getInt(ReaditProvider.COLUMN_HAS_REPLIES);
//        boolean hasReplies = hasRepliesInt!=0;
//        String flairText = c.getString(ReaditProvider.COLUMN_AUTHOR_FLAIR_TEXT);
//        return new CommentRecord(created_utc, id, postId, scoreHidden, score, author, bodyHtml, parent, timeCreated, depth, hasReplies, flairText);
//    }
}
