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
 * Created by avinashdavid on 8/28/17.
 */

class GetUserAboutService : IntentService("GetUserAboutService") {
    companion object {
        fun loadUserAbout(context: Context, userId: String) {
            val intent = Intent(context, GetUserAboutService::class.java)
            intent.putExtra(EXTRA_SERVICE_USER_NAME, userId)
            context.startService(intent)
        }
    }

    private var mUserId = ""

    override fun onHandleIntent(intent: Intent?) {
        mUserId = intent!!.getStringExtra(EXTRA_SERVICE_USER_NAME)

        val context : Context = this.applicationContext

        val request = JsonObjectRequest(Request.Method.GET,
                UriGenerator.GetUriUserAbout(mUserId).toString(),
                null, Response.Listener<JSONObject> { response ->
            val data = response!!.getJSONObject("data")!!
            data.remove("id")
            val dataString = data.toString();
            val gson: Gson = Gson()

            SugarRecord.deleteAll(UserAbout::class.java)

            val userAbout : UserAbout = gson.fromJson(dataString, UserAbout::class.java)
            val save = userAbout.save()

            val broadcast = Intent()
            broadcast.action = Constants.BROADCAST_USER_ABOUT_LOADED
            this.sendBroadcast(broadcast)},
                Response.ErrorListener { error ->
                    Timber.e(error, UriGenerator.GetUriUserAbout(mUserId).toString())
                    Timber.e(error)
                    val message = error.message
                    val errorIntent = Intent()
                    errorIntent.action = Constants.BROADCAST_USER_ABOUT_ERROR
                    errorIntent.putExtra(Constants.KEY_NETWORK_REQUEST_ERROR, message)
                    this.sendBroadcast(errorIntent)
                })

        NetworkSingleton.getInstance(applicationContext).addToRequestQueue(request)
    }
}