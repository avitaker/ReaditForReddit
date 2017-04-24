package com.avinashdavid.readitforreddit.UI;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.avinashdavid.readitforreddit.ManageSubredditsActivity;
import com.avinashdavid.readitforreddit.R;
import com.avinashdavid.readitforreddit.SubredditUtils.SubredditObject;

import java.util.ArrayList;

import timber.log.Timber;

/**
 * Created by avinashdavid on 4/23/17.
 */

public class SubredditRecyclerviewAdapter extends RecyclerView.Adapter {
    private ArrayList<String> mSubredditStringList;
    Context mContext;

    public SubredditRecyclerviewAdapter(@NonNull Context context, ArrayList<String> subredditObjects){
        this.mSubredditStringList = new ArrayList<>();
        this.mSubredditStringList.addAll(subredditObjects);
        this.mContext = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_subreddit, parent, false);
        return new SubredditViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        SubredditViewHolder subredditViewHolder = (SubredditViewHolder)holder;
        if (mSubredditStringList.size()>0) {
            String subredditString = mSubredditStringList.get(position);
            subredditViewHolder.subredditName.setText(subredditString);
            final int toPass = holder.getAdapterPosition();
            subredditViewHolder.deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Timber.d("click delete");
                    boolean removed = ((ManageSubredditsActivity)mContext).removeItem(toPass);
                    if (removed){
                        mSubredditStringList.remove(toPass);
                        notifyItemRemoved(toPass);
                        notifyItemRangeChanged(toPass, mSubredditStringList.size());
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mSubredditStringList ==null? 0: mSubredditStringList.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    static class SubredditViewHolder extends RecyclerView.ViewHolder {
        public TextView subredditName;
        public ImageButton deleteButton;
        private SubredditObject mSubredditObject;

        public SubredditViewHolder(View itemView){
            super(itemView);
            subredditName = (TextView)itemView.findViewById(R.id.subreddit_name);
            deleteButton = (ImageButton)itemView.findViewById(R.id.delete_subreddit_button);
        }
    }
}
