package com.avinashdavid.readitforreddit.User

import android.content.Context
import android.support.v4.app.Fragment
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.avinashdavid.readitforreddit.CommentsActivity
import com.avinashdavid.readitforreddit.MiscUtils.GeneralUtils
import com.avinashdavid.readitforreddit.MiscUtils.ScrollListener
import com.avinashdavid.readitforreddit.R
import com.avinashdavid.readitforreddit.UI.RedditPostRecyclerAdapter
import com.orm.SugarRecord
import java.util.*

/**
 * Created by avinashdavid on 9/1/17.
 */
class UserHistoryAdapter(context: Context, things: MutableList<SugarRecord>, fragment: Fragment): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object {
        const val VIEW_TYPE_COMMENT = 0
        const val VIEW_TYPE_LISTING = 1
    }

    var listOfThings = things
    val mContext = context
    var mScrollListener = fragment as ScrollListener

    override fun getItemViewType(position: Int): Int {
        val thing = listOfThings[position]
        if (thing is UserHistoryComment) return VIEW_TYPE_COMMENT
        else if (thing is UserHistoryListing) return VIEW_TYPE_LISTING
        else return VIEW_TYPE_COMMENT
    }

    override fun getItemCount(): Int {
        return listOfThings.size
    }

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
        var view: View? = null
        when (viewType) {
            VIEW_TYPE_COMMENT -> {
                view = LayoutInflater.from(mContext).inflate(R.layout.item_user_comment, parent, false)
                return CommentViewholder(view)
            }
            VIEW_TYPE_LISTING -> {
                view = LayoutInflater.from(mContext).inflate(R.layout.item_listing, parent, false)
                return RedditPostRecyclerAdapter.ListingHolder(view, false)
            }
            else -> {
                view = LayoutInflater.from(mContext).inflate(R.layout.item_user_comment, parent, false)
                return CommentViewholder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        if (holder!! is CommentViewholder) {
            val cHolder = holder as CommentViewholder
            cHolder.bindHolder(listOfThings[position] as UserHistoryComment)
        }
        else if (holder is RedditPostRecyclerAdapter.ListingHolder) {
            holder.bindUserHistoryListing(listOfThings[position] as UserHistoryListing, false)
        }
    }

    override fun onViewAttachedToWindow(holder: RecyclerView.ViewHolder?) {
        super.onViewAttachedToWindow(holder)
        if (holder!!.adapterPosition == listOfThings.size - 1) {
            mScrollListener.onReachedLast()
        }
    }

    class CommentViewholder(var view: View): RecyclerView.ViewHolder(view){
        val mContext = view.context
        public val tvLinkTitle = view.findViewById(R.id.tvLinkTitle) as TextView
        public val tvCommentScore = view.findViewById(R.id.tvCommentScore) as TextView
        public val tvCommentAge = view.findViewById(R.id.tvCommentAge) as TextView
        public val tvLinkSubreddit = view.findViewById(R.id.tvLinkSubreddit) as TextView
        public val tvCommentBody = view.findViewById(R.id.tvCommentBody) as TextView

        fun bindHolder(comment: UserHistoryComment) {
            tvLinkTitle.text = comment.link_title
            tvCommentScore.text = mContext.getString(R.string.format_points, comment.score)
            tvCommentAge.text = GeneralUtils.returnFormattedTime(view.context, Calendar.getInstance(TimeZone.getTimeZone("UTC")).timeInMillis, comment.created_utc)
            tvLinkSubreddit.text = comment.subreddit_name_prefixed
            tvCommentBody.text = GeneralUtils.returnFormattedStringFromHtml(comment.bodyHtml)
            itemView.setOnClickListener (object : View.OnClickListener{
                override fun onClick(v: View?) {
                    CommentsActivity.startCommentActivityForThread(mContext, comment.link_id, comment.commentId, 1)
                }
            })
        }
    }
}