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
const val EXTRA_SERVICE_USER_ID = "mUserId"

class GetUserCommentsService : IntentService("GetUserCommentsService") {
    companion object {

        fun loadUserComments(context: Context, userId: String) {
            val intent = Intent(context, GetUserCommentsService::class.java)
            intent.putExtra(EXTRA_SERVICE_USER_ID, userId)
            context.startService(intent)
        }
    }

    private var mUserId = ""

    override fun onHandleIntent(intent: Intent?) {
        mUserId = intent!!.getStringExtra(EXTRA_SERVICE_USER_ID)

        val context : Context = this.applicationContext;

        val comments = mutableListOf<UserHistoryComment>()

        val request = JsonObjectRequest(Request.Method.GET,
                UriGenerator.getUriUserComments(mUserId).toString(),
                null, Response.Listener<JSONObject> { response ->
            val children = response!!.getJSONObject("data")!!.getJSONArray("children")!!
            val gson: Gson = Gson()
            SugarRecord.deleteAll(UserHistoryComment::class.java)
            for (i in 0 until children.length()) {
                val data = children.getJSONObject(i).getJSONObject("data")
                data.remove("id")
                val dataString = data.toString()
                var userHistoryComment: UserHistoryComment
                try {
                    userHistoryComment = gson.fromJson(dataString, UserHistoryComment::class.java)
                    comments.add(userHistoryComment)
//                    userHistoryComment.save()
                } catch (e : NumberFormatException) {

                }
            }
            UserThingsSingleton.changeComments(comments)

            val broadcast = Intent()
            broadcast.action = Constants.BROADCAST_USER_COMMENTS_LOADED
            this.sendBroadcast(broadcast)},
                Response.ErrorListener { error ->
                    Timber.e(error, UriGenerator.getUriUserComments(mUserId).toString())
                    val message = error.message
                    val errorIntent = Intent()
                    errorIntent.action = Constants.BROADCAST_USER_COMMENTS_ERROR
                    errorIntent.putExtra(Constants.KEY_NETWORK_REQUEST_ERROR, message)
                    this.sendBroadcast(errorIntent)
                })

        NetworkSingleton.getInstance(applicationContext).addToRequestQueue(request)
    }
}