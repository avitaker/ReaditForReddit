package com.avinashdavid.readitforreddit.UI;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.avinashdavid.readitforreddit.MiscUtils.Constants;
import com.avinashdavid.readitforreddit.NetworkUtils.GetMorechildrenService;
import com.avinashdavid.readitforreddit.PostUtils.MoreChildrenCommentRecord;
import com.avinashdavid.readitforreddit.PostUtils.RedditListing;
import com.avinashdavid.readitforreddit.R;

import java.util.List;

/**
 * Created by avinashdavid on 5/4/17.
 */

public class FragmentMoreChildren extends DialogFragment {
    public static final String TAG_MORECHILDREN_FRAGMENT = "morechildFragment";
    private static final String KEY_PARENT_ID = "parentId";
    private static final String KEY_LINK_ID = "linkId";
    private static final String KEY_MORE_CHILDREN = "moreChildren";
    private static final String KEY_PARENT_DEPTH = "parentDepth";

    String mLinkId;
    String mParentId;
    String mChildren;
    int parentDepth;
    BroadcastReceiver mMoreChildrenReceiver;
    IntentFilter mIntentFilter;
    List<MoreChildrenCommentRecord> mMoreChildrenCommentRecords;
    View view;

    int width, height;

    RecyclerView mRecyclerView;
    MoreChildrenRecyclerAdapter mAdapter;

    public static FragmentMoreChildren getFragmentMoreChildren(String linkId, String parentId, String children, float parentDepth){
        Bundle bundle = new Bundle();
        bundle.putString(KEY_PARENT_ID, parentId);
        bundle.putString(KEY_LINK_ID, linkId);
        bundle.putString(KEY_MORE_CHILDREN, children);
        bundle.putInt(KEY_PARENT_DEPTH, (int)parentDepth);
        FragmentMoreChildren fragmentMoreChildren = new FragmentMoreChildren();
        fragmentMoreChildren.setArguments(bundle);
        return fragmentMoreChildren;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args!=null) {
            mLinkId = args.getString(KEY_LINK_ID);
            mParentId = args.getString(KEY_PARENT_ID);
            mChildren = args.getString(KEY_MORE_CHILDREN);
            parentDepth = args.getInt(KEY_PARENT_DEPTH);
        } else {
            mLinkId = savedInstanceState.getString(KEY_LINK_ID);
            mParentId = savedInstanceState.getString(KEY_PARENT_ID);
            mChildren = savedInstanceState.getString(KEY_MORE_CHILDREN);
            parentDepth = savedInstanceState.getInt(KEY_PARENT_DEPTH);
        }

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        width = size.x;
        height = size.y;

        Dialog dialog=new Dialog(getActivity(), R.style.MoreDialogTheme);

//        Window window = dialog.getWindow();
//        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        Window window = dialog.getWindow();
        window.setGravity(Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL);
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

//        WindowManager.LayoutParams params = window.getAttributes();
//        params.y = width;
////        params.y = 100;
//        window.setAttributes(params);

        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_more_children, container, false);
        mRecyclerView = (RecyclerView)view.findViewById(R.id.morechildren_recyclerview);
        mMoreChildrenReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Constants.BROADCAST_MORE_COMMENTS_LOADED)){
                    mMoreChildrenCommentRecords = MoreChildrenCommentRecord.listAll(MoreChildrenCommentRecord.class);
                    mAdapter = new MoreChildrenRecyclerAdapter(getActivity(), mMoreChildrenCommentRecords, RedditListing.find(RedditListing.class, "m_post_id = ?", mLinkId).get(0), mParentId, parentDepth);
                    mAdapter.setHasStableIds(true);
                    mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                    mRecyclerView.setAdapter(mAdapter);
                } else {
                    //TODO: HANDLE ERROR
                }
            }
        };
        mIntentFilter = new IntentFilter();

        if (MoreChildrenRecyclerAdapter.sLastMoreCommentsParent==null){
            GetMorechildrenService.loadMoreComments(getActivity(), mLinkId, mParentId, mChildren, 0);
        } else if (!MoreChildrenRecyclerAdapter.sLastMoreCommentsParent.equals(mParentId)){
            GetMorechildrenService.loadMoreComments(getActivity(), mLinkId, mParentId, mChildren, 0);
        } else {
            mMoreChildrenCommentRecords = MoreChildrenCommentRecord.listAll(MoreChildrenCommentRecord.class);
            mAdapter = new MoreChildrenRecyclerAdapter(getActivity(), mMoreChildrenCommentRecords, RedditListing.find(RedditListing.class, "m_post_id = ?", mLinkId).get(0), mParentId, parentDepth);
            mAdapter.setHasStableIds(true);
            mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
            mRecyclerView.setAdapter(mAdapter);
        }
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mIntentFilter.addAction(Constants.BROADCAST_MORE_COMMENTS_LOADED);
        mIntentFilter.addAction(Constants.BROADCAST_MORE_COMMENTS_ERROR);
        getActivity().registerReceiver(mMoreChildrenReceiver, mIntentFilter);
    }

    @Override
    public void onPause() {
        getActivity().unregisterReceiver(mMoreChildrenReceiver);
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(KEY_LINK_ID, mLinkId);
        outState.putString(KEY_PARENT_ID, mParentId);
        outState.putString(KEY_MORE_CHILDREN, mChildren);
        outState.putInt(KEY_PARENT_DEPTH, parentDepth);
    }
}
