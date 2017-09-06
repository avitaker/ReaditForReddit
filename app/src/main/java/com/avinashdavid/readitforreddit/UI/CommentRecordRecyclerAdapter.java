
package com.avinashdavid.readitforreddit.UI;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.text.method.LinkMovementMethod;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.avinashdavid.readitforreddit.CommentsActivity;
import com.avinashdavid.readitforreddit.MainActivity;
import com.avinashdavid.readitforreddit.MiscUtils.GeneralUtils;
import com.avinashdavid.readitforreddit.NetworkUtils.GetCommentsService;
import com.avinashdavid.readitforreddit.NetworkUtils.GetMorechildrenService;
import com.avinashdavid.readitforreddit.PostUtils.CommentRecord;
import com.avinashdavid.readitforreddit.PostUtils.RedditListing;
import com.avinashdavid.readitforreddit.R;
import com.avinashdavid.readitforreddit.User.UserHistoryActivity;

import java.util.List;

import timber.log.Timber;

/**
 * Created by avinashdavid on 3/18/17.
 * Adapter to display comments in a recyclerview, with ability to be used in MainActivity for tablet layout
 */

public class CommentRecordRecyclerAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<CommentRecord> mCommentRecords;
    //    private Cursor mCommentRecords;
    private RedditListing mRedditListing;
    private Activity mCommentsActivity;
    private boolean mIsCommentThread;
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
    private static final int VIEW_TYPE_DEPTH_8 = 9;
    private static final int VIEW_TYPE_DEPTH_9 = 10;
    private static final int VIEW_TYPE_DEPTH_10 = 11;
    private static final int VIEW_TYPE_AUTHOR = 47;
    private static final int VIEW_TYPE_VIEW_FULL_COMMENTS = 12;
    private BroadcastReceiver mMoreReceiver;
    private IntentFilter mIntentFilter;

    public CommentRecordRecyclerAdapter(@NonNull Context context, List<CommentRecord> commentRecords, RedditListing redditListing, boolean isCommentThread){
        try {
            this.mCommentsActivity = (CommentsActivity) context;
        } catch (ClassCastException e){
            this.mCommentsActivity = (Activity)context;
        }
        this.mLayoutInflater = LayoutInflater.from(mCommentsActivity);
        this.mCommentRecords = commentRecords;
        this.mRedditListing = redditListing;
        mIsCommentThread = isCommentThread;
        this.mMoreReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String parent = intent.getStringExtra(GetMorechildrenService.KEY_PARENT_ID);
                int startIndex = intent.getIntExtra(GetMorechildrenService.KEY_INSERT_START_POSITION, Integer.MAX_VALUE);
                List<CommentRecord> newRecords = CommentRecord.find(CommentRecord.class, "parent = ?", parent);
                int itemsInserted = newRecords.size();
                Timber.d(Integer.toString(itemsInserted));
                mCommentRecords.addAll(startIndex, newRecords);
                notifyItemRangeInserted(startIndex, itemsInserted);
            }
        };
        mIntentFilter = new IntentFilter();
    }

