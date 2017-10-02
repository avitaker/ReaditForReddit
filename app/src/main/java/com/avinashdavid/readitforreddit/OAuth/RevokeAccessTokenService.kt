package com.avinashdavid.readitforreddit.OAuth

import android.app.IntentService
import android.content.Intent
import android.preference.PreferenceManager
import android.util.Base64
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.avinashdavid.readitforreddit.MiscUtils.Constants
import com.avinashdavid.readitforreddit.NetworkUtils.NetworkSingleton
import com.avinashdavid.readitforreddit.OAuth.GetUserAuthService.Companion.PREF_NAME_ACCESS_TOKEN
import com.avinashdavid.readitforreddit.OAuth.GetUserAuthService.Companion.PREF_NAME_REFRESH_TOKEN
import com.avinashdavid.readitforreddit.User.LoggedInUser
import org.json.JSONObject
import timber.log.Timber
import java.util.HashMap

/**
 * Created by avinashdavid on 9/30/17.
 */
class RevokeAccessTokenService : IntentService("RevokeAccessTokenService") {
    val revokeUrlString = "https://www.reddit.com/api/v1/revoke_token"
    var currentToken = ""
    var currentRefreshToken = ""

    override fun onHandleIntent(intent: Intent?) {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.

        currentToken = PreferenceManager.getDefaultSharedPreferences(applicationContext).getString(PREF_NAME_ACCESS_TOKEN, "")
        currentRefreshToken = PreferenceManager.getDefaultSharedPreferences(applicationContext).getString(PREF_NAME_REFRESH_TOKEN, "")

        if (currentToken == "") return


        val request = object: StringRequest(Request.Method.POST, revokeUrlString, Response.Listener<String> { response ->
            Timber.d("TOKEN REVOKED")
            val spe = PreferenceManager.getDefaultSharedPreferences(applicationContext!!).edit()
            spe.putString(PREF_NAME_ACCESS_TOKEN, null)
                    .putString(PREF_NAME_REFRESH_TOKEN, null)
                    .apply()

            LoggedInUser.currentLoggedInUser = null

            val successIntent = Intent()
            successIntent.action = Constants.BROADCAST_REVOKE_TOKEN_SUCCEEDED
            this.sendBroadcast(successIntent)
        },
                Response.ErrorListener { error ->
                    Timber.e(error, revokeUrlString)
                    val message = error.message
                    val errorIntent = Intent()
                    errorIntent.action = Constants.BROADCAST_USER_AUTH_ERROR
                    errorIntent.putExtra(Constants.KEY_NETWORK_REQUEST_ERROR, message)
                    this.sendBroadcast(errorIntent)
                }
        ) {
            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()

                val creds = String.format("%s:%s", ROAM_CLIENT_ID, "")
                val auth = "Basic " + Base64.encodeToString(creds.toByteArray(), Base64.NO_WRAP)
                headers.put("Authorization", auth)

                return headers
            }

            override fun getParams(): MutableMap<String, String> {
                val headers = HashMap<String, String>()

                headers.put("token", currentToken)

                return headers
            }

            override fun getBodyContentType(): String {
                return "application/x-www-form-urlencoded; charset=UTF-8"
            }
        }

        NetworkSingleton.getInstance(this).addToRequestQueue(request)
    }
}