package com.avinashdavid.readitforreddit.MiscUtils

import android.content.Context
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView

/**
 * Created by avinashdavid on 8/30/17.
 */
fun RecyclerView.prepareLinear(context: Context, showDividers: Boolean = false) {
    layoutManager = LinearLayoutManager(context)
    if (showDividers) {
        val lm = layoutManager as LinearLayoutManager
        addItemDecoration(DividerItemDecoration(context, lm.orientation))
    }
}