//    @Override
//    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
//        super.onAttachedToRecyclerView(recyclerView);
//        mIntentFilter.addAction(Constants.BROADCAST_MORE_COMMENTS_LOADED);
//        mIntentFilter.addAction(Constants.BROADCAST_MORE_COMMENTS_ERROR);
//        mCommentsActivity.registerReceiver(mMoreReceiver, mIntentFilter);
//    }
//
//    @Override
//    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
//        mCommentsActivity.unregisterReceiver(mMoreReceiver);
//        super.onDetachedFromRecyclerView(recyclerView);
//    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int id = 0;
        switch (viewType){
            case VIEW_TYPE_POST_INFO:
                id = R.layout.post_details;
                break;
            case VIEW_TYPE_DEPTH_0:
                id = R.layout.item_comment_0;
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
            case VIEW_TYPE_DEPTH_8:
                id = R.layout.item_comment_8;
                break;
            case VIEW_TYPE_DEPTH_9:
                id = R.layout.item_comment_9;
                break;
            case VIEW_TYPE_DEPTH_10:
                id = R.layout.item_comment_10;
                break;
            case -VIEW_TYPE_DEPTH_0:
                id = R.layout.item_comment_0;
                break;
            case -VIEW_TYPE_DEPTH_1:
                id = R.layout.item_comment_1;
                break;
            case -VIEW_TYPE_DEPTH_2:
                id = R.layout.item_comment_2;
                break;
            case -VIEW_TYPE_DEPTH_3:
                id = R.layout.item_comment_3;
                break;
            case -VIEW_TYPE_DEPTH_4:
                id = R.layout.item_comment_4;
                break;
            case -VIEW_TYPE_DEPTH_5:
                id = R.layout.item_comment_5;
                break;
            case -VIEW_TYPE_DEPTH_6:
                id = R.layout.item_comment_6;
                break;
            case -VIEW_TYPE_DEPTH_7:
                id = R.layout.item_comment_7;
                break;
            case -VIEW_TYPE_DEPTH_8:
                id = R.layout.item_comment_8;
                break;
            case -VIEW_TYPE_DEPTH_9:
                id = R.layout.item_comment_9;
                break;
            case -VIEW_TYPE_DEPTH_10:
                id = R.layout.item_comment_10;
                break;
            case VIEW_TYPE_VIEW_FULL_COMMENTS:
                id = R.layout.item_view_full_comments;
                break;
            default:
                id = R.layout.item_comment;
                break;
        }
        View view = mLayoutInflater.inflate(id, parent, false);
        if (viewType == VIEW_TYPE_POST_INFO){
            return new RedditPostRecyclerAdapter.ListingHolder(view, true);
        } else if (viewType < 0) {
            return new MoreHolder(view);
        } else if (viewType == VIEW_TYPE_VIEW_FULL_COMMENTS) {
            return new FullThreadHolder(view);
        } else {
            return new CommentsHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == VIEW_TYPE_VIEW_FULL_COMMENTS) {
            ((FullThreadHolder)holder).bindHolder(mRedditListing.mPostId);
            return;
        }
        CommentRecord commentObject;
        if (MainActivity.usingTabletLayout){
            if (getItemViewType(position)==0){
                ((RedditPostRecyclerAdapter.ListingHolder) holder).bindListing(mRedditListing, false);
            } else {
                commentObject = mCommentRecords.get(position - 1);
                if (getItemViewType(position)<0){
                    ((MoreHolder) holder).bindMore(commentObject, position);
                } else {
                    ((CommentsHolder) holder).bindComment(commentObject, mRedditListing != null? mRedditListing.author : "");
                }
            }
        } else {
            if (position<mCommentRecords.size()){
                commentObject = mCommentRecords.get(position);
                if (getItemViewType(position)<0){
                    ((MoreHolder) holder).bindMore(commentObject, position);
                } else {
                    ((CommentsHolder) holder).bindComment(commentObject, mRedditListing != null? mRedditListing.author : "");
                }
            }
        }
