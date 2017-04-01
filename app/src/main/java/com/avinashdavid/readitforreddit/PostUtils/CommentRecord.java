package com.avinashdavid.readitforreddit.PostUtils;

import com.orm.SugarRecord;


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
}
