package com.avinashdavid.readitforreddit.UI;

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

import com.avinashdavid.readitforreddit.R;

/**
 * Created by avinashdavid on 3/12/17.
 */

public class GoToDialogFragment extends DialogFragment {
    public interface GoToDialogListener{
        void OnGoToDialogPositiveClick(DialogFragment dialogFragment, String query);
        void OnGoToDialogNegativeClick(DialogFragment dialogFragment);
    }

    GoToDialogListener mGoToDialogListener;
    String editTextInput;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mGoToDialogListener = (GoToDialogListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(context.toString()
                    + " must implement NoticeDialogListener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View v= inflater.inflate(R.layout.dialog_go_to_subreddit, null);

        final TextInputEditText textInputEditText = (TextInputEditText) v.findViewById(R.id.go_to_edittext);

        builder.setTitle(R.string.go_to)
                .setView(v)
                .setPositiveButton(R.string.go, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        editTextInput = textInputEditText.getText().toString();
                        editTextInput = editTextInput.replaceAll(" ", "");
                        mGoToDialogListener.OnGoToDialogPositiveClick(GoToDialogFragment.this, editTextInput);
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mGoToDialogListener.OnGoToDialogNegativeClick(GoToDialogFragment.this);
                    }
                });
        return builder.create();
    }
}
