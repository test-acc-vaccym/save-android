package com.github.albalitz.save.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.TextView;

import com.github.albalitz.save.BuildConfig;
import com.github.albalitz.save.R;

public class AboutDialogFragment extends DialogFragment {
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.about)
            .setView(R.layout.dialog_about);

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

            }
        });
        AlertDialog dialog = builder.create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                String appName = getString(R.string.app_name);
                String versionName = BuildConfig.VERSION_NAME;
                TextView textViewAppInfo = (TextView) getDialog().findViewById(R.id.textViewAppInfo);
                textViewAppInfo.setText(appName + " v" + versionName);
            }
        });

        return dialog;
    }
}
