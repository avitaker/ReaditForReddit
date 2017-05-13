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

import org.json.JSONArray;
import org.json.JSONObject;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmList;
import io.realm.RealmResults;
import timber.log.Timber;

/**
 * Created by avinashdavid on 3/8/17.
 * This class gets default subreddits and stores them as SubredditObject (Realm) objects.
 * It is only called on initial setup, or in the case that the user reaches zero subreddit subscriptions
 */

public class GetSubredditsService extends IntentService {
    private Realm mRealm;

    private Uri mUrl;

    private String mWhere;
    private String mMineWhere;

    public static final String EXTRA_URL = "extraUrlParcelable";

    private static final String KIND_KEY = "kind";
    private static final String LISTING_KIND = "Listing";
    private static final String SUBREDDIT_KIND = "t5";
    private static final String DATA_KEY = "data";
    private static final String CHILDREN_KEY = "children";
    private static final String DISPLAY_NAME_KEY = "display_name";
    private static final String PUBLIC_DESCRIPTION_KEY = "public_description";
    private static final String NSFW_KEY = "over18";
    private static final String SUBREDDIT_TYPE_KEY = "subreddit_type";
    private static final String TITLE_KEY = "title";
    private static final String HEADER_IMG_KEY = "header_img";

    RealmList<SubredditObject> mSubredditObjects;

    public GetSubredditsService() {
        super(GetSubredditsService.class.getSimpleName());
        try{
            mRealm = Realm.getDefaultInstance();

        }catch (Exception e){

            // Get a Realm instance for this thread
            RealmConfiguration config = new RealmConfiguration.Builder()
                    .deleteRealmIfMigrationNeeded()
                    .build();
            mRealm = Realm.getInstance(config);

        }
        mSubredditObjects = new RealmList<>();
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        mUrl = intent.getParcelableExtra(EXTRA_URL);
        if (mUrl == null){
            //TODO handle empty intent
            return;
        }
        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, mUrl.toString(), null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (!response.getString(KIND_KEY).equals(LISTING_KIND)){
                                return;
                            }
                            JSONObject jsonObject = response.getJSONObject(DATA_KEY);
                            JSONArray children = jsonObject.getJSONArray(CHILDREN_KEY);
                            for (int i = 0; i < children.length(); i++){
                                JSONObject subreddit = children.getJSONObject(i);
                                if (subreddit.getString(KIND_KEY).equals(SUBREDDIT_KIND)){
                                    JSONObject data = subreddit.getJSONObject(DATA_KEY);
                                    String subredditName = data.getString(DISPLAY_NAME_KEY);
                                    String publicDesc = data.getString(PUBLIC_DESCRIPTION_KEY);
                                    String subredditType = data.getString(SUBREDDIT_TYPE_KEY);
                                    boolean goodForKids = data.getBoolean(NSFW_KEY);
                                    String title = data.getString(TITLE_KEY);
                                    String headerImg = data.getString(HEADER_IMG_KEY);

                                    final SubredditObject subreeeed = new SubredditObject();
                                    subreeeed.setSubredditName(subredditName);
                                    subreeeed.setPublicDescription(publicDesc);
                                    subreeeed.setSubredditType(subredditType);
                                    subreeeed.setSafeForKids(goodForKids);
                                    subreeeed.setTitle(title);
                                    subreeeed.setHeaderImgUrl(headerImg);
                                    subreeeed.setTimestamp(System.currentTimeMillis());

                                    mSubredditObjects.add(i, subreeeed);

                                }
                            }

                        } catch (Exception e){
                            Timber.e(e);
                        }
                        final RealmResults<SubredditObject> realmResults = mRealm.where(SubredditObject.class).findAll();
                        mRealm.executeTransaction(new Realm.Transaction() {
                            @Override
                            public void execute(Realm realm) {
                                realmResults.deleteAllFromRealm();
                                mRealm.copyToRealmOrUpdate(mSubredditObjects);
                            }
                        });
                        Intent doneIntent = new Intent(Constants.BROADCAST_SUBREDDITS_LOADED);
                        GetSubredditsService.this.getApplicationContext().sendBroadcast(doneIntent);
                    }

                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub
                        Timber.e(error, "error while retreiving listings data");
                    }
                });

        NetworkSingleton.getInstance(getApplicationContext()).addToRequestQueue(jsObjRequest);
    }

    @Override
    public void onDestroy() {
        if (mRealm!=null){
            mRealm.close();
        }
        super.onDestroy();
    }

    public static void loadSubreddits(Context context, @Nullable String where, @Nullable String mineWhere){
        Intent intent = new Intent(context, GetSubredditsService.class);
        Uri url = UriGenerator.getUriSubredditList(where, mineWhere);
        intent.putExtra(GetSubredditsService.EXTRA_URL, url);
        context.startService(intent);
    }
}
