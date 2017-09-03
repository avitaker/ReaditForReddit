package com.avinashdavid.readitforreddit.User

import android.app.IntentService
import android.content.Context
import android.content.Intent
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.avinashdavid.readitforreddit.MiscUtils.Constants
import com.avinashdavid.readitforreddit.NetworkUtils.NetworkSingleton
import com.avinashdavid.readitforreddit.NetworkUtils.UriGenerator
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.orm.SugarRecord
import org.json.JSONObject
import timber.log.Timber

/**
 * Created by avinashdavid on 9/2/17.
 */
class GetUserSubmittedService: IntentService("GetUserSubmittedService") {
    companion object {
        fun loadUserSubmitted(context: Context, userId: String) {
            val intent = Intent(context, GetUserSubmittedService::class.java)
            intent.putExtra(EXTRA_SERVICE_USER_ID, userId)
            context.startService(intent)
        }
    }

    private var mUserId = ""

    override fun onHandleIntent(intent: Intent?) {
        mUserId = intent!!.getStringExtra(EXTRA_SERVICE_USER_ID)

        val submissions = mutableListOf<UserHistoryListing>()

        val request = JsonObjectRequest(Request.Method.GET,
                UriGenerator.getUriUserSubmitted(mUserId),
                null, Response.Listener<JSONObject> { response ->
            val children = response!!.getJSONObject("data")!!.getJSONArray("children")!!
            val gson = Gson()
            SugarRecord.deleteAll(UserHistoryListing::class.java)
            for (i in 0 until children.length()) {
                val data = children.getJSONObject(i).getJSONObject("data")
                val postId = data.getString("id")
                data.remove("id")
                val dataString = data.toString()
                var userHistoryListing: UserHistoryListing
                try {
                    userHistoryListing = gson.fromJson(dataString, UserHistoryListing::class.java)
                    userHistoryListing.postId = postId
                    submissions.add(userHistoryListing)
//                    userHistoryListing.save()
                } catch (e : NumberFormatException) {

                }
            }

            UserThingsSingleton.changeSubmitted(submissions)

            val broadcast = Intent()
            broadcast.action = Constants.BROADCAST_USER_SUBMITTED_LOADED
            this.sendBroadcast(broadcast)},
                Response.ErrorListener { error ->
                    Timber.e(error, UriGenerator.getUriUserSubmitted(mUserId).toString())
                    val message = error.message
                    val errorIntent = Intent()
                    errorIntent.action = Constants.BROADCAST_USER_SUBMITTED_ERROR
                    errorIntent.putExtra(Constants.KEY_NETWORK_REQUEST_ERROR, message)
                    this.sendBroadcast(errorIntent)
                })

        NetworkSingleton.getInstance(applicationContext).addToRequestQueue(request)
    }
}