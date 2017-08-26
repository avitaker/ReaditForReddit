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

/**
 * Created by avinashdavid on 8/21/17.
 */
class GetUserCommentsService : IntentService("GetUserCommentsService") {
    companion object {
        const val EXTRA_USER_ID = "userId"

        fun loadUserComments(context: Context, userId: String) {
            val intent = Intent(context, GetUserCommentsService::class.java)
            intent.putExtra(EXTRA_USER_ID, userId)
            context.startService(intent)
        }
    }

    private var mUserId = ""

    override fun onHandleIntent(intent: Intent?) {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        mUserId = intent!!.getStringExtra(EXTRA_USER_ID)

        val context : Context = this.applicationContext;

        val request = JsonObjectRequest(Request.Method.GET,
                UriGenerator.getUriUserComments(mUserId).toString(),
                null, Response.Listener<JSONObject> { response ->
            val children = response!!.getJSONObject("data")!!.getJSONArray("children")!!
            val gson: Gson = Gson()
            SugarRecord.deleteAll(UserHistoryComment::class.java)
            for (i in 0 until children.length()-1) {
                val data = children.getJSONObject(i).getJSONObject("data")
                data.remove("id")
                val dataString = data.toString()
                var userHistoryComment: UserHistoryComment
                try {
                    userHistoryComment = gson.fromJson(dataString, UserHistoryComment::class.java)
                    userHistoryComment.save()
                } catch (e : NumberFormatException) {

                }
            }
            val broadcast = Intent()
            broadcast.action = Constants.BROADCAST_USER_COMMENTS_LOADED
            this.sendBroadcast(broadcast)},
                Response.ErrorListener { error ->
                    val message = error.message
                    val errorIntent = Intent()
                    errorIntent.action = Constants.BROADCAST_USER_COMMENTS_ERROR
                    errorIntent.putExtra(Constants.KEY_NETWORK_REQUEST_ERROR, message)
                    this.sendBroadcast(errorIntent)
                })

        NetworkSingleton.getInstance(applicationContext).addToRequestQueue(request)
    }
}