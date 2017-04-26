package com.avinashdavid.readitforreddit.NetworkUtils;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.avinashdavid.readitforreddit.MiscUtils.Constants;
import com.avinashdavid.readitforreddit.PostUtils.CommentRecord;
import com.avinashdavid.readitforreddit.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import timber.log.Timber;

/**
 * Created by avinashdavid on 3/8/17.
 */

public class GetCommentsService extends IntentService {
//    private Realm mRealm;

    public static String sLastPostId = null;

    private Uri mUrl;
    private String mSort;

    private static int COMMENT_INDEX = 0;

    public static final int DEPTH_MORE = -5;

    public static final String EXTRA_URL = "extraUrlParcelableComments";
    public static final String EXTRA_SORT = "extraSort";

    private static final String KIND_KEY = "kind";
    private static final String LISTING_KIND = "Listing";
    private static final String DATA_KEY = "data";
    private static final String CHILDREN_KEY = "children";
    private static final String DEPTH_KEY = "depth";
    private static final String PARENT_KEY = "parent_id";
    private static final String LINK_KIND = "t3";
    private static final String COMMENT_KIND = "t1";
    private static final String ID_KEY = "id";
    private static final String SCORE_HIDDEN_KEY = "score_hidden";
    private static final String SCORE_KEY = "score";
    private static final String BODY_HTML = "body_html";
    private static final String AUTHOR_KEY = "author";
    private static final String REPLIES_KEY = "replies";
    private static final String TIME_CREATED_KEY = "created_utc";
    private static final String KEY_AUTHOR_FLAIR = "author_flair_text";
    private static final String GILDED = "gilded";
    private static final String EDITED = "edited";

    private static final String MORE_KIND = "more";
    private static final String COUNT_KEY = "count";


