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
import com.android.volley.toolbox.JsonArrayRequest;
import com.avinashdavid.readitforreddit.MiscUtils.Constants;
import com.avinashdavid.readitforreddit.PostUtils.CommentRecord;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import timber.log.Timber;

import static com.avinashdavid.readitforreddit.MiscUtils.Constants.CHILDREN_KEY;
import static com.avinashdavid.readitforreddit.MiscUtils.Constants.DATA_KEY;
import static com.avinashdavid.readitforreddit.MiscUtils.Constants.KIND_KEY;
import static com.avinashdavid.readitforreddit.MiscUtils.Constants.PARENT_KEY;
import static com.avinashdavid.readitforreddit.NetworkUtils.GetCommentsService.DEPTH_MORE;
import static com.avinashdavid.readitforreddit.NetworkUtils.GetCommentsService.getChildrenCommentObjectsFromJsonDataObject;

/**
 * Created by avinashdavid on 4/25/17.
 */

public class GetMorechildrenService extends IntentService {
    private static final String EXTRA_LINK = "extraLink";

    public static final String KEY_PARENT_ID = "extraParentId";

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
        final int startPosition = intent.getIntExtra(KEY_INSERT_START_POSITION, Integer.MAX_VALUE);
        isLoading = true;
        commentsAdded = 0;

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest
                (Request.Method.GET, mUrl.toString(), null, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        CommentRecord.deleteAll(CommentRecord.class);
                        try {
                            final ArrayList<JSONObject> parentReplyJsonObjects = new ArrayList<>();
                            final String linkId = response.getJSONObject(0).getJSONObject(Constants.DATA_KEY).getJSONArray(Constants.CHILDREN_KEY).getJSONObject(0).getJSONObject(Constants.DATA_KEY).getString(Constants.ID_KEY);
                            JSONArray childrenJsonArray = response.getJSONObject(1).getJSONObject(DATA_KEY).getJSONArray(CHILDREN_KEY);
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
                                }
                            };
                            asyncTask.execute(0);
                            Intent intent = new Intent();
                            intent.setAction(Constants.BROADCAST_MORE_COMMENTS_LOADED);
                            intent.putExtra(KEY_PARENT_ID, mParentId);
                            intent.putExtra(KEY_INSERT_START_POSITION, startPosition);
                            intent.putExtra(KEY_ITEMS_INSERTED, commentsAdded);
                            GetMorechildrenService.this.sendBroadcast(intent);
                        }
                        catch (Exception e){
                            Timber.e(e);
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Timber.e("error while retreiving more comments data");
                        Intent errorIntent = new Intent(Constants.BROADCAST_MORE_COMMENTS_ERROR);
                        context.sendBroadcast(errorIntent);
                    }
                });

        NetworkSingleton.getInstance(getApplicationContext()).addToRequestQueue(jsonArrayRequest);
    }

    public static void loadMoreComments(Context context, String linkId, String parentId, int startPosition){
        Uri url = UriGenerator.getUriMoreComments(linkId);
        Timber.e(url.toString());
        Timber.e(parentId);
        Intent intent = new Intent(context, GetMorechildrenService.class);
        intent.putExtra(GetMorechildrenService.EXTRA_LINK, url);
        intent.putExtra(GetMorechildrenService.KEY_PARENT_ID, parentId);
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
                        Timber.e("adding");
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
            CommentRecord commentObject = getChildrenCommentObjectsFromJsonDataObject(currentJsonObj, linkId);
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
}
