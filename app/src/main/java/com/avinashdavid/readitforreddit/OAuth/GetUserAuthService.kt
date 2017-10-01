package com.avinashdavid.readitforreddit.OAuth

import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.preference.PreferenceManager
import android.util.Base64
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.avinashdavid.readitforreddit.MiscUtils.Constants
import com.avinashdavid.readitforreddit.NetworkUtils.NetworkSingleton
import org.json.JSONException
import org.json.JSONObject
import timber.log.Timber
import java.util.*


/**
 * Created by avinashdavid on 9/17/17.
 */

private const val STATE_STRING = "whatAAAA"
private const val GET_AUTH_TOKEN_URI = "https://www.reddit.com/api/v1/access_token"
private const val REDIRECT_URI = "http://avinashdavid.com"

class GetUserAuthService : IntentService("GetUserAuthService") {
    companion object {
        private const val KEY_CODE = "KEY_CODE"
        private const val KEY_STATE = "KEY_STATE"

        const val PREF_NAME_ACCESS_TOKEN = "PREF_NAME_ACCESS_TOKEN"
        const val PREF_NAME_EXPIRES_AT = "PREF_NAME_EXPIRES_AT"
        const val PREF_NAME_REFRESH_TOKEN = "PREF_NAME_REFRESH_TOKEN"

        public const val KEY_ACCESS_TOKEN = "access_token"
        public const val KEY_REFRESH_TOKEN = "refresh_token"
        public const val KEY_EXPIRES_IN = "expires_in"

        fun loadGetUserAuthService(context: Context, code: String, state: String) {
            val intent = Intent(context, GetUserAuthService::class.java)
            intent.putExtra(KEY_CODE, code)
            intent.putExtra(KEY_STATE, state)
            context.startService(intent)
        }

        fun getRefreshTime(context: Context, secondsToExpire: Long): Long{
            val cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"))
            val time = cal.timeInMillis
            val expiresAt = time + (secondsToExpire * 1000)
            return expiresAt
        }
    }

    override fun onHandleIntent(intent: Intent?) {
        val codeExtra = intent?.getStringExtra(KEY_CODE)
        val stateExtra = intent?.getStringExtra(KEY_STATE)


        val request = object: StringRequest(Request.Method.POST, GET_AUTH_TOKEN_URI, Response.Listener<String> { response ->
            val resObject = JSONObject(response)

            try {
                val accesToken = resObject.getString(KEY_ACCESS_TOKEN)
                val spe = PreferenceManager.getDefaultSharedPreferences(applicationContext!!).edit()
                spe.putString(PREF_NAME_ACCESS_TOKEN, accesToken)

                if (resObject.getString(KEY_REFRESH_TOKEN) != null)
                    spe.putString(PREF_NAME_REFRESH_TOKEN, resObject.getString(KEY_REFRESH_TOKEN))
                spe.putLong(PREF_NAME_EXPIRES_AT, getRefreshTime(this, resObject.getLong(KEY_EXPIRES_IN)))

                spe.commit()

                val successIntent = Intent()
                successIntent.action = Constants.BROADCAST_USER_AUTH_SUCCEEDED
                this.sendBroadcast(successIntent)
            } catch (ex: Exception) {
                Timber.e(ex, GET_AUTH_TOKEN_URI)
                val errorIntent = Intent()
                errorIntent.action = Constants.BROADCAST_USER_AUTH_ERROR
                errorIntent.putExtra(Constants.KEY_NETWORK_REQUEST_ERROR, ex.message)
                this.sendBroadcast(errorIntent)
            }
        },
                Response.ErrorListener { error ->
                    Timber.e(error, GET_AUTH_TOKEN_URI)
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

                headers.put("grant_type", "authorization_code")
                headers.put("code", codeExtra!!)
                headers.put("redirect_uri", REDIRECT_URI)

                return headers
            }

            override fun getBodyContentType(): String {
                return "application/x-www-form-urlencoded; charset=UTF-8"
            }
        }

        NetworkSingleton.getInstance(this).addToRequestQueue(request)
    }

    private fun getListOfScopes() : List<String> {
        return listOf("identity","mysubreddits","read","report","submit","subscribe", "vote", "save", "mysubscriptions")
    }

    private fun getScopeString(scopes: List<String>) : String {
        if (scopes.isEmpty()) return ""
        var retString = ""
        scopes.forEach { x : String -> retString += x }
        return retString
    }
}