//        setAnimation(holder.itemView, position);
    }

    @Override
    public int getItemCount() {
        if (!MainActivity.usingTabletLayout) {
            if (!mIsCommentThread) return mCommentRecords == null ? 0 : mCommentRecords.size();
            else return mCommentRecords == null? 1 : mCommentRecords.size() + 1;
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
            if (mIsCommentThread && position == getItemCount() - 1) {
                return VIEW_TYPE_VIEW_FULL_COMMENTS;
            }
            if (position<mCommentRecords.size()) {
                commentRecord = mCommentRecords.get(position);
            } else {
                commentRecord = mCommentRecords.get(position -1);
            }
            if (commentRecord.depth!= GetCommentsService.DEPTH_MORE) {
                return commentRecord.depth + 1;
            } else {
                return (int)-commentRecord.createdTime - 1;
            }
        } else {
            if (position==0){
                return VIEW_TYPE_POST_INFO;
            } else {
                commentRecord = mCommentRecords.get(position - 1);
                if (commentRecord.depth != GetCommentsService.DEPTH_MORE) {
                    return commentRecord.depth + 1;
                } else {
                    return (int)-commentRecord.createdTime - 1;
                }
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

    static class MoreHolder extends RecyclerView.ViewHolder{
        View infoBox;
        TextView moreTextview;
        Context mContext;
        CommentRecord mMoreObject;


        public MoreHolder(View itemView) {
            super(itemView);
            mContext = itemView.getContext();
            this.moreTextview = (TextView)itemView.findViewById(R.id.author);
            this.infoBox = itemView.findViewById(R.id.bodyHtml);
            moreTextview.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            moreTextview.setTypeface(null, Typeface.ITALIC);
            infoBox.setVisibility(View.GONE);
        }

        void bindMore(final CommentRecord commentRecord, final int position){
            this.mMoreObject = commentRecord;
            moreTextview.setText(mContext.getString(R.string.load_more_comments));

            moreTextview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final String linkId = commentRecord.linkId;
                    final String parentId = commentRecord.parent;
                    final String children = commentRecord.bodyHtml;
                    final float depth = commentRecord.createdTime;
//                    Timber.d("load more comments for LINK: " + link_id + " and PARENT: " + parentId);
                    FragmentMoreChildren fragmentViewImage = FragmentMoreChildren.getFragmentMoreChildren(linkId, parentId, children, depth);
                    FragmentManager fragmentManager = ((AppCompatActivity)mContext).getSupportFragmentManager();
                    FragmentTransaction ft = fragmentManager.beginTransaction();
                    Fragment prev = fragmentManager.findFragmentByTag(FragmentMoreChildren.TAG_MORECHILDREN_FRAGMENT);
                    if (prev != null) {
                        ft.remove(prev);
                    }
                    ft.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                    ft.addToBackStack(null);

                    fragmentViewImage.show(ft, FragmentMoreChildren.TAG_MORECHILDREN_FRAGMENT);
//                    GetMorechildrenService.loadMoreComments(mContext, link_id, parentId, position);
                }
            });
        }
    }

    static class FullThreadHolder extends RecyclerView.ViewHolder {
        private Context mContext;

        public FullThreadHolder(View itemView) {
            super(itemView);
            mContext = itemView.getContext();
        }

        public void bindHolder(final String postId) {
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CommentsActivity.startCommentActivity(mContext, postId);
                    ((AppCompatActivity)mContext).finish();
                }
            });
        }
    }

    static class CommentsHolder extends RecyclerView.ViewHolder{
        TextView author;
        TextView score;
        TextView time_elapsed;
        TextView bodyText;
        View infoBox;
        TextView flairText;
        View containerView;

        private CommentRecord mCommentObject;
        private Context mContext;
        final Drawable authorBackground;
        final int goldColor;

        public CommentsHolder(View itemView) {
            super(itemView);
            author = (TextView)itemView.findViewById(R.id.author);
            score = (TextView)itemView.findViewById(R.id.score);
            time_elapsed = (TextView)itemView.findViewById(R.id.time_elapsed);
            bodyText = (TextView)itemView.findViewById(R.id.bodyHtml);
            infoBox = itemView.findViewById(R.id.info_linear_layout);
            flairText = (TextView)itemView.findViewById(R.id.flair_text);
            mContext = itemView.getContext();
            if (Build.VERSION.SDK_INT>=21) {
                authorBackground = mContext.getDrawable(R.drawable.background_post_author);
            } else {
                authorBackground = mContext.getResources().getDrawable(R.drawable.background_post_author);
            }
            goldColor = GeneralUtils.getSDKSensitiveColor(mContext, R.color.gold);
        }

        void bindComment(final CommentRecord commentRecord, String postAuthor){
            mCommentObject = commentRecord;
            author.setText(commentRecord.author);
            if (commentRecord.author.equals(postAuthor)) {
                GeneralUtils.setSDKSensitiveBackground(author, authorBackground);
                author.setTextColor(mContext.getResources().getColor(R.color.milk));
            } else {
                if (authorBackground != null && author.getBackground() != null) {
                    if (author.getBackground().equals(authorBackground)) {
                        GeneralUtils.setSDKSensitiveBackground(author, null);
                        author.setTextColor(GeneralUtils.getThemeAccentColor(mContext));
                    }
                }
            }
            if (commentRecord.scoreHidden) {
                score.setText(mContext.getString(R.string.score_hidden));
            } else {
                score.setText(mContext.getString(R.string.format_points, commentRecord.score));
            }
            String rawTime = GeneralUtils.returnFormattedTime(mContext, System.currentTimeMillis(), commentRecord.createdTime);
            if (commentRecord.isEdited) {
                rawTime = rawTime.concat("*");
            }
            time_elapsed.setText(rawTime);
            bodyText.setText(GeneralUtils.returnFormattedStringFromHtml(commentRecord.bodyHtml));
            bodyText.setMovementMethod(LinkMovementMethod.getInstance());
            if (commentRecord.authorFlairText != null) {
                if (!commentRecord.authorFlairText.equals("null")) {
                    flairText.setVisibility(View.VISIBLE);
                    flairText.setText(GeneralUtils.returnFormattedStringFromHtml(commentRecord.authorFlairText));
                }
            }
            if (commentRecord.isGilded) {
                author.setTextColor(GeneralUtils.getSDKSensitiveColor(mContext, R.color.gold));
            } else {
                if (author.getCurrentTextColor() == goldColor) {
                    if (commentRecord.author.equals(postAuthor)) {
                        author.setTextColor(GeneralUtils.getSDKSensitiveColor(mContext, android.R.color.white));
                    } else {
                        author.setTextColor(GeneralUtils.getThemeAccentColor(mContext));
                    }
                }
            }

            author.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    UserHistoryActivity.Companion.startUserHistoryActivity(mContext, commentRecord.author);
                }
            });
        }


        public void clearAnimation()
        {
            itemView.clearAnimation();
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