    public GetCommentsService() {
        super(GetCommentsService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mUrl = intent.getParcelableExtra(EXTRA_URL);
        mSort = intent.getStringExtra(EXTRA_SORT);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String key = getString(R.string.pref_last_post);

        if (null == mSort){
            mSort = getResources().getStringArray(R.array.sort_listing_options)[0];
        }
        Timber.d(mUrl.toString());
        final Context context = GetCommentsService.this.getApplicationContext();
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest
                (Request.Method.GET, mUrl.toString(), null, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        CommentRecord.deleteAll(CommentRecord.class);
                        try {
                            final ArrayList<JSONObject> parentReplyJsonObjects = new ArrayList<>();
                            final String linkId = response.getJSONObject(0).getJSONObject(DATA_KEY).getJSONArray(CHILDREN_KEY).getJSONObject(0).getJSONObject(DATA_KEY).getString(ID_KEY);
                            JSONArray childrenJsonArray = response.getJSONObject(1).getJSONObject(DATA_KEY).getJSONArray(CHILDREN_KEY);
                            parentReplyJsonObjects.addAll(getCommentDataJsonObjectsFromChildrenJsonArray(childrenJsonArray));
                            AsyncTask asyncTask = new AsyncTask() {
                                @Override
                                protected Object doInBackground(Object[] params) {
                                    makeCommentObjectsFromJsonObjects(context, parentReplyJsonObjects, linkId);
                                    return true;
                                }
                            };
                            asyncTask.execute(0);
                            Intent intent = new Intent();
                            intent.setAction(Constants.BROADCAST_COMMENTS_LOADED);
                            GetCommentsService.this.sendBroadcast(intent);
                        }
                        catch (Exception e){
                            Timber.d(e);
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub
                        Timber.e("error while retreiving comments data");
                        Intent errorIntent = new Intent(Constants.BROADCAST_ERROR_WHILE_LOADING_COMMENTS);
                        context.sendBroadcast(errorIntent);
                    }
                });

        NetworkSingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonArrayRequest);
    }

    public static void loadCommentsForArticle(Context context, @Nullable String subreddit, String articleId, @Nullable String sort){
        Intent intent = new Intent(context, GetCommentsService.class);
        Uri url = UriGenerator.getUriCommentsForArticle(subreddit, articleId, sort);
        intent.putExtra(GetCommentsService.EXTRA_URL, url);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        String key = context.getString(R.string.pref_last_post);
        sp.edit().putString(key, articleId).apply();
        context.startService(intent);
    }

    private static void makeCommentObjectsFromJsonObjects(Context context, ArrayList<JSONObject> jsonObjects, String linkId){
        for (int i = 0; i < jsonObjects.size(); i++){
            JSONObject currentJsonObj = jsonObjects.get(i);
            CommentRecord commentObject = getChildrenCommentObjectsFromJsonDataObject(currentJsonObj, linkId);
            if (commentObject.depth!=DEPTH_MORE) {
                commentObject.save();
            }

            if (commentObject.hasReplies){
                ArrayList<JSONObject> childObjects = getRepliesJsonObjectsFromCommentDataObj(currentJsonObj);
                makeCommentObjectsFromJsonObjects(context, childObjects, linkId);
            }

            if (commentObject.depth==DEPTH_MORE){
                commentObject.save();
            }
        }
    }

    private static ArrayList<JSONObject> getRepliesJsonObjectsFromCommentDataObj(JSONObject commentDataObj){
        ArrayList<JSONObject> list = new ArrayList<>();
        try {
            JSONArray repliesArray = commentDataObj.getJSONObject(REPLIES_KEY).getJSONObject(DATA_KEY).getJSONArray(CHILDREN_KEY);
            list.addAll(getCommentDataJsonObjectsFromChildrenJsonArray(repliesArray));
        } catch (Exception e){
            Timber.e(e, "getRepliesJsonObjectsFromCommentDataObj");
        }
        return list;
    }

    private static ArrayList<JSONObject> getCommentDataJsonObjectsFromChildrenJsonArray(JSONArray jsonArray){
        ArrayList<JSONObject> jsonObjects = new ArrayList<>();
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String kind = jsonObject.getString(KIND_KEY);
                if (kind.equals(COMMENT_KIND)) {
                    JSONObject replyData = jsonObject.getJSONObject(DATA_KEY);
                    jsonObjects.add(replyData);
                }
            }
        } catch (Exception e){
            Timber.e(e, "error in getChildrenCommentObjectsFromParentJSONArray");
        }
        return jsonObjects;
    }

    /**
     * To make things easier as far as view types in the recyclerview, a little shortcut has been taken here
     * The "more" objects are also being saved as comment objects, but with following caveats
     *  - the number of comments in the more jsonobject is being saved as the commentobject's score
     *  - the commentobject's createdTime is set as a negative value (this will be used to distinguish it as a more object
     *  - the more object's depth is stored as is. This way, it will be displayed as the correct depth in the recyclerview
     */
    private static CommentRecord getChildrenCommentObjectsFromJsonDataObject(JSONObject replyData, String linkId){
        CommentRecord commentObject = null;
        int moreCount = 0;
        try {
//            try {
//                moreCount = replyData.getInt(COUNT_KEY);
//                String id = replyData.getString(ID_KEY);
//                String author = "";
//                String body = "";
//                float timecreated = DEPTH_MORE;
//                String parent = replyData.getString(PARENT_KEY);
//                int depth = replyData.getInt(DEPTH_KEY);
//                boolean hasReplies = false;
//                String authorflair = "";
//
//                commentObject = new CommentRecord(System.currentTimeMillis(), id, linkId, false, moreCount, author, body, parent, timecreated, depth, false, authorflair, false, false);
//                return commentObject;
//            } catch (Exception e){
//                String id = replyData.getString(ID_KEY);
//                boolean scoreHidden = replyData.getBoolean(SCORE_HIDDEN_KEY);
//                int score = replyData.getInt(SCORE_KEY);
//                String author = replyData.getString(AUTHOR_KEY);
//                String body = replyData.getString(BODY_HTML);
//                float timecreated = (float) replyData.getLong(TIME_CREATED_KEY);
//                String parent = replyData.getString(PARENT_KEY);
//                int depth = replyData.getInt(DEPTH_KEY);
//                boolean hasReplies = !replyData.get(REPLIES_KEY).toString().equals("");
//                String authorflair = replyData.getString(KEY_AUTHOR_FLAIR);
//                boolean isGilded = replyData.getInt(GILDED) > 0;
//                boolean isEdited = !replyData.getString(EDITED).equals("false");
//
//                commentObject = new CommentRecord(System.currentTimeMillis(), id, linkId, scoreHidden, score, author, body, parent, timecreated, depth, hasReplies, authorflair, isGilded, isEdited);
//                return commentObject;
//            }
            String id = replyData.getString(ID_KEY);
            boolean scoreHidden = replyData.getBoolean(SCORE_HIDDEN_KEY);
            int score = replyData.getInt(SCORE_KEY);
            String author = replyData.getString(AUTHOR_KEY);
            String body = replyData.getString(BODY_HTML);
            float timecreated = (float) replyData.getLong(TIME_CREATED_KEY);
            String parent = replyData.getString(PARENT_KEY);
            int depth = replyData.getInt(DEPTH_KEY);
            boolean hasReplies = !replyData.get(REPLIES_KEY).toString().equals("");
            String authorflair = replyData.getString(KEY_AUTHOR_FLAIR);
            boolean isGilded = replyData.getInt(GILDED) > 0;
            boolean isEdited = !replyData.getString(EDITED).equals("false");

            commentObject = new CommentRecord(System.currentTimeMillis(), id, linkId, scoreHidden, score, author, body, parent, timecreated, depth, hasReplies, authorflair, isGilded, isEdited);
            return commentObject;
        } catch (Exception e){
            Timber.e(e, "error in getChildrenCommentObjectsFromJsonDataObject");
        }
        return null;
    }
}
