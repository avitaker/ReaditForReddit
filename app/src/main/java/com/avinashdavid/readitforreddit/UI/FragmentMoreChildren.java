package com.avinashdavid.readitforreddit.UI;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.avinashdavid.readitforreddit.MiscUtils.Constants;
import com.avinashdavid.readitforreddit.NetworkUtils.GetMorechildrenService;
import com.avinashdavid.readitforreddit.PostUtils.CommentRecord;
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
    CommentRecord mParentRecord;

    BroadcastReceiver mMoreChildrenReceiver;
    IntentFilter mIntentFilter;
    List<MoreChildrenCommentRecord> mMoreChildrenCommentRecords;
    View view;
    LinearLayoutManager mLinearLayoutManager;
    View progressBar;

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
//        mParentRecord = CommentRecord.find(CommentRecord.class, "comment_id = ?", mParentId).get(0);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Dialog dialog=new Dialog(getActivity(), R.style.MoreDialogTheme);

        Window window = dialog.getWindow();
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_more_children, container, false);
        mRecyclerView = (RecyclerView)view.findViewById(R.id.morechildren_recyclerview);
        progressBar = view.findViewById(R.id.loadingPanel);
        mLinearLayoutManager = new LinearLayoutManager(getActivity());
//        mLinearLayoutManager.setStackFromEnd(true);
        mMoreChildrenReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(Constants.BROADCAST_MORE_COMMENTS_LOADED)){
                    initUi();

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
            initUi();
        }
        view.findViewById(R.id.btn_close_more).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
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

    private void initUi(){
        progressBar.setVisibility(View.GONE);
        mMoreChildrenCommentRecords = MoreChildrenCommentRecord.listAll(MoreChildrenCommentRecord.class);
        mAdapter = new MoreChildrenRecyclerAdapter(getActivity(), mMoreChildrenCommentRecords, RedditListing.find(RedditListing.class, "m_post_id = ?", mLinkId).get(0), mParentId, parentDepth);
        mAdapter.setHasStableIds(true);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);
        mRecyclerView.setAdapter(mAdapter);
    }
}
