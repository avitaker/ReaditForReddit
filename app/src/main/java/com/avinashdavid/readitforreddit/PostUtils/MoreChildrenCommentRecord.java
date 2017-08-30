package com.avinashdavid.readitforreddit.PostUtils;

import com.orm.SugarRecord;

/**
 * Created by avinashdavid on 5/4/17.
 * This class extends ORM storage object, and contains all the relevant information for a comment in a "more" endpoint
 * It is modeled after CommentRecord, and allows for easier distinction of main thread comments and those from hidden threads
 */

public class MoreChildrenCommentRecord extends SugarRecord{
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

    public MoreChildrenCommentRecord() {
    }

    public MoreChildrenCommentRecord(long timestamp, String commentId, String linkId, boolean scoreHidden, int score, String author, String bodyHtml, String parent, long createdTime, int depth, boolean hasReplies, String authorFlairText, boolean isGilded, boolean isEdited) {
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
}
