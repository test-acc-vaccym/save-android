package com.github.albalitz.save.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.github.albalitz.save.fragments.SaveLinkDialogFragment;
import com.github.albalitz.save.persistence.Link;
import com.github.albalitz.save.utils.Utils;

import java.util.ArrayList;

/**
 * Created by albalitz on 5/21/17.
 */

public class SaveLinkActivity extends Activity implements ApiActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Handle stuff being shared to this app from another app
        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        if (action.equals(Intent.ACTION_SEND) && type != null) {
            if (type.equals("text/plain")) {
                handleSendIntent(intent);
            }
        }
    }

    private void handleSendIntent(Intent intent) {
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (sharedText == null) {
            Utils.showToast(getApplicationContext(), "No data found.");
            finish();
        }

        Log.d(this.toString(), "Got shared text: " + sharedText);
        SaveLinkDialogFragment saveLinkDialogFragment = new SaveLinkDialogFragment();
        Bundle args = new Bundle();
        args.putString("url", sharedText);
        saveLinkDialogFragment.setArguments(args);
        saveLinkDialogFragment.show(getFragmentManager(), "save");
    }

    @Override
    public void onSavedLinksUpdate(ArrayList<Link> savedLinks) {
        this.finish();
    }

    @Override
    public void onSaveDialogDone() {
        this.finish();
    }

    @Override
    public void onRegistrationError(String errorMessage) {
    }

    @Override
    public void onRegistrationSuccess() {
    }
}
