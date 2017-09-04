package com.avinashdavid.readitforreddit.MiscUtils

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.orm.SugarRecord

/**
 * Created by avinashdavid on 8/30/17.
 */
interface ScrollListener {
    fun onReachedLast()
}

fun changeToolbarColor(activity: AppCompatActivity, listOfIds: List<Int>) {
    val listOfViews = arrayListOf<View>()
    listOfIds.forEach { id -> listOfViews.add(activity.findViewById(id)!!) }
    PreferenceUtils.changeToolbarColor(activity, listOfViews)
}