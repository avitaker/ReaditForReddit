package com.avinashdavid.readitforreddit.PostUtils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;

import com.avinashdavid.readitforreddit.Data.ReaditContract;
import com.orm.SugarRecord;
import com.orm.util.NamingHelper;
import com.orm.util.SugarCursorFactory;

import static com.orm.SugarContext.getSugarContext;


/**
 * Created by avinashdavid on 3/18/17.
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

    public float createdTime;

    public int depth;

    public boolean hasReplies;
    public String authorFlairText;

    public CommentRecord() {
    }

    public CommentRecord(long timestamp, String commentId, String linkId, boolean scoreHidden, int score, String author, String bodyHtml, String parent, float createdTime, int depth, boolean hasReplies, String authorFlairText) {
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
}
