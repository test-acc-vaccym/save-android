package com.github.albalitz.save.activities;

import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.github.albalitz.save.R;
import com.github.albalitz.save.SaveApplication;
import com.github.albalitz.save.fragments.LinkActionsDialogFragment;
import com.github.albalitz.save.fragments.SaveLinkDialogFragment;
import com.github.albalitz.save.persistence.Link;
import com.github.albalitz.save.persistence.SavePersistenceOption;
import com.github.albalitz.save.persistence.Storage;
import com.github.albalitz.save.persistence.importexport.SavedLinksExporter;
import com.github.albalitz.save.persistence.importexport.SavedLinksImporter;
import com.github.albalitz.save.persistence.importexport.ViewExportedFileListener;
import com.github.albalitz.save.persistence.offline_queue.OfflineQueue;
import com.github.albalitz.save.utils.ActivityUtils;
import com.github.albalitz.save.utils.LinkAdapter;
import com.github.albalitz.save.utils.Utils;
import com.github.albalitz.save.utils.temporary_sharedpreference.TemporaryPreference;
import com.github.albalitz.save.utils.temporary_sharedpreference.TemporarySharedPreferenceHandler;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;

import static com.github.albalitz.save.SaveApplication.setAppContext;

public class MainActivity extends AppCompatActivity
        implements ApiActivity, LinkActionsDialogFragment.LinkActionListener, SnackbarActivity, SwipeRefreshLayout.OnRefreshListener {

    private Context context;

    private SwipeRefreshLayout swipeRefreshLayout;

    private ListView listViewSavedLinks;
    private LinkAdapter adapter;
    private ArrayList<Link> savedLinks;
    private Link selectedLink;

    private SavePersistenceOption storage;
    private OfflineQueue offlineQueue;
    private SharedPreferences prefs = SaveApplication.getSharedPreferences();

    private Button saveQueuedLinksButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        this.context = this;
        setAppContext(this.context);

        // assign content
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        listViewSavedLinks = (ListView) findViewById(R.id.listViewSavedLinks);
        saveQueuedLinksButton = (Button) findViewById(R.id.saveQueuedLinksButton);
        saveQueuedLinksButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                offlineQueue.saveQueuedLinks();
            }
        });

        // prepare stuff
        prepareListViewListeners();
        swipeRefreshLayout.setOnRefreshListener(this);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SaveLinkDialogFragment saveLinkDialogFragment = new SaveLinkDialogFragment();
                saveLinkDialogFragment.show(getFragmentManager(), "save");
            }
        });

        // do actual stuff
        /*
         * Check for configuration and let the user know what to do accordingly:
         * - ask to register
         * - ask to configure api url
         */
        if (prefs.getBoolean("pref_key_use_api_or_local", false)) {
            if (prefs.getString("pref_key_api_url", "").isEmpty()) {
                Utils.showToast(context, "Please configure the API's URL.");
                ActivityUtils.openSettings(this);
            } else {
                if (prefs.getString("pref_key_api_username", "").isEmpty()
                        || prefs.getString("pref_key_api_password", "").isEmpty()) {
                    Log.w(this.toString(), "No credentials found. Opening registration.");
                    Utils.showToast(context, "No credentials found for API. Please register.");
                    Intent intent = new Intent(this, RegisterActivity.class);
                    startActivity(intent);
                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        storage = Storage.getStorageSettingChoice(this);
        offlineQueue = new OfflineQueue(this);
        try {
            storage.updateSavedLinks();
        } catch (IllegalArgumentException e) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }
        setOfflineQueueButtonVisibility();
        setMessageTextView();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_settings:
                ActivityUtils.openSettings(this);
                return true;
            case R.id.action_about:
                ActivityUtils.showAboutDialog(this);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case SaveApplication.PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showExportConfirmation(SavedLinksExporter.export(this, savedLinks));
                } else {
                    Utils.showToast(context, "Please grant external storage permission to export.");
                }
                return;
            }
            case SaveApplication.PERMISSION_REQUEST_READ_EXTERNAL_STORAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    importLinks();
                } else {
                    Utils.showToast(context, "Please grant external storage permission to import.");
                }
                return;
            }
        }
    }

    private void showExportConfirmation(boolean successfullyExported) {
        if (successfullyExported) {
            Snackbar.make(listViewSavedLinks, "Exported " + savedLinks.size() + " links.", Snackbar.LENGTH_LONG)
                .setAction("View", new ViewExportedFileListener()).show();
        }
    }

    private void showImportConfirmation(ArrayList importedLinks) {
        if (importedLinks != null && importedLinks.size() > 0) {
            Snackbar.make(listViewSavedLinks, "Imported " + importedLinks.size() + " links.", Snackbar.LENGTH_LONG).show();
        }
    }

    private void setOfflineQueueButtonVisibility() {
        Log.d(this.toString(), "Updating offline queue button...");
        int queueSize = offlineQueue.queuedCount();
        if (Utils.storageSettingChoiceIsAPI() && queueSize > 0 && Utils.networkAvailable(this)) {
            saveQueuedLinksButton.setVisibility(View.VISIBLE);
            String linkPluralized = queueSize == 1 ? "link" : "links";
            String buttonText = "Save " + offlineQueue.queuedCount() + " queued " + linkPluralized;
            saveQueuedLinksButton.setText(buttonText);
        } else {
            saveQueuedLinksButton.setVisibility(View.GONE);
        }
    }

    private void setMessageTextView() {
        Log.d(this.toString(), "Updating message text view...");
        TextView messageTextView = (TextView) findViewById(R.id.messageTextView);
        if (Utils.storageSettingChoiceIsAPI() && !Utils.networkAvailable(this)) {
            messageTextView.setText(getString(R.string.no_connection_message));
        } else if (savedLinks != null && savedLinks.isEmpty()) {
            messageTextView.setText(getString(R.string.no_link_message));
        } else {
            messageTextView.setText("");
            messageTextView.setVisibility(View.GONE);
            return;
        }

        messageTextView.setVisibility(View.VISIBLE);
    }

    private void handleTempPrefs() {
        if (TemporarySharedPreferenceHandler.popTemporarySharedPreferenceValue(TemporaryPreference.EXPORT)) {
            boolean exportResult = SavedLinksExporter.export(this, savedLinks);
            showExportConfirmation(exportResult);
        }

        if (TemporarySharedPreferenceHandler.popTemporarySharedPreferenceValue(TemporaryPreference.IMPORT)) {
            importLinks();
        }

        if (TemporarySharedPreferenceHandler.popTemporarySharedPreferenceValue(TemporaryPreference.DELETE_ALL)) {
            Utils.showToast(this, "Deleting all saved links...");
            // todo: do this in the storage option with one call
            for (Link link : savedLinks) {
                storage.deleteLink(link);
            }
        }
    }

    private void importLinks() {
        try {
            ArrayList<Link> importResult = SavedLinksImporter.importLinks(this.context, this.storage);
            showImportConfirmation(importResult);
        } catch (JSONException e) {
            e.printStackTrace();
            Utils.showToast(this.context, "Can't import links. Please check your exported file for correct JSON syntax.");
        } catch (IOException e) {
            e.printStackTrace();
            Utils.showToast(this.context, "Error while reading file. Please check the app's permissions and if the file exists.");
        }
    }



    @Override
    public void onSavedLinksUpdate(ArrayList<Link> savedLinks) {
        this.savedLinks = savedLinks;
        adapter = new LinkAdapter(this, savedLinks);
        this.listViewSavedLinks.setAdapter(adapter);
        this.swipeRefreshLayout.setRefreshing(false);
        setOfflineQueueButtonVisibility();
        setMessageTextView();
        handleTempPrefs();
    }

    @Override
    public void onSaveDialogDone() {}

    @Override
    public void onRegistrationError(String errorMessage) {}

    @Override
    public void onRegistrationSuccess() {}


    private void prepareListViewListeners() {
        listViewSavedLinks.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Link clickedLink = savedLinks.get(position);
                Utils.showSnackbar(MainActivity.this, "Opening link...");
                Utils.openInExternalBrowser(context, clickedLink.url());
            }
        });

        listViewSavedLinks.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                selectedLink = savedLinks.get(position);
                LinkActionsDialogFragment linkActionsDialogFragment = new LinkActionsDialogFragment();
                linkActionsDialogFragment.show(getFragmentManager(), "actions");
                return true;
            }
        });
    }

    public SavePersistenceOption getStorage() {
        return this.storage;
    }

    /*
     * Implement link dialog actions
     */
    @Override
    public void onSelectLinkOpen(DialogFragment dialog) {
        if (selectedLink == null) {
            return;
        }

        Utils.openInExternalBrowser(context, selectedLink.url());
    }

    @Override
    public void onSelectLinkShare(DialogFragment dialog) {
        if (selectedLink == null) {
            return;
        }

        Intent shareLinkIntent = new Intent();
        shareLinkIntent.setAction(Intent.ACTION_SEND);
        shareLinkIntent.putExtra(Intent.EXTRA_TEXT, selectedLink.url());
        shareLinkIntent.setType("text/plain");
        startActivity(Intent.createChooser(shareLinkIntent, getResources().getText(R.string.share_destination_chooser)));
    }

    @Override
    public void onSelectLinkDelete(DialogFragment dialog) {
        if (selectedLink == null) {
            return;
        }

        storage.deleteLink(selectedLink);
    }

    @Override
    public void onDialogDismiss(DialogFragment dialog) {
        selectedLink = null;
    }

    /*
     * Implement SnackbarActivity
     */
    @Override
    public View viewFromActivity() {
        return findViewById(R.id.listViewSavedLinks);
    }

    /*
     * SwipeRefreshLayout.OnRefreshListener methods
     */
    @Override
    public void onRefresh() {
        storage.updateSavedLinks();
    }

}
