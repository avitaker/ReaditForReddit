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
 * Created by avinashdavid on 8/30/17.
 */
const val EXTRA_USER_OVERVIEW_MORE = "EXTRA_USER_OVERVIEW_MORE"
class GetUserOverviewService : IntentService("GetUserOverviewService") {
    companion object {
        fun loadUserOverview(context: Context, userId: String, loadMore: Boolean = false) {
            val intent = Intent(context, GetUserOverviewService::class.java)
            intent.putExtra(EXTRA_SERVICE_USER_NAME, userId)
            intent.putExtra(EXTRA_USER_OVERVIEW_MORE, loadMore)
            context.startService(intent)
        }
    }

    private var mUserName = ""
    private var mLoadMore = false

    override fun onHandleIntent(intent: Intent?) {
        mUserName = intent!!.getStringExtra(EXTRA_SERVICE_USER_NAME)
        mLoadMore = intent.getBooleanExtra(EXTRA_USER_OVERVIEW_MORE, false)

        val context : Context = this.applicationContext

        if (mLoadMore && mUserName == UserThingsSingleton.lastUser && UserThingsSingleton.lastThingFullName == "null") {
            return
        } else {
            if (mUserName != UserThingsSingleton.lastUser){
                UserThingsSingleton.lastUser = mUserName
                UserThingsSingleton.lastCommentFullName = ""
                UserThingsSingleton.lastSubmittedFullName = ""
                UserThingsSingleton.lastThingFullName = ""
            }
            val url = UriGenerator.getUriUserOverview(mUserName, mLoadMore)

            val request = JsonObjectRequest(Request.Method.GET,
                    url,
                    null, Response.Listener<JSONObject> { response ->
                val parentData = response!!.getJSONObject("data")
                val children = parentData!!.getJSONArray("children")!!
                val fullNameAfter = parentData.getString("after")

                val gson = Gson()

                val things = mutableListOf<SugarRecord>()

                for (i in 0 until children.length()) {
                    val obj = children.getJSONObject(i)
                    val kind = obj.getString("kind")
                    val data = obj.getJSONObject("data")
                    val thingId = data.getString("id")
                    data.remove("id")
                    val dataString = data.toString()
                    if (kind.equals("t1")) {
                        var userHistoryComment: UserHistoryComment
                        try {
                            userHistoryComment = gson.fromJson(dataString, UserHistoryComment::class.java)
                            userHistoryComment.commentId = thingId
                            things.add(userHistoryComment)
                        } catch (e : NumberFormatException) {

                        }
                    } else if (kind.equals("t3")){
                        try {
                            val userHistoryListing = gson.fromJson(dataString, UserHistoryListing::class.java)
                            userHistoryListing.postId = thingId
                            things.add(userHistoryListing)
                        } catch (e: NumberFormatException) {

                        }
                    }
                }

                UserThingsSingleton.lastThingFullName = fullNameAfter

                val broadcast = Intent()
                if (mLoadMore) {
                    UserThingsSingleton.addToThings(things)
                    broadcast.action = Constants.BROADCAST_USER_OVERVIEW_MORE_LOADED
                }
                else {
                    UserThingsSingleton.changeThings(things)
                    broadcast.action = Constants.BROADCAST_USER_OVERVIEW_LOADED
                }

                this.sendBroadcast(broadcast)},
                    Response.ErrorListener { error ->
                        Timber.e(error, UriGenerator.getUriUserOverview(mUserName).toString())
                        val message = error.message
                        val errorIntent = Intent()
                        errorIntent.action = Constants.BROADCAST_USER_OVERVIEW_ERROR
                        errorIntent.putExtra(Constants.KEY_NETWORK_REQUEST_ERROR, message)
                        this.sendBroadcast(errorIntent)
                    })

            NetworkSingleton.getInstance(applicationContext).addToRequestQueue(request)
        }
    }
}