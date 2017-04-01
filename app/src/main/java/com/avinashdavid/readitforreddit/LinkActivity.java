package com.avinashdavid.readitforreddit;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import com.avinashdavid.readitforreddit.UI.LinkWebviewFragment;
import com.avinashdavid.readitforreddit.UI.VideoEnabledWebChromeClient;
import com.avinashdavid.readitforreddit.UI.VideoEnabledWebView;

/**
 * Created by avinashdavid on 3/12/17.
 */

public class LinkActivity extends FragmentActivity {
//    private String mUrlString;
//    public static final String EXTRA_URL = "linkUrl";
//    private WebView mWebview;
//
//    @Override
//    protected void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_link);
//
//        mUrlString = getIntent().getStringExtra(EXTRA_URL);
//
//        if (mUrlString == null){
//            //TODO: make empty view
//        } else {
////            Fragment fragment = LinkWebviewFragment.newInstance(mUrlString);
////            android.support.v4.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
////
////// Replace whatever is in the fragment_container view with this fragment,
////// and add the transaction to the back stack
////            transaction.replace(R.id.link_fragment_container, fragment);
////            transaction.addToBackStack(null);
////
////// Commit the transaction
////            transaction.commit();
//            mWebview = (WebView) findViewById(R.id.link_webview);
//
//            final Activity activity = LinkActivity.this;
//
//            WebSettings webSettings = mWebview.getSettings();
//            webSettings.setJavaScriptEnabled(true); // enable javascript
//            mWebview.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);
//            webSettings.setBuiltInZoomControls(true);
//            webSettings.setPluginState(WebSettings.PluginState.ON);
//            webSettings.setUseWideViewPort(true);
////        mWebview.getSettings().setLoadWithOverviewMode(true);
////        mWebview.getSettings().setUseWideViewPort(true);
////        mWebview.getSettings().setDomStorageEnabled(true);
//
//            mWebview.setWebChromeClient(new WebChromeClient() {
//                public void onProgressChanged(WebView view, int progress)
//                {
//                    activity.setTitle("Loading...");
//                    activity.setProgress(progress * 100);
//
//                    if(progress == 100)
//                        activity.setTitle(R.string.app_name);
//                }
//            });
//
//            mWebview.setWebViewClient(new WebViewClient()
//            {
//                @Override
//                public void onReceivedError(WebView view, int errorCode, String description, String failingUrl)
//                {
//                    // Handle the error
//                }
//
//                @Override
//                public boolean shouldOverrideUrlLoading(WebView view, String url)
//                {
//                    view.loadUrl(url);
//                    return true;
//                }
//
//                @Override
//                public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
//                    handler.proceed();
//                }
//            }
//            );
//
//            mWebview.setWebChromeClient(new WebChromeClient());
//
//            mWebview.loadUrl(mUrlString);
//        }
//    }

    private String mUrlString;
    public static final String EXTRA_URL = "linkUrl";
    private VideoEnabledWebView webView;
    private VideoEnabledWebChromeClient webChromeClient;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        mUrlString = getIntent().getStringExtra(EXTRA_URL);

        // Set layout
        setContentView(R.layout.activity_video_enabled_webview);

        // Save the web view
        webView = (VideoEnabledWebView) findViewById(R.id.webView);

        // Initialize the VideoEnabledWebChromeClient and set event handlers
        View nonVideoLayout = findViewById(R.id.nonVideoLayout); // Your own view, read class comments
        ViewGroup videoLayout = (ViewGroup) findViewById(R.id.videoLayout); // Your own view, read class comments
//        View loadingView = getLayoutInflater().inflate(R.layout.view_loading_video, null); // Your own view, read class comments
        webChromeClient = new VideoEnabledWebChromeClient(nonVideoLayout, videoLayout, null, webView) // See all available constructors...
        {
            // Subscribe to standard events, such as onProgressChanged()...
            @Override
            public void onProgressChanged(WebView view, int progress)
            {
                // Your code...
            }
        };
        webChromeClient.setOnToggledFullscreen(new VideoEnabledWebChromeClient.ToggledFullscreenCallback()
        {
            @Override
            public void toggledFullscreen(boolean fullscreen)
            {
                // Your code to handle the full-screen change, for example showing and hiding the title bar. Example:
                if (fullscreen)
                {
                    WindowManager.LayoutParams attrs = getWindow().getAttributes();
                    attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
                    attrs.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
                    getWindow().setAttributes(attrs);
                    if (android.os.Build.VERSION.SDK_INT >= 14)
                    {
                        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
                    }
                }
                else
                {
                    WindowManager.LayoutParams attrs = getWindow().getAttributes();
                    attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
                    attrs.flags &= ~WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
                    getWindow().setAttributes(attrs);
                    if (android.os.Build.VERSION.SDK_INT >= 14)
                    {
                        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                    }
                }

            }
        });
        webView.setWebChromeClient(webChromeClient);

        // Navigate everywhere you want, this classes have only been tested on YouTube's mobile site
        webView.loadUrl(mUrlString);
    }

    @Override
    public void onBackPressed()
    {
        // Notify the VideoEnabledWebChromeClient, and handle it ourselves if it doesn't handle it
        if (!webChromeClient.onBackPressed())
        {
            if (webView.canGoBack())
            {
                webView.goBack();
            }
            else
            {
                // Close app (presumably)
                super.onBackPressed();
            }
        }
    }
}
