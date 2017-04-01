package com.avinashdavid.readitforreddit.UI;

import android.app.Activity;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.SslErrorHandler;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.avinashdavid.readitforreddit.R;

import timber.log.Timber;

/**
 * Created by avinashdavid on 3/12/17.
 */

public class LinkWebviewFragment extends Fragment {
    private WebView mWebview;
    private String mUrlString;

    private static final String KEY_URL = "urlToOpen";

    public static Fragment newInstance(String url){
        Bundle bundle = new Bundle();
        bundle.putString(KEY_URL, url);

        LinkWebviewFragment newFragment = new LinkWebviewFragment();
        newFragment.setArguments(bundle);

        return newFragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUrlString = getArguments().getString(KEY_URL);
        Timber.d(mUrlString);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_webview, container, false);

        mWebview = (WebView) v.findViewById(R.id.link_webview);

        final Activity activity = getActivity();

        mWebview.getSettings().setJavaScriptEnabled(true); // enable javascript
        mWebview.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);
//        mWebview.getSettings().setLoadWithOverviewMode(true);
//        mWebview.getSettings().setUseWideViewPort(true);
//        mWebview.getSettings().setDomStorageEnabled(true);

        mWebview.setWebChromeClient(new WebChromeClient() {
            public void onProgressChanged(WebView view, int progress)
            {
                activity.setTitle("Loading...");
                activity.setProgress(progress * 100);

                if(progress == 100)
                    activity.setTitle(R.string.app_name);
            }
        });

        mWebview.setWebViewClient(new WebViewClient() {
            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl)
            {
                // Handle the error
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url)
            {
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                handler.proceed();
            }
        });

        mWebview.loadUrl(mUrlString);

        return super.onCreateView(inflater, container, savedInstanceState);
    }
}
