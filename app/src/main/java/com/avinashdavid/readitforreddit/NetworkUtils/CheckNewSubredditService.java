package com.avinashdavid.readitforreddit.NetworkUtils;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.avinashdavid.readitforreddit.MiscUtils.Constants;
import com.avinashdavid.readitforreddit.SubredditUtils.SubredditObject;

import org.json.JSONObject;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmList;
import timber.log.Timber;

/**
 * Created by avinashdavid on 3/20/17.
 */

public class CheckNewSubredditService extends IntentService {
    private Uri mUrl;
    public static final String EXTRA_URL = "extraUrlParcelableSubreddits";
    Realm mRealm;
    RealmList<SubredditObject> mSubredditObjectRealmList;

    private static final String DISPLAY_NAME_KEY = "display_name";
    private static final String PUBLIC_DESCRIPTION_KEY = "public_description";
    private static final String NSFW_KEY = "over18";
    private static final String SUBREDDIT_TYPE_KEY = "subreddit_type";
    private static final String TITLE_KEY = "title";
    private static final String HEADER_IMG_KEY = "header_img";

    public CheckNewSubredditService() {
        super(CheckNewSubredditService.class.getSimpleName());
        try{
            mRealm = Realm.getDefaultInstance();

        }catch (Exception e){

            // Get a Realm instance for this thread
            RealmConfiguration config = new RealmConfiguration.Builder()
                    .deleteRealmIfMigrationNeeded()
                    .build();
            mRealm = Realm.getInstance(config);

        }
        mSubredditObjectRealmList = new RealmList<>();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent==null){
            return;
        }
        mUrl = intent.getParcelableExtra(EXTRA_URL);
        if (mUrl == null){
            return;
        }

//        String subreddit = mUrl.getPathSegments().get(mUrl.getPathSegments().size()-2);
//        if (mRealm.where(SubredditObject.class).equalTo("subredditName", subreddit).findAll().size()>0){
//            Intent intent1 = new Intent(Constants.BROADCAST_SUBREDDIT_PRESENT);
//            sendBroadcast(intent1);
//        }
//        else {
            final Context context = CheckNewSubredditService.this.getApplicationContext();
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, mUrl.toString(), null, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    try {
                        if (response.getString("kind") == null) {
                            Intent intent = new Intent();
                            intent.setAction(Constants.BROADCAST_SUBREDDIT_BANNED);
                            sendBroadcast(intent);
                            return;
                        } else {
                            if (response.getString("kind").equals("t5")) {
                                JSONObject data = response.getJSONObject("data");
                                if (data != null) {
                                    String subredditName = data.getString(DISPLAY_NAME_KEY);
                                    String publicDesc = data.getString(PUBLIC_DESCRIPTION_KEY);
                                    String subredditType = data.getString(SUBREDDIT_TYPE_KEY);
                                    boolean goodForKids = data.getBoolean(NSFW_KEY);
                                    String title = data.getString(TITLE_KEY);
                                    String headerImg = data.getString(HEADER_IMG_KEY);

                                    final SubredditObject subreeeed = new SubredditObject();
                                    if (mRealm.where(SubredditObject.class).equalTo("subredditName", subredditName).findAll().size()>0){
                                        Intent intent1 = new Intent(Constants.BROADCAST_SUBREDDIT_PRESENT);
                                        sendBroadcast(intent1);
                                        return;
                                    }
                                    subreeeed.setSubredditName(subredditName);
                                    subreeeed.setPublicDescription(publicDesc);
                                    subreeeed.setSubredditType(subredditType);
                                    subreeeed.setSafeForKids(goodForKids);
                                    subreeeed.setTitle(title);
                                    subreeeed.setHeaderImgUrl(headerImg);
                                    subreeeed.setTimestamp(System.currentTimeMillis());

                                    mSubredditObjectRealmList.add(0, subreeeed);
                                    mRealm.executeTransaction(new Realm.Transaction() {
                                        @Override
                                        public void execute(Realm realm) {
                                            mRealm.copyToRealmOrUpdate(mSubredditObjectRealmList);
                                        }
                                    });

                                    Intent intent = new Intent();
                                    intent.setAction(Constants.BROADCAST_SUBREDDIT_ADDED);
                                    sendBroadcast(intent);
                                    return;
                                }
                            } else {
                                Intent intent = new Intent();
                                intent.setAction(Constants.BROADCAST_NO_SUCH_SUBREDDIT);
                                sendBroadcast(intent);
                                return;
                            }
                        }
                        Intent intent = new Intent();
                        intent.setAction(Constants.BROADCAST_NO_SUCH_SUBREDDIT);
                        sendBroadcast(intent);
                    } catch (Exception e) {
                        Timber.e(e);
                    }
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Timber.e(error);
                    Intent intent = new Intent();
                    intent.setAction(Constants.BROADCAST_NO_SUCH_SUBREDDIT);
                    context.sendBroadcast(intent);
                }
            });
            NetworkSingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
//        }
    }

    @Override
    public void onDestroy() {
        if (mRealm!=null){
            mRealm.close();
        }
        super.onDestroy();
    }
}
