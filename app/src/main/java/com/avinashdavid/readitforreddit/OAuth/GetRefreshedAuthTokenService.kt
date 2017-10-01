package com.avinashdavid.readitforreddit.OAuth

import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.preference.PreferenceManager
import android.util.Base64
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.avinashdavid.readitforreddit.MiscUtils.Constants
import com.avinashdavid.readitforreddit.NetworkUtils.NetworkSingleton
import com.avinashdavid.readitforreddit.OAuth.GetUserAuthService.Companion.PREF_NAME_ACCESS_TOKEN
import com.avinashdavid.readitforreddit.OAuth.GetUserAuthService.Companion.PREF_NAME_EXPIRES_AT
import com.avinashdavid.readitforreddit.OAuth.GetUserAuthService.Companion.PREF_NAME_REFRESH_TOKEN
import com.avinashdavid.readitforreddit.OAuth.GetUserAuthService.Companion.getRefreshTime
import org.json.JSONObject
import timber.log.Timber
import java.util.HashMap

/**
 * Created by avinashdavid on 9/30/17.
 */
class GetRefreshedAuthTokenService : IntentService("GetRefreshedAuthTokenService") {
    companion object {
        private  const val REFRESH_TOKEN_BASE_URL = "https://www.reddit.com/api/v1/access_token"
        fun loadRefreshTokenService(context: Context) {
            val intent = Intent(context, GetRefreshedAuthTokenService::class.java)
            context.startService(intent)
        }
    }

    override fun onHandleIntent(intent: Intent?) {
//        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
        val refreshToken = PreferenceManager.getDefaultSharedPreferences(this).getString(PREF_NAME_REFRESH_TOKEN, null)
        if (refreshToken == null) {
            Toast.makeText(this.applicationContext, "Please sign in", Toast.LENGTH_LONG).show()
            val authActivityIntent = Intent(this, GetAuthActivity::class.java)
            startActivity(authActivityIntent)
            return
        }

        val request = object: StringRequest(Request.Method.POST, REFRESH_TOKEN_BASE_URL, Response.Listener<String> { response ->
            val resObject = JSONObject(response)

            try {
                PreferenceManager.getDefaultSharedPreferences(applicationContext!!).edit().putString(PREF_NAME_ACCESS_TOKEN, resObject.getString(GetUserAuthService.KEY_ACCESS_TOKEN)).apply()
                if (resObject.getString(GetUserAuthService.KEY_REFRESH_TOKEN) != null)
                    PreferenceManager.getDefaultSharedPreferences(applicationContext!!).edit().putString(PREF_NAME_REFRESH_TOKEN, resObject.getString(GetUserAuthService.KEY_REFRESH_TOKEN)).apply()
                PreferenceManager.getDefaultSharedPreferences(applicationContext!!).edit().putLong(PREF_NAME_EXPIRES_AT, getRefreshTime(this, resObject.getLong(GetUserAuthService.KEY_EXPIRES_IN)))

                val successIntent = Intent()
                successIntent.action = Constants.BROADCAST_USER_AUTH_SUCCEEDED
                this.sendBroadcast(successIntent)
            } catch (ex: Exception) {
                Timber.e(ex, REFRESH_TOKEN_BASE_URL)
                val errorIntent = Intent()
                errorIntent.action = Constants.BROADCAST_USER_AUTH_ERROR
                errorIntent.putExtra(Constants.KEY_NETWORK_REQUEST_ERROR, ex.message)
                this.sendBroadcast(errorIntent)
            }
        },
                Response.ErrorListener { error ->
                    Timber.e(error, REFRESH_TOKEN_BASE_URL)
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

                headers.put("grant_type", "refresh_token")
                headers.put("refresh_token", refreshToken)

                return headers
            }

            override fun getBodyContentType(): String {
                return "application/x-www-form-urlencoded; charset=UTF-8"
            }
        }

        NetworkSingleton.getInstance(this).addToRequestQueue(request)
    }
}