package com.avinashdavid.readitforreddit.UI;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.avinashdavid.readitforreddit.CommentsActivity;
import com.avinashdavid.readitforreddit.MainActivity;
import com.avinashdavid.readitforreddit.MiscUtils.GeneralUtils;
import com.avinashdavid.readitforreddit.PostUtils.CommentRecord;
import com.avinashdavid.readitforreddit.PostUtils.RedditListing;
import com.avinashdavid.readitforreddit.R;

import java.util.List;

import timber.log.Timber;

/**
 * Created by avinashdavid on 3/18/17.
 */

public class CommentRecordRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<CommentRecord> mCommentRecords;
//    private Cursor mCommentRecords;
    private RedditListing mRedditListing;
    private Activity mCommentsActivity;
    private LayoutInflater mLayoutInflater;
    private int lastPosition = -1;

    private static final int VIEW_TYPE_POST_INFO = 0;
    private static final int VIEW_TYPE_DEPTH_0 = 1;
    private static final int VIEW_TYPE_DEPTH_1 = 2;
    private static final int VIEW_TYPE_DEPTH_2 = 3;
    private static final int VIEW_TYPE_DEPTH_3 = 4;
    private static final int VIEW_TYPE_DEPTH_4 = 5;
    private static final int VIEW_TYPE_DEPTH_5 = 6;
    private static final int VIEW_TYPE_DEPTH_6 = 7;
    private static final int VIEW_TYPE_DEPTH_7 = 8;

    public CommentRecordRecyclerAdapter(@NonNull Context context, List<CommentRecord> commentRecords, RedditListing redditListing){
        try {
            this.mCommentsActivity = (CommentsActivity) context;
        } catch (ClassCastException e){
            this.mCommentsActivity = (Activity)context;
        }
        this.mLayoutInflater = LayoutInflater.from(mCommentsActivity);
        mCommentRecords = commentRecords;
        mRedditListing = redditListing;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int id = 0;
        switch (viewType){
            case VIEW_TYPE_POST_INFO:
                id = R.layout.post_details;
                break;
            case VIEW_TYPE_DEPTH_0:
                id = R.layout.item_comment;
                break;
            case VIEW_TYPE_DEPTH_1:
                id = R.layout.item_comment_1;
                break;
            case VIEW_TYPE_DEPTH_2:
                id = R.layout.item_comment_2;
                break;
            case VIEW_TYPE_DEPTH_3:
                id = R.layout.item_comment_3;
                break;
            case VIEW_TYPE_DEPTH_4:
                id = R.layout.item_comment_4;
                break;
            case VIEW_TYPE_DEPTH_5:
                id = R.layout.item_comment_5;
                break;
            case VIEW_TYPE_DEPTH_6:
                id = R.layout.item_comment_6;
                break;
            case VIEW_TYPE_DEPTH_7:
                id = R.layout.item_comment_7;
                break;
            default:
                id = R.layout.item_comment;
                break;
        }
        View view = mLayoutInflater.inflate(id, parent, false);
        if (viewType == VIEW_TYPE_POST_INFO){
            return new RedditPostRecyclerAdapter.ListingHolder(view);
        } else {
            return new CommentsHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        CommentRecord commentObject;
        if (MainActivity.usingTabletLayout){
            if (getItemViewType(position)==0){
                ((RedditPostRecyclerAdapter.ListingHolder) holder).bindListing(mRedditListing);
            } else {
                commentObject = mCommentRecords.get(position-1);
                ((CommentsHolder)holder).bindComment(commentObject, mRedditListing.author);
            }
        } else {
            if (position<mCommentRecords.size()){
                commentObject = mCommentRecords.get(position);
                ((CommentsHolder)holder).bindComment(commentObject, mRedditListing.author);
            }
        }
//        setAnimation(holder.itemView, position);
    }

    @Override
    public int getItemCount() {
        if (!MainActivity.usingTabletLayout) {
            return mCommentRecords == null ? 0 : mCommentRecords.size();
        } else {
            return mCommentRecords == null ? 1 : mCommentRecords.size()+1;
        }
    }

    @Override
    public void onViewDetachedFromWindow(RecyclerView.ViewHolder holder) {
//        ((CommentsHolder)holder).clearAnimation();
        super.onViewDetachedFromWindow(holder);
    }

    @Override
    public int getItemViewType(int position) {
        CommentRecord commentRecord;
        if (!MainActivity.usingTabletLayout) {
            if (position<mCommentRecords.size()) {
                commentRecord = mCommentRecords.get(position);
                return commentRecord.depth + 1;
            } else {
                commentRecord = mCommentRecords.get(position -1);
                return commentRecord.depth + 1;
            }
        } else {
            if (position==0){
                return VIEW_TYPE_POST_INFO;
            } else {
                commentRecord = mCommentRecords.get(position - 1);
                return commentRecord.depth + 1;
            }
        }
    }

    @Override
    public long getItemId(int position) {
        CommentRecord commentRecord;
        if (MainActivity.usingTabletLayout){
            if (position==0){
                return 0;
            } else {
                commentRecord = mCommentRecords.get(position - 1);
                return commentRecord.getId();
            }
        } else {
            if (position<mCommentRecords.size()) {
                commentRecord = mCommentRecords.get(position);
                return commentRecord.getId();
            } else {
                commentRecord = mCommentRecords.get(position - 1);
                return commentRecord.getId();
            }
        }
    }


    static class CommentsHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView author;
        TextView score;
        TextView time_elapsed;
        TextView bodyText;
        TextView flairText;

        private CommentRecord mCommentObject;
        private Context mContext;
        final Drawable authorBackground;
        final int goldColor;

        void bindComment(CommentRecord commentRecord, String postAuthor){
            mCommentObject = commentRecord;
            author.setText(commentRecord.author);
            if (commentRecord.author.equals(postAuthor)){
                GeneralUtils.setSDKSensitiveBackground(author, authorBackground);
                author.setTextColor(mContext.getResources().getColor(R.color.milk));
            } else {
                if (authorBackground!=null && author.getBackground()!=null) {
                    if (author.getBackground().equals(authorBackground)) {
                        GeneralUtils.setSDKSensitiveBackground(author, null);
                        author.setTextColor(GeneralUtils.getThemeAccentColor(mContext));
                    }
                }
            }
            if (commentRecord.scoreHidden){
                score.setText(mContext.getString(R.string.score_hidden));
            } else {
                score.setText(mContext.getString(R.string.format_points, commentRecord.score));
            }
            time_elapsed.setText(GeneralUtils.returnFormattedTime(mContext, System.currentTimeMillis(), commentRecord.createdTime));
            bodyText.setText(GeneralUtils.returnFormattedStringFromHtml(commentRecord.bodyHtml));
            bodyText.setMovementMethod(LinkMovementMethod.getInstance());
            if (commentRecord.authorFlairText!=null) {
                if (!commentRecord.authorFlairText.equals("null")) {
                    flairText.setVisibility(View.VISIBLE);
                    flairText.setText(GeneralUtils.returnFormattedStringFromHtml(commentRecord.authorFlairText));
                }
            }
            if (commentRecord.isGilded){
                author.setTextColor(GeneralUtils.getSDKSensitiveColor(mContext, R.color.gold));
            } else {
                if (author.getCurrentTextColor() == goldColor) {
                    author.setTextColor(GeneralUtils.getSDKSensitiveColor(mContext, android.R.color.white));
                }
            }
        }

        public CommentsHolder(View itemView) {
            super(itemView);
            author = (TextView)itemView.findViewById(R.id.author);
            score = (TextView)itemView.findViewById(R.id.score);
            time_elapsed = (TextView)itemView.findViewById(R.id.time_elapsed);
            bodyText = (TextView)itemView.findViewById(R.id.bodyHtml);
            flairText = (TextView)itemView.findViewById(R.id.flair_text);
            mContext = itemView.getContext();
            if (Build.VERSION.SDK_INT>=21) {
                authorBackground = mContext.getDrawable(R.drawable.background_post_author);
            } else {
                authorBackground = mContext.getResources().getDrawable(R.drawable.background_post_author);
            }
            goldColor = GeneralUtils.getSDKSensitiveColor(mContext, R.color.gold);
        }

        public void clearAnimation()
        {
            itemView.clearAnimation();
        }

        @Override
        public void onClick(View v) {
            Timber.d("Clicked on a recyclerview item");
        }
    }

    private void setAnimation(View viewToAnimate, int position)
    {
        // If the bound view wasn't previously displayed on screen, it's animated
        if (position > lastPosition)
        {
            Animation animation = AnimationUtils.loadAnimation(mCommentsActivity, R.anim.comments_anim);
            animation.setDuration(200);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }

}
