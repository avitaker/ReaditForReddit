package com.avinashdavid.readitforreddit.SubredditUtils;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by avinashdavid on 3/8/17.
 */

public class SubredditObject extends RealmObject {
    @PrimaryKey
    String subredditName;

    long timestamp;

    String publicDescription;

    boolean safeForKids;

    String subredditType;

    String title;

    String headerImgUrl;

    String sidebarTextHtml;

    public String getSubredditName() {
        return subredditName;
    }

    public void setSubredditName(String subredditName) {
        this.subredditName = subredditName;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getPublicDescription() {
        return publicDescription;
    }

    public void setPublicDescription(String publicDescription) {
        this.publicDescription = publicDescription;
    }

    public boolean isSafeForKids() {
        return safeForKids;
    }

    public void setSafeForKids(boolean safeForKids) {
        this.safeForKids = safeForKids;
    }

    public String getSubredditType() {
        return subredditType;
    }

    public void setSubredditType(String subredditType) {
        this.subredditType = subredditType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getHeaderImgUrl() {
        return headerImgUrl;
    }

    public void setHeaderImgUrl(String headerImgUrl) {
        this.headerImgUrl = headerImgUrl;
    }
}
