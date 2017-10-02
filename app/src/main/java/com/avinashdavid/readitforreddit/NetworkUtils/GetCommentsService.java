package com.avinashdavid.readitforreddit.NetworkUtils;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.avinashdavid.readitforreddit.MiscUtils.Constants;
import com.avinashdavid.readitforreddit.OAuth.GetUserAuthService;
import com.avinashdavid.readitforreddit.PostUtils.CommentRecord;
import com.avinashdavid.readitforreddit.PostUtils.RedditListing;
import com.avinashdavid.readitforreddit.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

import static com.avinashdavid.readitforreddit.MiscUtils.Constants.AUTHOR_KEY;
import static com.avinashdavid.readitforreddit.MiscUtils.Constants.BODY_HTML;
import static com.avinashdavid.readitforreddit.MiscUtils.Constants.CHILDREN_KEY;
import static com.avinashdavid.readitforreddit.MiscUtils.Constants.COUNT_KEY;
import static com.avinashdavid.readitforreddit.MiscUtils.Constants.DATA_KEY;
import static com.avinashdavid.readitforreddit.MiscUtils.Constants.DEPTH_KEY;
import static com.avinashdavid.readitforreddit.MiscUtils.Constants.EDITED;
import static com.avinashdavid.readitforreddit.MiscUtils.Constants.GILDED;
import static com.avinashdavid.readitforreddit.MiscUtils.Constants.ID_KEY;
import static com.avinashdavid.readitforreddit.MiscUtils.Constants.KEY_AUTHOR_FLAIR;
import static com.avinashdavid.readitforreddit.MiscUtils.Constants.KIND_KEY;
import static com.avinashdavid.readitforreddit.MiscUtils.Constants.MORE_KIND;
import static com.avinashdavid.readitforreddit.MiscUtils.Constants.PARENT_KEY;
import static com.avinashdavid.readitforreddit.MiscUtils.Constants.REPLIES_KEY;
import static com.avinashdavid.readitforreddit.MiscUtils.Constants.SCORE_HIDDEN_KEY;
import static com.avinashdavid.readitforreddit.MiscUtils.Constants.SCORE_KEY;
import static com.avinashdavid.readitforreddit.MiscUtils.Constants.TIME_CREATED_KEY;

