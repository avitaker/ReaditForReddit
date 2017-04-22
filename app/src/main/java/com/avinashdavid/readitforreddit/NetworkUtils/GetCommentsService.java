package com.avinashdavid.readitforreddit.NetworkUtils;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.avinashdavid.readitforreddit.Data.ReaditContract;
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

    private static ArrayList<ContentValues> sContentValues;


    public GetCommentsService() {
        super(GetCommentsService.class.getSimpleName());
        sContentValues = new ArrayList<>();
//        try{
//            mRealm = Realm.getDefaultInstance();
//
//        }catch (Exception e){
//
//            // Get a Realm instance for this thread
//            RealmConfiguration config = new RealmConfiguration.Builder()
//                    .deleteRealmIfMigrationNeeded()
//                    .build();
//            mRealm = Realm.getInstance(config);
//
//        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        mUrl = intent.getParcelableExtra(EXTRA_URL);
        mSort = intent.getStringExtra(EXTRA_SORT);
        if (null == mSort){
            mSort = getResources().getStringArray(R.array.sort_listing_options)[0];
        }
        Timber.d(mUrl.toString());
        CommentRecord.deleteAll(CommentRecord.class);
        final Context context = GetCommentsService.this.getApplicationContext();
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest
                (Request.Method.GET, mUrl.toString(), null, new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        CommentRecord.deleteAll(CommentRecord.class);
                        try {
                            ArrayList<JSONObject> parentReplyJsonObjects = new ArrayList<>();
                            String linkId = response.getJSONObject(0).getJSONObject(DATA_KEY).getJSONArray(CHILDREN_KEY).getJSONObject(0).getJSONObject(DATA_KEY).getString(ID_KEY);
                            JSONArray childrenJsonArray = response.getJSONObject(1).getJSONObject(DATA_KEY).getJSONArray(CHILDREN_KEY);
                            parentReplyJsonObjects.addAll(getCommentDataJsonObjectsFromChildrenJsonArray(childrenJsonArray));
//                            getContentValuesList(GetCommentsService.this, parentReplyJsonObjects, linkId);
//                            getContentResolver().bulkInsert(ReaditContract.CommentEntry.getUriComments(linkId), toInsert);
                            makeCommentObjectsFromJsonObjects(context, parentReplyJsonObjects, linkId);
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
        sLastPostId = articleId;
        context.startService(intent);
    }

    private static void getContentValuesList(Context context, ArrayList<JSONObject> jsonObjects, String linkId){
        ContentValues cv;
        try {
            makeCommentObjectsFromJsonObjects(context, jsonObjects, linkId);
        } finally {
            ContentValues[] contentValues1 = new ContentValues[sContentValues.size()];
            contentValues1 = sContentValues.toArray(contentValues1);
            context.getContentResolver().bulkInsert(ReaditContract.CommentEntry.getUriComments(linkId), contentValues1);
        }
    }

    private static void makeCommentObjectsFromJsonObjects(Context context, ArrayList<JSONObject> jsonObjects, String linkId){
//        Uri uri = ReaditContract.CommentEntry.getUriComments(linkId);
//        ContentValues cv = null;
        for (int i = 0; i < jsonObjects.size(); i++){
            Timber.e("Saving comment");
            JSONObject currentJsonObj = jsonObjects.get(i);
            CommentRecord commentObject = getChildrenCommentObjectsFromJsonDataObject(currentJsonObj, linkId);
            commentObject.save();
//            cv = CommentRecord.makeContentValues(commentObject);
//            sContentValues.add(cv);

            if (commentObject.hasReplies){
                ArrayList<JSONObject> childObjects = getRepliesJsonObjectsFromCommentDataObj(currentJsonObj);
                makeCommentObjectsFromJsonObjects(context, childObjects, linkId);
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
                if (jsonObject.getString(KIND_KEY).equals(COMMENT_KIND)){
                    JSONObject replyData = jsonObject.getJSONObject(DATA_KEY);
                    jsonObjects.add(replyData);
                }
            }
        } catch (Exception e){
            Timber.e(e, "error in getChildrenCommentObjectsFromParentJSONArray");
        }
        return jsonObjects;
    }

    private static CommentRecord getChildrenCommentObjectsFromJsonDataObject(JSONObject replyData, String linkId){
        CommentRecord commentObject = null;
        try {
            String id = replyData.getString(ID_KEY);
            boolean scoreHidden = replyData.getBoolean(SCORE_HIDDEN_KEY);
            int score = replyData.getInt(SCORE_KEY);
            String author = replyData.getString(AUTHOR_KEY);
            String body = replyData.getString(BODY_HTML);
            float timecreated = (float)replyData.getLong(TIME_CREATED_KEY);
            String parent = replyData.getString(PARENT_KEY);
            int depth = replyData.getInt(DEPTH_KEY);
            boolean hasReplies = !replyData.get(REPLIES_KEY).toString().equals("");
            String authorflair = replyData.getString(KEY_AUTHOR_FLAIR);
            boolean isGilded = replyData.getInt(GILDED)>0;
            commentObject = new CommentRecord(System.currentTimeMillis(), id, linkId, scoreHidden, score, author, body, parent, timecreated, depth, hasReplies, authorflair, isGilded);


        } catch (Exception e){
            Timber.e(e, "error in getChildrenCommentObjectsFromJsonDataObject");
        }
        return commentObject;
    }
}
