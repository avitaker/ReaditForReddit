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
import com.avinashdavid.readitforreddit.PostUtils.RedditListing;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

import timber.log.Timber;

import static com.avinashdavid.readitforreddit.MiscUtils.Constants.BROADCAST_SUBREDDIT_WIDGET;

/**
 * Created by avinashdavid on 3/6/17.
 */

public class GetListingsService extends IntentService {
    private Context mContext;
    private String mUrlString;

    public static final int SORT_TOP_ALLTIME = 3;
    public static final int SORT_TOP_DAY = 4;
    public static final int SORT_TOP_WEEK = 5;
    public static final int SORT_TOP_MONTH = 6;
    public static final int SORT_TOP_YEAR = 7;


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
    private static final String GILDED = "gilded";

    public GetListingsService() {
        super(GetListingsService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(final Intent intent) {
        mUrl = intent.getParcelableExtra(EXTRA_URL);
        final boolean haveToSendWidgetData = intent.getBooleanExtra(EXTRA_FOR_WIDGET, false);
        final boolean isRandom = intent.getBooleanExtra(EXTRA_RANDOM, false);
        mLoadAfter = intent.getStringExtra(EXTRA_LOAD_AFTER);
        if (null!=mLoadAfter){
            mUrl = mUrl.buildUpon().appendQueryParameter(QUERY_AFTER, mLoadAfter).build();
        } else {
            RedditListing.deleteAll(RedditListing.class);
        }

        final Context context = GetListingsService.this.getApplicationContext();
        final ArrayList<RedditListing> redditListings = new ArrayList<>();

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
                                        boolean isGilded = linkObject.getInt(GILDED)>0;
                                        boolean isNSFW = Boolean.valueOf(linkObject.getString("over_18"));

                                        RedditListing redditListing = new RedditListing(postId, System.currentTimeMillis(), title, voteCount, commentCount, author, subreddit, timeCreated, selftext, domain, afterResponse, url, thumbnailUrl, isGilded, isNSFW);
                                        redditListings.add(redditListing);
                                    }
                                }

                                SaveListingAsyncTask saveListingAsyncTask = new SaveListingAsyncTask(){
                                    @Override
                                    protected void onPostExecute(Void aVoid) {
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

                                        Intent broadcastIntent = new Intent(BROADCAST_SUBREDDIT_WIDGET);
                                        context.sendBroadcast(broadcastIntent);
                                        super.onPostExecute(aVoid);
                                    }
                                };
                                saveListingAsyncTask.execute(redditListings);
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
        String sort = null;
        if (sorting!=null) {
            sort = sorting.toLowerCase();
            if (sort.contains("top")) {
                if (sort.length() > 3) {
                    Integer time;
                    if (sort.contains("all")) {
                        time = SORT_TOP_ALLTIME;
                    } else if (sort.contains("year")) {
                        time = SORT_TOP_YEAR;
                    } else if (sort.contains("month")) {
                        time = SORT_TOP_MONTH;
                    } else if (sort.contains("week")) {
                        time = SORT_TOP_WEEK;
                    } else if (sort.contains("day")) {
                        time = SORT_TOP_DAY;
                    } else {
                        time = SORT_TOP_ALLTIME;
                    }
                    String rawSort = sort.substring(0, 3);
                    sort = rawSort.concat(Integer.toString(time));
                }
            }
        }
        Uri url = UriGenerator.getUriPosts(subreddit, sort, limit, after);
        intent.putExtra(GetListingsService.EXTRA_URL, url);
        if (forWidget){
            intent.putExtra(EXTRA_FOR_WIDGET, true);
        }
        context.startService(intent);
    }

    public static void loadListingsSearch(Context context, @Nullable String subreddit, String query, @Nullable String after, @Nullable String sorting, boolean restrictSr){
        Intent intent = new Intent(context, GetListingsService.class);
        String sort = null;
        if (sorting!=null) {
            sort = sorting.toLowerCase();
            if (sort.contains("top")) {
                if (sort.length() > 3) {
                    Integer time;
                    if (sort.contains("all")) {
                        time = SORT_TOP_ALLTIME;
                    } else if (sort.contains("year")) {
                        time = SORT_TOP_YEAR;
                    } else if (sort.contains("month")) {
                        time = SORT_TOP_MONTH;
                    } else if (sort.contains("week")) {
                        time = SORT_TOP_WEEK;
                    } else if (sort.contains("day")) {
                        time = SORT_TOP_DAY;
                    } else {
                        time = SORT_TOP_ALLTIME;
                    }
                    String rawSort = sort.substring(0, 3);
                    sort = rawSort.concat(Integer.toString(time));
                }
            }
        }
        Uri url = UriGenerator.getUriSearch(subreddit, query, after, sort, restrictSr);
        intent.putExtra(GetListingsService.EXTRA_URL, url);
        if (after!=null){
            intent.putExtra(GetListingsService.EXTRA_LOAD_AFTER, after);
        }
        context.startService(intent);
    }


    public static void loadListingsRandom(Context context){
        Intent intent = new Intent(context, GetListingsService.class);
        Uri url = UriGenerator.getUriPosts("random", null, 0, null);
        intent.putExtra(GetListingsService.EXTRA_URL, url);
        intent.putExtra(GetListingsService.EXTRA_RANDOM, true);
        context.startService(intent);
    }

    private class SaveListingAsyncTask extends AsyncTask<ArrayList<RedditListing>, Void, Void> {

        @Override
        protected Void doInBackground(ArrayList<RedditListing>... params) {
            for (RedditListing redditListing: params[0]){
                redditListing.save();
            }
            return null;
        }
    }
}
