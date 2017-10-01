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
 * Created by avinashdavid on 8/21/17.
 */
const val EXTRA_SERVICE_USER_NAME = "mUserId"

class GetUserCommentsService : IntentService("GetUserCommentsService") {
    companion object {

        fun loadUserComments(context: Context, userId: String, loadMore: Boolean = false) {
            val intent = Intent(context, GetUserCommentsService::class.java)
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

        if (mLoadMore && mUserName == UserThingsSingleton.lastUser && UserThingsSingleton.lastCommentFullName == "null") {
            return
        } else {
            if (mUserName != UserThingsSingleton.lastUser){
                UserThingsSingleton.lastUser = mUserName
                UserThingsSingleton.lastCommentFullName = ""
                UserThingsSingleton.lastSubmittedFullName = ""
                UserThingsSingleton.lastThingFullName = ""
            }

            val comments = mutableListOf<UserHistoryComment>()

            val request = JsonObjectRequest(Request.Method.GET,
                    UriGenerator.getUriUserComments(mUserName, mLoadMore),
                    null, Response.Listener<JSONObject> { response ->
                val parentData = response!!.getJSONObject("data")
                val children = parentData!!.getJSONArray("children")!!
                val fullNameAfter = parentData.getString("after")
                val gson: Gson = Gson()
                SugarRecord.deleteAll(UserHistoryComment::class.java)
                for (i in 0 until children.length()) {
                    val data = children.getJSONObject(i).getJSONObject("data")
                    val commentId = data.getString("id")
                    data.remove("id")
                    val dataString = data.toString()
                    var userHistoryComment: UserHistoryComment
                    try {
                        userHistoryComment = gson.fromJson(dataString, UserHistoryComment::class.java)
                        userHistoryComment.commentId = commentId
                        comments.add(userHistoryComment)
//                    userHistoryComment.save()
                    } catch (e : NumberFormatException) {

                    }
                }

                UserThingsSingleton.lastCommentFullName = fullNameAfter

                if (mLoadMore) UserThingsSingleton.addToComments(comments)
                else UserThingsSingleton.changeComments(comments)

                val broadcast = Intent()
                if (mLoadMore) broadcast.action = Constants.BROADCAST_USER_COMMENTS_MORE_LOADED
                else broadcast.action = Constants.BROADCAST_USER_COMMENTS_LOADED
                this.sendBroadcast(broadcast)},
                    Response.ErrorListener { error ->
                        Timber.e(error, UriGenerator.getUriUserComments(mUserName).toString())
                        val message = error.message
                        val errorIntent = Intent()
                        errorIntent.action = Constants.BROADCAST_USER_COMMENTS_ERROR
                        errorIntent.putExtra(Constants.KEY_NETWORK_REQUEST_ERROR, message)
                        this.sendBroadcast(errorIntent)
                    })

            NetworkSingleton.getInstance(applicationContext).addToRequestQueue(request)
        }
    }
}