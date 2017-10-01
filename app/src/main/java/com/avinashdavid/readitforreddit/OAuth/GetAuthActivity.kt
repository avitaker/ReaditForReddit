package com.avinashdavid.readitforreddit.OAuth

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.net.Uri
import android.net.http.SslError
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.webkit.SslErrorHandler
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import com.avinashdavid.readitforreddit.MiscUtils.Constants
import com.avinashdavid.readitforreddit.MiscUtils.addActions
import com.avinashdavid.readitforreddit.MiscUtils.configureSafeWebview
import com.avinashdavid.readitforreddit.R
import java.util.*
import kotlinx.android.synthetic.main.dialog_get_reddit_auth.*


/**
 * Created by avinashdavid on 9/17/17.
 */
private const val KEY_AUTH_STATE_STRING = "KEY_AUTH_STATE_STRING"
public const val ROAM_CLIENT_ID = "nY6_3zi8tr6TdA"
class GetAuthActivity : AppCompatActivity() {
    companion object {
        const val KEY_ACCESS_TOKEN = "KEY_ACCESS_TOKEN"
    }

    private val AUTH_BASE_URI = "https://www.reddit.com/api/v1/authorize.compact"
    private val TOKEN_RETREIVAL_BASE_URI = "https://www.reddit.com/api/v1/revoke_token"

    private val PARAM_CLIENT_ID = "client_id"
    private val PARAM_RESPONSE_TYPE = "response_type"
    private val PARAM_STATE = "state"
    private val PARAM_REDIRECT_URI = "redirect_uri"
    private val PARAM_SCOPE = "scope"

    private val VALUE_RESPONSE_TYPE = "code"
    private val VALUE_REDIRECT_URI = "http://avinashdavid.com"
    private val VALUE_SCOPE = listOf("identity", "edit", "flair", "history", "privatemessages", "read", "report", "save", "submit", "subscribe", "vote", "mysubreddits")

    private val INVALID_RESPONSES = listOf("access_denied","unsupported_response_type","invalid_scope","invalid_request")

    val getTokenBroadcastReciever = object: BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
//            TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            val action = intent?.action
            if (action == Constants.BROADCAST_USER_AUTH_SUCCEEDED) {
                setResult(Activity.RESULT_OK)
                finish()
            }
        }
    }

    var getTokenIntentFilter = IntentFilter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.dialog_get_reddit_auth)

        val randomString = UUID.randomUUID().toString()
        PreferenceManager.getDefaultSharedPreferences(this).edit().putString(KEY_AUTH_STATE_STRING, randomString).apply()

        val authUrl = getInitialUrl(randomString)
//        setSupportActionBar(my_toolbar)
//        my_toolbar.title = "Profile"
//        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
//        supportActionBar!!.setHomeButtonEnabled(true)

        wvAuth.configureSafeWebview()
        wvAuth.setWebViewClient(object: WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                return false
            }

            override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
                if (handler != null ) handler.proceed()
                //TODO: Actually handle ssl error here
                Toast.makeText(applicationContext, "SSL ERROR", Toast.LENGTH_LONG).show()
            }

            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
//                if (url == null) return
                if (url!!.contains("avinashdavid.com") && !url.contains("reddit.com")) {
                    Log.d("AUTHENTICATED", url)
                    INVALID_RESPONSES.forEach { x -> if (url.contains(x)) {
                            setResult(Activity.RESULT_CANCELED)
                            finish()
                            return
                        }
                    }
                    val posState = url.indexOf("state=")
                    val posAfterState = url.indexOf('&', posState)
                    val preState = url.substring(posState, posAfterState)
                    val state = preState.substringAfter("state=")

                    val code = url.substringAfter("code=")
                    Log.d("AUTHENTICATED", code)
                    GetUserAuthService.loadGetUserAuthService(this@GetAuthActivity, code, state)
                    return
                }
                super.onPageStarted(view, url, favicon)
            }
        })
        wvAuth.loadUrl(authUrl)
    }

    override fun onResume() {
        super.onResume()
        setupReciever()
    }

    override fun onPause() {
        super.onPause()
        stopReciever()
    }

    fun getInitialUrl(stateString: String): String {
        val url = Uri.parse(AUTH_BASE_URI)
        val builder= url.buildUpon()
        builder.appendQueryParameter(PARAM_CLIENT_ID, ROAM_CLIENT_ID)
        builder.appendQueryParameter(PARAM_RESPONSE_TYPE, VALUE_RESPONSE_TYPE)
        builder.appendQueryParameter(PARAM_STATE, stateString)


//        builder.appendQueryParameter(PARAM_REDIRECT_URI, VALUE_REDIRECT_URI).appendQueryParameter(PARAM_SCOPE, getCommaSeparatedString(VALUE_SCOPE))
//        return builder.build().toString()

        var intermediate = builder.build().toString()
        intermediate += ("&" + PARAM_REDIRECT_URI + "=" + VALUE_REDIRECT_URI) + ("&duration=permanent") + ("&scope=" + getCommaSeparatedString(VALUE_SCOPE))
        return intermediate
    }

    fun getCommaSeparatedString(strings: List<String>): String {
        var retString = ""
        strings.forEach { x -> retString+= (x + ",") }
        return retString.substring(0, retString.length - 1)
    }

    fun setupReciever(){
        getTokenIntentFilter.addActions(listOf(Constants.BROADCAST_USER_AUTH_SUCCEEDED, Constants.BROADCAST_USER_AUTH_ERROR))
        registerReceiver(getTokenBroadcastReciever, getTokenIntentFilter)
    }

    fun stopReciever() {
        unregisterReceiver(getTokenBroadcastReciever)
    }
}