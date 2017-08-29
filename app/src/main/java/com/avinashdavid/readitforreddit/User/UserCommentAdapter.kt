package com.avinashdavid.readitforreddit.User

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.avinashdavid.readitforreddit.MiscUtils.GeneralUtils
import com.avinashdavid.readitforreddit.R
import java.util.*

/**
 * Created by avinashdavid on 8/24/17.
 */
class UserCommentAdapter(context: Context, comments: List<UserHistoryComment>?): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    public var userHistoryComments = comments
    var parentContext = context

    override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): RecyclerView.ViewHolder {
        val v: View = LayoutInflater.from(parentContext).inflate(R.layout.item_user_comment, parent, false)
        return CommentViewholder(v)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder?, position: Int) {
        val cvh: CommentViewholder = holder as CommentViewholder
        if (position<userHistoryComments!!.size)cvh.bindHolder(userHistoryComments!![position])
    }

    override fun getItemCount(): Int {
        if (userHistoryComments==null) return 0
        return userHistoryComments!!.size
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
        }
    }
}