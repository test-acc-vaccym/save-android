package com.github.albalitz.save.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.EditText;

import com.github.albalitz.save.R;
import com.github.albalitz.save.activities.MainActivity;
import com.github.albalitz.save.activities.SnackbarActivity;
import com.github.albalitz.save.persistence.Link;
import com.github.albalitz.save.persistence.SavePersistenceOption;
import com.github.albalitz.save.utils.Utils;

import org.json.JSONException;

import java.io.UnsupportedEncodingException;

/**
 * Created by albalitz on 3/24/17.
 */
public class SaveLinkDialogFragment extends DialogFragment {

    private Activity listener;

    private String url;
    // todo: support editing already saved links

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        listener = activity;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.dialog_save_link_title)
                .setView(R.layout.dialog_save_link);

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                String url = ((EditText) getDialog().findViewById(R.id.save_dialog_link_url)).getText().toString();
                String annotation = ((EditText) getDialog().findViewById(R.id.save_dialog_link_annotation)).getText().toString();
                if (url.isEmpty() || url.equals("http://") || url.equals("https://")) {
                    Utils.showToast(listener, "Please enter a URL.");
                    // todo: disable this button while no url is entered!
                    return;
                }
                Link link = new Link(url, annotation);

                Utils.showSnackbar((SnackbarActivity) listener, "Saving link...");
                SavePersistenceOption storage = ((MainActivity) listener).getStorage();
                try {
                    storage.saveLink(link);
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });
        AlertDialog dialog = builder.create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Bundle args = getArguments();
                if (args == null) {
                    // this happens, when opening this dialog without arguments,
                    // i.e. when pressing the FAB to save a link without having previous data.
                    return;
                }

                url = args.getString("url", null);
                if (!url.isEmpty()) {
                    EditText urlEditText = (EditText) getDialog().findViewById(R.id.save_dialog_link_url);
                    urlEditText.setText(url);
                }
            }
        });

        return dialog;
    }
}
