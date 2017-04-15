package com.avinashdavid.readitforreddit.NetworkUtils;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.avinashdavid.readitforreddit.MiscUtils.Constants;
import com.avinashdavid.readitforreddit.MiscUtils.GeneralUtils;
import com.avinashdavid.readitforreddit.PostUtils.RedditListing;
import com.avinashdavid.readitforreddit.PostUtils.RedditPost;
import com.avinashdavid.readitforreddit.R;
import com.avinashdavid.readitforreddit.Widget.RedditPostWidget;
import com.avinashdavid.readitforreddit.Widget.SubredditWidgetProvider;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmList;
import io.realm.RealmResults;
import timber.log.Timber;

import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;
import static com.avinashdavid.readitforreddit.MiscUtils.Constants.BROADCAST_SUBREDDIT_WIDGET;

/**
 * Created by avinashdavid on 3/6/17.
 */

public class GetListingsService extends IntentService {
    private Context mContext;
    private String mUrlString;

    private Uri mUrl;

    private String mLoadAfter;

    public static final String EXTRA_URL_STRING = "extraUrl";
    public static final String EXTRA_URL = "extraUrlParcelable";
    public static final String EXTRA_LOAD_AFTER = "extraLoadAfter";

    public static final String EXTRA_FOR_WIDGET = "extraforw";
    public static final String EXTRA_RANDOM = "extraRand";

    private static final String QUERY_AFTER = "after";

    private static final String KIND_KEY = "kind";
    private static final String LISTING_KIND = "Listing";
    private static final String DATA_KEY = "data";
    private static final String CHILDREN_KEY = "children";
    private static final String LINK_KIND = "t3";
    private static final String ID_KEY = "id";
    private static final String TITLE_KEY = "title";
    private static final String VOTE_KEY = "score";
    private static final String NUMBER_OF_COMMENTS_KEY = "num_comments";
    private static final String DOMAIN_KEY = "domain";
    private static final String AUTHOR_KEY = "author";
    private static final String TIME_CREATED_KEY = "created_utc";
    private static final String SUBREDDIT_KEY = "subreddit";
    private static final String URL_KEY = "url";
    private static final String SELFTEXT_HTML = "selftext_html";
    private static final String THUMBNAIL_URL = "thumbnail";

//    private RealmList<RedditPost> mRedditPosts;

