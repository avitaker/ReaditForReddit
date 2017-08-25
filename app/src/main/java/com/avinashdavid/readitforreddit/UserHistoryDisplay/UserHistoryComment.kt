package com.avinashdavid.readitforreddit.UserHistoryDisplay

import com.google.gson.annotations.SerializedName
import com.orm.SugarRecord

/**
 * Created by avinashdavid on 8/21/17.
 */
class UserHistoryComment() : SugarRecord() {
    @SerializedName("body_html")
    var bodyHtml: String = ""
    var score : Int = 0
    var created_utc: Float = 0f
    var link_title: String = ""
    var link_url:  String = ""
    var link_author: String = ""
    var subreddit_name_prefixed: String = ""
    var link_id: String = ""
    var over_18: Boolean = false
    var num_comments: Int = 0

    constructor(bodyHtml : String, score: Int, timestamp: Float, linkId:String, linkTitle : String, linkUrl:String, linkAuthor:String, linkSubredditName:String, linkNumComments:Int, linkNSFW : Boolean) : this() {
        this.bodyHtml = bodyHtml
        this.score = score
        this.created_utc = timestamp
        this.link_title = linkTitle
        this.link_url = linkUrl
        this.link_author =linkAuthor
        this.subreddit_name_prefixed = linkSubredditName
        this.link_id = linkId
        this.over_18 = linkNSFW
        this.num_comments = linkNumComments
    }
}
