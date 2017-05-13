package com.avinashdavid.readitforreddit.NetworkUtils;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.avinashdavid.readitforreddit.MiscUtils.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import timber.log.Timber;

/**
 * Created by avinashdavid on 4/9/17.
 * This service gets sidebar information for any subreddit that the user visits, and stores it in sharedpreferences
 */

public class GetSubredditInfoService extends IntentService {
    public static final String EXTRA_URL = "extraSubreddUrl";
    private Uri mUrl;
    public static final String KEY_DESCRIPTION_HTML = "description_html";

    public GetSubredditInfoService(){
        super(GetSubredditInfoService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent==null){
            return;
        }

        if (intent.getParcelableExtra(EXTRA_URL)==null){
            return;
        } else {
            mUrl = intent.getParcelableExtra(EXTRA_URL);
            final Context context = GetSubredditInfoService.this.getApplicationContext();
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, mUrl.toString(), null,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                if (response.getString("kind") == null) {
//                                    Intent intent = new Intent();
//                                    intent.setAction(Constants.BROADCAST_SUBREDDIT_BANNED);
//                                    sendBroadcast(intent);
//                                    return;
                                    Intent intent = new Intent();
                                    intent.setAction(Constants.BROADCAST_SIDEBAR_ERROR);
                                    sendBroadcast(intent);
                                } else {
                                    if (response.getString("kind").equals("t5")) {
                                        JSONObject data = response.getJSONObject("data");
                                        if (data != null) {
                                            String descriptionHtml = data.getString(KEY_DESCRIPTION_HTML);

                                            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                                            sharedPreferences.edit().putString(KEY_DESCRIPTION_HTML,descriptionHtml).apply();

                                            Intent intent = new Intent();
                                            intent.setAction(Constants.BROADCAST_SIDEBAR);
                                            sendBroadcast(intent);
//                                            return;
                                        }
                                    }
                                    else {
                                        Intent intent = new Intent();
                                        intent.setAction(Constants.BROADCAST_SIDEBAR_ERROR);
                                        sendBroadcast(intent);
                                    }
                                }
                            } catch (JSONException e){
                                Timber.e(e);
                                Intent intent = new Intent();
                                intent.setAction(Constants.BROADCAST_SIDEBAR_ERROR);
                                sendBroadcast(intent);
                            }
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            });
            NetworkSingleton.getInstance(this).addToRequestQueue(jsonObjectRequest);
        }
    }

    public static void loadSidebar(Context context, @NonNull String subredditName){
        Uri url = UriGenerator.getUriSubredditAbout(subredditName);
        Intent intent = new Intent(context, GetSubredditInfoService.class);
        intent.putExtra(EXTRA_URL, url);
        context.startService(intent);
    }
}
