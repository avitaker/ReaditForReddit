package com.avinashdavid.readitforreddit.PostUtils;

import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

/**
 * Created by avinashdavid on 3/7/17.
 */

public class CommentObject extends RealmObject {

    @Index
    private long timestamp;

    @PrimaryKey
    private String commentId;

    private String linkId;

    private boolean scoreHidden;

    private int score;

    private String author;

    private String bodyHtml;

    private String parent;

    private float createdTime;

    private int depth;

    private boolean hasReplies;
    private String authorFlairText;

    public String getCommentId() {
        return commentId;
    }

    public void setCommentId(String commentId) {
        this.commentId = commentId;
    }

    public boolean isScoreHidden() {
        return scoreHidden;
    }

    public void setScoreHidden(boolean scoreHidden) {
        this.scoreHidden = scoreHidden;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getBodyHtml() {
        return bodyHtml;
    }

    public void setBodyHtml(String bodyHtml) {
        this.bodyHtml = bodyHtml;
    }

    public String getLinkId() {
        return linkId;
    }

    public void setLinkId(String linkId) {
        this.linkId = linkId;
    }


    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public float getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(float createdTime) {
        this.createdTime = createdTime;
    }


    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public boolean isHasReplies() {
        return hasReplies;
    }

    public void setHasReplies(boolean hasReplies) {
        this.hasReplies = hasReplies;
    }

    public String getAuthorFlairText() {
        return authorFlairText;
    }

    public void setAuthorFlairText(String authorFlairText) {
        this.authorFlairText = authorFlairText;
    }
}
