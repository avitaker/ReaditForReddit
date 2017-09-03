package com.avinashdavid.readitforreddit.User

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.orm.SugarRecord

/**
 * Created by avinashdavid on 8/30/17.
 */
class UserHistoryListing() : SugarRecord() {
    var domain = ""
    var title = ""
    var score: Int = 0
    var num_comments: Int = 0
    var author = ""
    var subreddit_name_prefixed =""
    var created_utc: Long = 0
    var selftext_html = ""
    var url = ""
    var thumbnail = ""
    var over_18 = false
    @Transient var postId = ""
}