/**
 * Created by avinashdavid on 3/8/17.
 * This service gets and stores comments for an article as CommentRecord objects, or notifies if an error occurs via broadcast intent
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

    public static void loadCommentsForArticle(Context context, @Nullable String subreddit, String articleId, @Nullable String sort){
        Intent intent = new Intent(context, GetCommentsService.class);
        Uri url = UriGenerator.getUriCommentsForArticle(subreddit, articleId, sort);
        intent.putExtra(GetCommentsService.EXTRA_URL, url);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        String key = context.getString(R.string.pref_last_post);
        sp.edit().putString(key, articleId).apply();
        context.startService(intent);
    }

    public static void loadCommentsInThread(Context context, @NonNull String postId, @NonNull String commentId, int numberOfParents) {
        Intent intent = new Intent(context, GetCommentsService.class);
        Uri url = UriGenerator.getUriCommentThread(postId, commentId, numberOfParents);
        intent.putExtra(GetCommentsService.EXTRA_URL, url);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context.getApplicationContext());
        sp.edit().putString(context.getString(R.string.pref_last_comment_thread), commentId).apply();
        context.startService(intent);
    }

    public GetCommentsService() {
        super(GetCommentsService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mUrl = intent.getParcelableExtra(EXTRA_URL);
        mSort = intent.getStringExtra(EXTRA_SORT);
        final String mAuthToken = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext()).getString(GetUserAuthService.PREF_NAME_ACCESS_TOKEN, null);
        Timber.d("AUTHORITY: " + mUrl.getAuthority());

        if (mAuthToken!=null && !mAuthToken.equals("")) {
            mUrl = mUrl.buildUpon().authority("oauth.reddit.com").build();
            Timber.d(mUrl.toString());
        }

        Timber.d("COMMENTS LINK:  " + mUrl.toString());

        if (null == mSort){
            mSort = getResources().getStringArray(R.array.sort_listing_options)[0];
        }
        final Context context = GetCommentsService.this.getApplicationContext();
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest
                (Request.Method.GET, mUrl.toString(), null, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        CommentRecord.deleteAll(CommentRecord.class);
                        try {
                            final ArrayList<JSONObject> parentReplyJsonObjects = new ArrayList<>();
                            final RedditListing listing = new RedditListing(response.getJSONObject(0).getJSONObject(DATA_KEY).getJSONArray(CHILDREN_KEY).getJSONObject(0).getJSONObject(DATA_KEY));
                            final String linkId = listing.mPostId;
                            JSONArray childrenJsonArray = response.getJSONObject(1).getJSONObject(DATA_KEY).getJSONArray(CHILDREN_KEY);
                            parentReplyJsonObjects.addAll(getCommentDataJsonObjectsFromChildrenJsonArray(childrenJsonArray));
                            AsyncTask asyncTask = new AsyncTask() {
                                @Override
                                protected Object doInBackground(Object[] params) {
                                    if(RedditListing.find(RedditListing.class, "m_post_id = ?", linkId).size()  < 1) {
                                        listing.save();
                                    }
                                    makeCommentObjectsFromJsonObjects(context, parentReplyJsonObjects, linkId);
                                    return true;
                                }

                                @Override
                                protected void onPostExecute(Object o) {
                                    super.onPostExecute(o);
                                    Intent intent = new Intent();
                                    intent.setAction(Constants.BROADCAST_COMMENTS_LOADED);
                                    GetCommentsService.this.sendBroadcast(intent);
                                }
                            };
                            asyncTask.execute(0);
//                            Intent intent = new Intent();
//                            intent.setAction(Constants.BROADCAST_COMMENTS_LOADED);
//                            GetCommentsService.this.sendBroadcast(intent);
                        }
                        catch (Exception e){
                            Timber.e(e);
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String message = error.getMessage();
                        Timber.e(message);
                        Intent errorIntent = new Intent(Constants.BROADCAST_ERROR_WHILE_LOADING_COMMENTS);
                        errorIntent.putExtra(Constants.KEY_NETWORK_REQUEST_ERROR, message);
                        context.sendBroadcast(errorIntent);
                    }

                    
                }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("grant_type", "client_credentials");
                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();

                if (mAuthToken != null) {
                    headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
                    headers.put("Authorization", "bearer " + mAuthToken);
                }
                return headers;
            }
        };

        NetworkSingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonArrayRequest);
    }

    public static void makeCommentObjectsFromJsonObjects(Context context, ArrayList<JSONObject> jsonObjects, String linkId){
        for (int i = 0; i < jsonObjects.size(); i++){
            JSONObject currentJsonObj = jsonObjects.get(i);
            CommentRecord commentObject = getChildrenCommentObjectsFromJsonDataObject(currentJsonObj, linkId);
            if (commentObject == null) break;
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

    public static ArrayList<JSONObject> getRepliesJsonObjectsFromCommentDataObj(JSONObject commentDataObj){
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
                if (kind.equals(Constants.COMMENT_KIND) || kind.equals(MORE_KIND)) {
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
    public static CommentRecord getChildrenCommentObjectsFromJsonDataObject(JSONObject replyData, String linkId){
        CommentRecord commentObject = null;
        int moreCount = 0;
        try {
            try {
                moreCount = replyData.getInt(COUNT_KEY);
                String id = replyData.getString(ID_KEY);
                String author = "";
                String bodyRaw = replyData.getJSONArray(CHILDREN_KEY).toString().substring(1);
                String body = bodyRaw.substring(0, bodyRaw.length()-1).replace("\"","");
                long timecreated = replyData.getInt(DEPTH_KEY);
                String parent = replyData.getString(PARENT_KEY);
                Object isLikedValue = replyData.get("likes");
                boolean isLiked = isLikedValue != null;
                boolean hasReplies = false;
                String authorflair = "";

                commentObject = new CommentRecord(System.currentTimeMillis(), id, linkId, false, moreCount, author, body, parent, timecreated, DEPTH_MORE, false, authorflair, false, false, isLiked);
                return commentObject;
            } catch (Exception e){
                String id = replyData.getString(ID_KEY);
                boolean scoreHidden = replyData.getBoolean(SCORE_HIDDEN_KEY);
                int score = replyData.getInt(SCORE_KEY);
                String author = replyData.getString(AUTHOR_KEY);
                String body = replyData.getString(BODY_HTML);
                long timecreated = replyData.getLong(TIME_CREATED_KEY);
                String parent = replyData.getString(PARENT_KEY);
                int depth = replyData.getInt(DEPTH_KEY);
                boolean hasReplies = !replyData.get(REPLIES_KEY).toString().equals("");
                String authorflair = replyData.getString(KEY_AUTHOR_FLAIR);
                boolean isGilded = replyData.getInt(GILDED) > 0;
                boolean isEdited = !replyData.getString(EDITED).equals("false");

                Object isLikedValue = replyData.get("likes");
                boolean isLiked = isLikedValue != null;

                commentObject = new CommentRecord(System.currentTimeMillis(), id, linkId, scoreHidden, score, author, body, parent, timecreated, depth, hasReplies, authorflair, isGilded, isEdited, isLiked);
                return commentObject;
            }
//            String id = replyData.getString(ID_KEY);
//            boolean scoreHidden = replyData.getBoolean(SCORE_HIDDEN_KEY);
//            int score = replyData.getInt(SCORE_KEY);
//            String author = replyData.getString(AUTHOR_KEY);
//            String body = replyData.getString(BODY_HTML);
//            float timecreated = (float) replyData.getLong(TIME_CREATED_KEY);
//            String parent = replyData.getString(PARENT_KEY);
//            int depth = replyData.getInt(DEPTH_KEY);
//            boolean hasReplies = !replyData.get(REPLIES_KEY).toString().equals("");
//            String authorflair = replyData.getString(KEY_AUTHOR_FLAIR);
//            boolean isGilded = replyData.getInt(GILDED) > 0;
//            boolean isEdited = !replyData.getString(EDITED).equals("false");
//
//            commentObject = new CommentRecord(System.currentTimeMillis(), id, link_id, scoreHidden, score, author, body, parent, timecreated, depth, hasReplies, authorflair, isGilded, isEdited);
//            return commentObject;
        } catch (Exception e){
            Timber.e(e, "error in getMoreChildrenCommentObjectsFromJsonDataObject");
        }
        return null;
    }
}
