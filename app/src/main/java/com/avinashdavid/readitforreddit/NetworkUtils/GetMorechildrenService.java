package com.avinashdavid.readitforreddit.NetworkUtils;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.Nullable;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.avinashdavid.readitforreddit.MiscUtils.Constants;
import com.avinashdavid.readitforreddit.PostUtils.MoreChildrenCommentRecord;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

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
import static com.avinashdavid.readitforreddit.MiscUtils.Constants.PARENT_KEY;
import static com.avinashdavid.readitforreddit.MiscUtils.Constants.REPLIES_KEY;
import static com.avinashdavid.readitforreddit.MiscUtils.Constants.SCORE_HIDDEN_KEY;
import static com.avinashdavid.readitforreddit.MiscUtils.Constants.SCORE_KEY;
import static com.avinashdavid.readitforreddit.MiscUtils.Constants.TIME_CREATED_KEY;
import static com.avinashdavid.readitforreddit.NetworkUtils.GetCommentsService.DEPTH_MORE;

/**
 * Created by avinashdavid on 4/25/17.
 */

public class GetMorechildrenService extends IntentService {
    private static final String EXTRA_LINK = "extraLink";

    public static final String KEY_PARENT_ID = "extraParentId";
    public static final String KEY_LINK_ID = "linkId";
    public static final String KEY_INSERT_START_POSITION  = "insertStart";
    public static final String KEY_ITEMS_INSERTED = "itemsInserted";

    private static boolean isLoading = false;
    private static int commentsAdded =0;

    public GetMorechildrenService(){
        super(GetMorechildrenService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        final Context context = GetMorechildrenService.this.getApplicationContext();
        final Uri mUrl = (Uri)intent.getParcelableExtra(EXTRA_LINK);
        final String mParentId = intent.getStringExtra(KEY_PARENT_ID);
        final String linkId = intent.getStringExtra(KEY_LINK_ID);
        final int startPosition = intent.getIntExtra(KEY_INSERT_START_POSITION, Integer.MAX_VALUE);
        isLoading = true;
        commentsAdded = 0;

        JsonObjectRequest jsonArrayRequest = new JsonObjectRequest
                (Request.Method.GET, mUrl.toString(), null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        MoreChildrenCommentRecord.deleteAll(MoreChildrenCommentRecord.class);
                        try {
                            final ArrayList<JSONObject> parentReplyJsonObjects = new ArrayList<>();
                            JSONArray childrenJsonArray = response.getJSONObject("json").getJSONObject(DATA_KEY).getJSONArray("things");
                            parentReplyJsonObjects.addAll(getCommentDataJsonObjectsFromChildrenJsonArray(childrenJsonArray, mParentId));
                            AsyncTask asyncTask = new AsyncTask() {
                                @Override
                                protected Object doInBackground(Object[] params) {
                                    makeCommentObjectsFromJsonObjects(context, parentReplyJsonObjects, linkId);
                                    return true;
                                }

                                @Override
                                protected void onPostExecute(Object o) {
                                    super.onPostExecute(o);
                                    Intent intent = new Intent();
                                    intent.setAction(Constants.BROADCAST_MORE_COMMENTS_LOADED);
                                    GetMorechildrenService.this.sendBroadcast(intent);
                                }
                            };
                            asyncTask.execute(0);
                        }
                        catch (Exception e){
                            Timber.e(e);
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Timber.e(error, "error while retreiving more comments data");
                        Intent errorIntent = new Intent(Constants.BROADCAST_MORE_COMMENTS_ERROR);
                        context.sendBroadcast(errorIntent);
                    }
                });

        NetworkSingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonArrayRequest);
    }

    public static void loadMoreComments(Context context, String linkId, String parentId, String children, int startPosition){
        Uri url = UriGenerator.getUriMoreComments(linkId, children);
        Timber.e(url.toString());
        Intent intent = new Intent(context, GetMorechildrenService.class);
        intent.putExtra(GetMorechildrenService.EXTRA_LINK, url);
        intent.putExtra(GetMorechildrenService.KEY_PARENT_ID, parentId);
        intent.putExtra(GetMorechildrenService.KEY_LINK_ID, linkId);
        intent.putExtra(GetMorechildrenService.KEY_INSERT_START_POSITION, startPosition);
        context.startService(intent);
    }

    private static ArrayList<JSONObject> getCommentDataJsonObjectsFromChildrenJsonArray(JSONArray jsonArray, String parentId){
        ArrayList<JSONObject> jsonObjects = new ArrayList<>();
        try {
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String kind = jsonObject.getString(KIND_KEY);
                if (kind.equals(Constants.COMMENT_KIND)) {
                    JSONObject replyData = jsonObject.getJSONObject(DATA_KEY);
                    if (replyData.getString(PARENT_KEY).equals(parentId)) {
                        jsonObjects.add(replyData);
                    }
                }
            }
        } catch (Exception e){
            Timber.e(e, "error in getChildrenCommentObjectsFromParentJSONArray");
        }
        return jsonObjects;
    }

    public static void makeCommentObjectsFromJsonObjects(Context context, ArrayList<JSONObject> jsonObjects, String linkId){
        for (int i = 0; i < jsonObjects.size(); i++){
            JSONObject currentJsonObj = jsonObjects.get(i);
            MoreChildrenCommentRecord commentObject = getMoreChildrenCommentObjectsFromJsonDataObject(currentJsonObj, linkId);
            if (commentObject.depth!=DEPTH_MORE) {
                commentObject.save();
                commentsAdded++;
            }

            if (commentObject.hasReplies){
                ArrayList<JSONObject> childObjects = GetCommentsService.getRepliesJsonObjectsFromCommentDataObj(currentJsonObj);
                makeCommentObjectsFromJsonObjects(context, childObjects, linkId);
            }

            if (commentObject.depth==DEPTH_MORE){
                commentObject.save();
                commentsAdded++;
            }
        }
    }

    public static MoreChildrenCommentRecord getMoreChildrenCommentObjectsFromJsonDataObject(JSONObject replyData, String linkId){
        MoreChildrenCommentRecord commentObject = null;
        int moreCount = 0;
        try {
            try {
                moreCount = replyData.getInt(COUNT_KEY);
                String id = replyData.getString(ID_KEY);
                String author = "";
                String bodyRaw = replyData.getJSONArray(CHILDREN_KEY).toString().substring(1);
                String body = bodyRaw.substring(0, bodyRaw.length()-1).replace("\"","");
                Timber.d("more: " + body);
                float timecreated = (float)replyData.getInt(DEPTH_KEY);
                String parent = replyData.getString(PARENT_KEY);
                boolean hasReplies = false;
                String authorflair = "";

                commentObject = new MoreChildrenCommentRecord(System.currentTimeMillis(), id, linkId, false, moreCount, author, body, parent, timecreated, DEPTH_MORE, false, authorflair, false, false);
                return commentObject;
            } catch (Exception e){
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

                commentObject = new MoreChildrenCommentRecord(System.currentTimeMillis(), id, linkId, scoreHidden, score, author, body, parent, timecreated, depth, hasReplies, authorflair, isGilded, isEdited);
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
//            commentObject = new CommentRecord(System.currentTimeMillis(), id, linkId, scoreHidden, score, author, body, parent, timecreated, depth, hasReplies, authorflair, isGilded, isEdited);
//            return commentObject;
        } catch (Exception e){
            Timber.e(e, "error in getMoreChildrenCommentObjectsFromJsonDataObject");
        }
        return null;
    }
}
