package com.avinashdavid.readitforreddit.UI;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;

import com.avinashdavid.readitforreddit.MiscUtils.GPSUtils;
import com.avinashdavid.readitforreddit.R;

/**
 * Created by avinashdavid on 3/20/17.
 * Dialog fragment that allows users to add a typed-in subreddit to their subscriptions.
 * Currently not in use in the app.
 */

public class AddSubredditDialogFragment extends DialogFragment {
    public interface AddSubDialogListener{
        void OnAddDialogPositiveClick(DialogFragment dialogFragment, String query);
    }

    AddSubDialogListener mAddSubDialogListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mAddSubDialogListener = (AddSubDialogListener)context;
        } catch (ClassCastException e){
            throw new ClassCastException(context.toString()
                    + " must implement AddSubDialogListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Activity activity = getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();

        View v= inflater.inflate(R.layout.dialog_add_subreddit, null);
        final TextInputEditText textInputEditText = (TextInputEditText) v.findViewById(R.id.add_subreddit_edittext);

        builder.setTitle(getString(R.string.add_subscription))
                .setView(v)
                .setPositiveButton(getString(R.string.add), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String toAdd = textInputEditText.getText().toString();
                        toAdd = toAdd.replaceAll(" ", "");
                        mAddSubDialogListener.OnAddDialogPositiveClick(AddSubredditDialogFragment.this, toAdd);
                    }
                }).setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                getDialog().cancel();
            }
        });
        GPSUtils.setScreenName(activity, AddSubredditDialogFragment.class.getSimpleName());
        return builder.create();
    }
}
