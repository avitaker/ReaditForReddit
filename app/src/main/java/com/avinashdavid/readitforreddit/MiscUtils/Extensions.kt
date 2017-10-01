package com.avinashdavid.readitforreddit.MiscUtils

import android.content.Context
import android.content.IntentFilter
import android.graphics.Bitmap
import android.net.http.SslError
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.webkit.*
import android.widget.Toast

/**
 * Created by avinashdavid on 8/30/17.
 */
fun RecyclerView.prepareLinear(context: Context, showDividers: Boolean = false) {
    layoutManager = LinearLayoutManager(context)
    if (showDividers) {
        val lm = layoutManager as LinearLayoutManager
        addItemDecoration(DividerItemDecoration(context, lm.orientation))
    }
}

fun WebView.configureSafeWebview() {
    setWebViewClient(object: WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
            return false
        }

        override fun onReceivedSslError(view: WebView?, handler: SslErrorHandler?, error: SslError?) {
            if (handler != null ) handler.proceed()
            //TODO: Actually handle ssl error here
            Toast.makeText(context, "SSL ERROR", Toast.LENGTH_LONG).show()
        }
    })

    setWebChromeClient(WebChromeClient())

    settings.javaScriptEnabled = true
    settings.useWideViewPort = true
}

fun IntentFilter.addActions(actions: List<String>) {
    actions.forEach { action -> addAction(action) }
}