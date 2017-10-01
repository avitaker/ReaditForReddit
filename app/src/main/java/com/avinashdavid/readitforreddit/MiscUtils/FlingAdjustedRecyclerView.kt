package com.avinashdavid.readitforreddit.MiscUtils

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.View

/**
 * Created by avinashdavid on 9/24/17.a
 */
class FlingAdjustedRecyclerView : RecyclerView{
    private val flingFactor = 0.5

    override fun fling(velocityX: Int, velocityY: Int): Boolean {
        val newVelocityY = (flingFactor * velocityY).toInt()
        return super.fling(velocityX, newVelocityY)
    }

    constructor(context: Context, attributeSet: AttributeSet?, defStyle: Int): super(context, attributeSet, defStyle)
    constructor(context: Context, attributeSet: AttributeSet?) : super(context, attributeSet)
    constructor(context:Context) : super(context)
}