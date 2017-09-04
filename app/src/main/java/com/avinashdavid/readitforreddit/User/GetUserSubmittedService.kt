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
import com.orm.SugarRecord
import org.json.JSONObject
import timber.log.Timber

/**
 * Created by avinashdavid on 9/2/17.
 */
class GetUserSubmittedService: IntentService("GetUserSubmittedService") {
    companion object {
        fun loadUserSubmitted(context: Context, userId: String, loadMore: Boolean = false) {
            val intent = Intent(context, GetUserSubmittedService::class.java)
            intent.putExtra(EXTRA_SERVICE_USER_NAME, userId)
            intent.putExtra(EXTRA_USER_OVERVIEW_MORE, loadMore)
            context.startService(intent)
        }
    }

    private var mUserName = ""
    private var mLoadMore = false

    override fun onHandleIntent(intent: Intent?) {
        mUserName = intent!!.getStringExtra(EXTRA_SERVICE_USER_NAME)
        mLoadMore = intent!!.getBooleanExtra(EXTRA_USER_OVERVIEW_MORE, false)

        if (mLoadMore && mUserName == UserThingsSingleton.lastUser && UserThingsSingleton.lastSubmittedFullName == "null") {
            return
        } else {
            if (mUserName != UserThingsSingleton.lastUser){
                UserThingsSingleton.lastUser = mUserName
                UserThingsSingleton.lastCommentFullName = ""
                UserThingsSingleton.lastSubmittedFullName = ""
                UserThingsSingleton.lastThingFullName = ""
            }

            val submissions = mutableListOf<UserHistoryListing>()

            val request = JsonObjectRequest(Request.Method.GET,
                    UriGenerator.getUriUserSubmitted(mUserName, mLoadMore),
                    null, Response.Listener<JSONObject> { response ->
                val parentData = response!!.getJSONObject("data")
                val children = parentData!!.getJSONArray("children")!!
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

                val name = parentData.getString("after")
                UserThingsSingleton.lastSubmittedFullName = name

                if (mLoadMore) UserThingsSingleton.addToSubmitted(submissions)
                else UserThingsSingleton.changeSubmitted(submissions)

                val broadcast = Intent()
                if (mLoadMore) broadcast.action = Constants.BROADCAST_USER_SUBMITTED_MORE_LOADED
                else broadcast.action = Constants.BROADCAST_USER_SUBMITTED_LOADED
                this.sendBroadcast(broadcast)},
                    Response.ErrorListener { error ->
                        Timber.e(error, UriGenerator.getUriUserSubmitted(mUserName).toString())
                        val message = error.message
                        val errorIntent = Intent()
                        errorIntent.action = Constants.BROADCAST_USER_SUBMITTED_ERROR
                        errorIntent.putExtra(Constants.KEY_NETWORK_REQUEST_ERROR, message)
                        this.sendBroadcast(errorIntent)
                    })

            NetworkSingleton.getInstance(applicationContext).addToRequestQueue(request)
        }
    }
}