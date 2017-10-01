package com.avinashdavid.readitforreddit.OAuth

import android.content.Context
import android.preference.PreferenceManager
import com.avinashdavid.readitforreddit.OAuth.GetUserAuthService.Companion.PREF_NAME_ACCESS_TOKEN
import java.util.HashMap

/**
 * Created by avinashdavid on 9/30/17.
 */
object OauthUtils {
    fun getUserAuthHeader(context: Context): HashMap<String, String> {
        val authToken = PreferenceManager.getDefaultSharedPreferences(context.applicationContext).getString(PREF_NAME_ACCESS_TOKEN, null)
        val headers = HashMap<String, String>()

        if (authToken != null) {
            headers.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
            headers.put("Authorization", "bearer " + authToken)
        }
        return headers
    }
}