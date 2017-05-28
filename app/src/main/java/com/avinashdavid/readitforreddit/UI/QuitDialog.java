package com.avinashdavid.readitforreddit.UI;

import android.app.AlertDialog;
import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.avinashdavid.readitforreddit.R;

/**
 * Created by avinashdavid on 5/28/17.
 */

public class QuitDialog extends DialogFragment {
    AppCompatActivity activity;
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        activity = (AppCompatActivity)getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(getString(R.string.quit_dialog_title)).setMessage(getString(R.string.quit_dialog_message));
        builder.setPositiveButton(getString(R.string.yes).toUpperCase(), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                activity.finish();
            }
        });
        builder.setNegativeButton(getString(R.string.no).toUpperCase(), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dismiss();
            }
        });
        return builder.create();
    }
}
