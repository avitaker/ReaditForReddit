package com.avinashdavid.readitforreddit.MiscUtils;

import com.avinashdavid.readitforreddit.PostUtils.CommentObject;
import com.avinashdavid.readitforreddit.PostUtils.RedditPost;
import com.avinashdavid.readitforreddit.SubredditUtils.SubredditObject;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import timber.log.Timber;

/**
 * Created by avinashdavid on 3/8/17.
 */

public class RealmDataUtils {
    /**
     * Comment retrieval constants
     */
    private static final String KIND_KEY = "kind";
    private static final String LISTING_KIND = "Listing";
    private static final String DATA_KEY = "data";
    private static final String CHILDREN_KEY = "children";
    private static final String DEPTH_KEY = "depth";
    private static final String PARENT_KEY = "parent_id";
    private static final String LINK_KIND = "t3";
    private static final String MORE_KIND = "more";
    private static final String COMMENT_KIND = "t1";
    private static final String ID_KEY = "id";
    private static final String SCORE_HIDDEN_KEY = "score_hidden";
    private static final String SCORE_KEY = "score";
    private static final String BODY_KEY = "body";
    private static final String AUTHOR_KEY = "author";
    private static final String REPLIES_KEY = "replies";
    private static final String TIME_CREATED_KEY = "created_utc";

    static int COMMENT_INDEX = 0;

    public static boolean deleteListings(Realm realm){
        final RealmResults<RedditPost> realmResults = realm.where(RedditPost.class).findAll();
        final boolean[] toReturn = new boolean[1];
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                toReturn[0] = realmResults.deleteAllFromRealm();
            }
        });
        return toReturn[0];
    }

    public static boolean deleteComments(Realm realm){
        final RealmResults<CommentObject> realmResults = realm.where(CommentObject.class).findAll();
        final boolean[] toReturn = new boolean[1];
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                toReturn[0] = realmResults.deleteAllFromRealm();
            }
        });
        return toReturn[0];
    }

    public static boolean deleteSubreddits(Realm realm){
        final RealmResults<SubredditObject> realmResults = realm.where(SubredditObject.class).findAll();
        final boolean[] toReturn = new boolean[1];
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                toReturn[0] = realmResults.deleteAllFromRealm();
            }
        });
        return toReturn[0];
    }


}
