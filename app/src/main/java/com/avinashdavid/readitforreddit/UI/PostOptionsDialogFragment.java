package com.avinashdavid.readitforreddit.UI;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.TextView;

import com.avinashdavid.readitforreddit.MainActivity;
import com.avinashdavid.readitforreddit.PostUtils.RedditListing;
import com.avinashdavid.readitforreddit.R;

/**
 * Created by avinashdavid on 4/13/17.
 * Dialog fragment that displays actions that can be taken on a listing (like sharing and open link)
 */

public class PostOptionsDialogFragment extends DialogFragment {
    String subredditName;
    String linkUrl;
    String mTitle;
    SharedPreferences sp;

    public static final String TAG_POST_OPTIONS = "tagPostOptionsFrag";

    private static final String KEY_SUB = "keysub";
    private static final String KEY_LINK = "keylink";
    private static final String KEY_TITLE = "keyTitle";

    static PostOptionsDialogFragment newInstance(String postTitle, String linkUrl, String subredditName){
        PostOptionsDialogFragment fragment = new PostOptionsDialogFragment();
        Bundle args = new Bundle();
        args.putString(KEY_SUB, subredditName);
        args.putString(KEY_LINK, linkUrl);
        args.putString(KEY_TITLE, postTitle);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args==null){
            return;
        }
        subredditName = args.getString(KEY_SUB);
        linkUrl = args.getString(KEY_LINK);
        mTitle = args.getString(KEY_TITLE);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View v = getActivity().getLayoutInflater().inflate(R.layout.dialog_post_options, null);
        TextView openLink = (TextView)v.findViewById(R.id.view_post);
        openLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(linkUrl));
                startActivity(browserIntent);
                dismiss();
            }
        });

        TextView shareLink = (TextView)v.findViewById(R.id.share_post);
        shareLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_SUBJECT, mTitle);
                sendIntent.putExtra(Intent.EXTRA_TEXT, linkUrl);
                sendIntent.setType("text/plain");
                startActivity(sendIntent);
                dismiss();
            }
        });

        TextView viewSubreddit = (TextView)v.findViewById(R.id.view_subreddit);
        sp = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        try {
            if (sp.getString(getString(R.string.pref_current_subreddit), null).equals(subredditName)) {
                viewSubreddit.setVisibility(View.GONE);
            } else {
                viewSubreddit.setText(getString(R.string.view_subreddit, getString(R.string.format_subreddit, subredditName)));
                viewSubreddit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            if (!sp.getString(getString(R.string.pref_current_subreddit), null).equals(subredditName)) {
                                sp.edit().putString(getString(R.string.pref_current_subreddit), subredditName).commit();
                                RedditListing.deleteAll(RedditListing.class);
                                Intent intent = new Intent(getActivity(), MainActivity.class);
                                intent.putExtra(MainActivity.EXTRA_SUBREDDIT_NAME, subredditName);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);
                            }
                        } catch (NullPointerException e) {
                            sp.edit().putString(getString(R.string.pref_current_subreddit), subredditName).commit();
                            RedditListing.deleteAll(RedditListing.class);
                            Intent intent = new Intent(getActivity(), MainActivity.class);
                            intent.putExtra(MainActivity.EXTRA_SUBREDDIT_NAME, subredditName);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                        }
                        dismiss();
                    }
                });
            }
        } catch (NullPointerException e){
            viewSubreddit.setText(getString(R.string.view_subreddit, getString(R.string.format_subreddit, subredditName)));
            viewSubreddit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sp.edit().putString(getString(R.string.pref_current_subreddit), subredditName).commit();
                    RedditListing.deleteAll(RedditListing.class);
                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    intent.putExtra(MainActivity.EXTRA_SUBREDDIT_NAME, subredditName);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    dismiss();
                }
            });
        }
        return new AlertDialog.Builder(getActivity())
                .setTitle(mTitle)
                .setView(v)
                .create();
    }
}