    public GetListingsService() {
        super(GetListingsService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(final Intent intent) {
        mUrl = intent.getParcelableExtra(EXTRA_URL);
        final boolean haveToSendWidgetData = intent.getBooleanExtra(EXTRA_FOR_WIDGET, false);
        final boolean isRandom = intent.getBooleanExtra(EXTRA_RANDOM, false);
        Timber.d(mUrl.toString());
        mLoadAfter = intent.getStringExtra(EXTRA_LOAD_AFTER);
        if (null!=mLoadAfter){
            mUrl = mUrl.buildUpon().appendQueryParameter(QUERY_AFTER, mLoadAfter).build();
        } else {
            Timber.d("mAfter is null");
            RedditListing.deleteAll(RedditListing.class);
        }

        final Context context = GetListingsService.this.getApplicationContext();

        JsonObjectRequest jsObjRequest = new JsonObjectRequest
                (Request.Method.GET, mUrl.toString(), null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (!response.getString(KIND_KEY).equals(LISTING_KIND)){
                                return;
                            } else {
                                JSONObject dataObject = response.getJSONObject(DATA_KEY);
                                String afterResponse = dataObject.getString(QUERY_AFTER);
                                JSONArray childrenArray = dataObject.getJSONArray(CHILDREN_KEY);
                                if (haveToSendWidgetData){
                                    RedditListing.deleteAll(RedditListing.class);
                                }
                                for (int i =0; i<childrenArray.length(); i++){
                                    JSONObject listingObject = childrenArray.getJSONObject(i);
                                    if (listingObject.getString(KIND_KEY).equals(LINK_KIND)){
                                        JSONObject linkObject = listingObject.getJSONObject(DATA_KEY);
                                        String postId = linkObject.getString(ID_KEY);
                                        String title = linkObject.getString(TITLE_KEY);
                                        int voteCount = linkObject.getInt(VOTE_KEY);
                                        int commentCount = linkObject.getInt(NUMBER_OF_COMMENTS_KEY);
                                        float timeCreated = (float)linkObject.getDouble(TIME_CREATED_KEY);
                                        String domain = linkObject.getString(DOMAIN_KEY);
                                        String author = linkObject.getString(AUTHOR_KEY);
                                        String subreddit = linkObject.getString(SUBREDDIT_KEY);
                                        String url = linkObject.getString(URL_KEY);
                                        String selftext = linkObject.getString(SELFTEXT_HTML);
                                        String thumbnailUrl = linkObject.getString(THUMBNAIL_URL);

                                        RedditListing redditListing = new RedditListing(postId, System.currentTimeMillis(), title, voteCount, commentCount, author, subreddit, timeCreated, selftext, domain, afterResponse, url, thumbnailUrl);
                                        redditListing.save();
//                                        getContentResolver().query(redditListing.)
                                    }
                                }
                                Intent localIntent = new Intent();
                                if (!isRandom) {
                                    localIntent.setAction(Constants.BROADCAST_POSTS_LOADED);
                                } else {
                                    localIntent.setAction(Constants.BROADCAST_RANDOM_SUBREDDIT_POSTS_LOADED);
                                }
                                sendBroadcast(localIntent);
//                                if (haveToSendWidgetData){
//                                    Timber.d("have to send widget data");
//
//                                    Intent broadcastIntent = new Intent(BROADCAST_SUBREDDIT_WIDGET);
//                                    context.sendBroadcast(broadcastIntent);
//                                }
                                Timber.d("have to send widget data");

                                Intent broadcastIntent = new Intent(BROADCAST_SUBREDDIT_WIDGET);
                                context.sendBroadcast(broadcastIntent);
                            }
                        }
                        catch (Exception e){
                            Timber.e(e);
                        }

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO Auto-generated method stub
                        Timber.e(error, "error while retreiving listings data");
                        Intent errorIntent = new Intent(Constants.BROADCAST_ERROR_WHILE_RETREIVING_POSTS);
                        context.sendBroadcast(errorIntent);
                    }
                });
        NetworkSingleton.getInstance(getApplicationContext()).addToRequestQueue(jsObjRequest);
    }

    public static void loadListingsSubreddit(Context context, @Nullable String subreddit, @Nullable String sorting, int limit, @Nullable String after, boolean forWidget){
        Intent intent = new Intent(context, GetListingsService.class);
        Uri url = UriGenerator.getUriPosts(subreddit, sorting, limit, after);
        intent.putExtra(GetListingsService.EXTRA_URL, url);
        if (forWidget){
            intent.putExtra(EXTRA_FOR_WIDGET, true);
        }
        context.startService(intent);
    }

    public static void loadListingsSearch(Context context, @Nullable String subreddit, String query, @Nullable String after, @Nullable String sort, boolean restrictSr){
        Intent intent = new Intent(context, GetListingsService.class);
        Uri url = UriGenerator.getUriSearch(subreddit, query, after, sort, restrictSr);
        intent.putExtra(GetListingsService.EXTRA_URL, url);
        if (after!=null){
            intent.putExtra(GetListingsService.EXTRA_LOAD_AFTER, after);
        }
        context.startService(intent);
    }

    public static RealmResults<RedditPost> getPostsForWidget(Context context, @Nullable String subreddit){
        RealmResults<RedditPost> realmResults;
        try {
            Intent intent = new Intent(context, GetListingsService.class);
            intent.putExtra(GetListingsService.EXTRA_URL, UriGenerator.getUriPosts(subreddit, null, 0, null));
            context.startService(intent);
        } finally {
            Realm realm = Realm.getDefaultInstance();
            realmResults =  realm.where(RedditPost.class).findAll();
        }
        return realmResults;
    }

    public static void loadListingsRandom(Context context){
        Intent intent = new Intent(context, GetListingsService.class);
        Uri url = UriGenerator.getUriPosts("random", null, 0, null);
        intent.putExtra(GetListingsService.EXTRA_URL, url);
        intent.putExtra(GetListingsService.EXTRA_RANDOM, true);
        context.startService(intent);
    }
}
