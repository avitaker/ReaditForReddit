package com.avinashdavid.readitforreddit.UI;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.avinashdavid.readitforreddit.MainActivity;
import com.avinashdavid.readitforreddit.MiscUtils.GPSUtils;
import com.avinashdavid.readitforreddit.R;

/**
 * Created by avinashdavid on 3/12/17.
 * Dialog fragment that takes user input and allows search
 */

public class SearchDialogFragment extends DialogFragment {
    public interface SearchDialogListener{
        void OnSearchDialogPositiveClick(DialogFragment dialogFragment, String query);
        void OnSearchDialogNegativeClick(DialogFragment dialogFragment);
    }

    SearchDialogListener mSearchDialogListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mSearchDialogListener = (SearchDialogListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(context.toString()
                    + " must implement SearchDialogListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Activity activity  = getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LayoutInflater inflater = activity.getLayoutInflater();

        View v= inflater.inflate(R.layout.dialog_search_posts, null);

        final TextInputEditText textInputEditText = (TextInputEditText) v.findViewById(R.id.search_posts_edittext);
        TextView limitlabel = (TextView)v.findViewById(R.id.limit_search_label);
        CheckBox limitCheckbox = (CheckBox)v.findViewById(R.id.limit_search_checkbox);
        String currentSub = PreferenceManager.getDefaultSharedPreferences(activity).getString(activity.getString(R.string.pref_current_subreddit), null);
        if (currentSub==null){
            limitCheckbox.setVisibility(View.GONE);
            limitlabel.setVisibility(View.GONE);
        } else {
            limitCheckbox.setChecked(((MainActivity)activity).mRestrictSearchBoolean);
            limitlabel.setText(activity.getString(R.string.format_limit_to, currentSub));
        }

        builder.setTitle(R.string.search_posts)
                .setView(v)
                .setPositiveButton(R.string.search, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String searchFOr = textInputEditText.getText().toString();
                        mSearchDialogListener.OnSearchDialogPositiveClick(SearchDialogFragment.this, searchFOr);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mSearchDialogListener.OnSearchDialogNegativeClick(SearchDialogFragment.this);
                    }
                });
        GPSUtils.setScreenName(activity, SearchDialogFragment.class.getSimpleName());
        return builder.create();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        MainActivity activity = (MainActivity)getActivity();
        int toCheck = PreferenceManager.getDefaultSharedPreferences(activity).getInt(getString(R.string.pref_last_valid_nav_item),0);
        activity.setCheckedNavigationItem(toCheck);
    }
}
