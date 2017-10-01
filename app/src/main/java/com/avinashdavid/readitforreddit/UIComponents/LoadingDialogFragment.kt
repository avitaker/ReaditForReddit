package com.avinashdavid.readitforreddit.UIComponents

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.app.FragmentManager
import android.view.LayoutInflater
import com.avinashdavid.readitforreddit.R
import kotlinx.android.synthetic.main.dialog_loading.view.*

/**
 * Created by avinashdavid on 10/1/17.
 */
class LoadingDialogFragment :  DialogFragment() {
    companion object {
        private const val KEY_TITLE = "KEY_TITLE"
        private const val KEY_MESSAGE = "KEY_MESSAGE"

        fun showLoadingDialog(title: String, message: String): LoadingDialogFragment {
            val args = Bundle()
            args.putString(KEY_TITLE, title)
            args.putString(KEY_MESSAGE, message)

            val fragment = LoadingDialogFragment()
            fragment.arguments = args
            return fragment
        }
    }

    var dialogTitle = getString(R.string.loading_dialog_defaultTitle)
    var dialogMessage = getString(R.string.loading_dialog_defaultMessage)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        dialogTitle = arguments.getString(KEY_TITLE)
        dialogMessage = arguments.getString(KEY_MESSAGE)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(activity)

        val view = LayoutInflater.from(activity).inflate(R.layout.dialog_loading, null)

        view.tvLoadingDialogTitle.text = dialogTitle
        view.tvLoadingDialogMessage.text = dialogMessage

        dialog.setContentView(view)
        return dialog
    }
}