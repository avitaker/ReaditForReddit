package com.avinashdavid.readitforreddit.SubredditUtils

import io.realm.RealmObject
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass

/**
 * Created by avinashdavid on 3/8/17.
 * This class extends Realm object, and contains all the relevant information about a subreddit
 */

@RealmClass
open class SubredditObject : RealmObject() {
    @PrimaryKey
    open var subredditName: String = ""

    open var timestamp: Long = 0

    open var publicDescription: String? = null

    open var isSafeForKids: Boolean = false

    open var subredditType: String? = null

    open var title: String? = null

    open var headerImgUrl: String? = null

    open var sidebarTextHtml: String? = null
}
