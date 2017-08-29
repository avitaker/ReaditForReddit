package com.avinashdavid.readitforreddit.User

import com.google.gson.annotations.SerializedName
import com.orm.SugarRecord

/**
 * Created by avinashdavid on 8/28/17.
 */
class UserAbout() : SugarRecord() {
    var name : String = ""
    var created_utc : Long = 0
    var comment_karma: Int = 0
    var link_karma : Int = 0
    var is_gold = false
    var is_mod = false
}