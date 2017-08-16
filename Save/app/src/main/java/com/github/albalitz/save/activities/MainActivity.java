package com.github.albalitz.save.activities;

import android.app.DialogFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
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
import android.widget.ListView;

import com.github.albalitz.save.R;
import com.github.albalitz.save.SaveApplication;
import com.github.albalitz.save.fragments.LinkActionsDialogFragment;
import com.github.albalitz.save.fragments.SaveLinkDialogFragment;
import com.github.albalitz.save.persistence.Link;
import com.github.albalitz.save.persistence.SavePersistenceOption;
import com.github.albalitz.save.persistence.Storage;
import com.github.albalitz.save.persistence.api.Api;
import com.github.albalitz.save.persistence.api.OfflineQueue;
import com.github.albalitz.save.persistence.export.SavedLinksExporter;
import com.github.albalitz.save.persistence.export.ViewExportedFileListener;
import com.github.albalitz.save.utils.ActivityUtils;
import com.github.albalitz.save.utils.LinkAdapter;
import com.github.albalitz.save.utils.Utils;

import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements ApiActivity, LinkActionsDialogFragment.LinkActionListener, SnackbarActivity, SwipeRefreshLayout.OnRefreshListener {

    private Context context;

    private SwipeRefreshLayout swipeRefreshLayout;

    private ListView listViewSavedLinks;
    private LinkAdapter adapter;
    private ArrayList<Link> savedLinks;
    private Link selectedLink;

    private SavePersistenceOption storage;

    private SharedPreferences prefs = SaveApplication.getSharedPreferences();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        this.context = this;

        // assign content
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        listViewSavedLinks = (ListView) findViewById(R.id.listViewSavedLinks);

        // prepare stuff
        storage = Storage.getStorageSettingChoice(this);
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

        registerReceiver(new ConnectionChangeReceiver(), new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));

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

        try {
            if (!OfflineQueue.getLinks().isEmpty()) {
                saveQueuedLinks();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        storage.updateSavedLinks();
    }

    @Override
    public void onResume() {
        super.onResume();
        storage = Storage.getStorageSettingChoice(this);
        storage.updateSavedLinks();
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
            case R.id.action_export:
                showExportConfirmation(SavedLinksExporter.export(this, savedLinks));
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
        }
    }

    private void showExportConfirmation(boolean successfullyExported) {
        if (successfullyExported) {
            Snackbar.make(listViewSavedLinks, "Exported " + savedLinks.size() + " links.", Snackbar.LENGTH_LONG)
                .setAction("View", new ViewExportedFileListener()).show();
        }
    }


    @Override
    public void onSavedLinksUpdate(ArrayList<Link> savedLinks) {
        this.savedLinks = savedLinks;
        adapter = new LinkAdapter(this, savedLinks);
        this.listViewSavedLinks.setAdapter(adapter);
        this.swipeRefreshLayout.setRefreshing(false);
    }

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

    private void saveQueuedLinks() {
        if (!(this.storage instanceof Api)) {
            Log.w(this.toString(), "Not using API as persistence backend. Not trying to save queued links now!");
            return;
        }

        List<Link> queuedLinks = new ArrayList<>();
        try {
            queuedLinks = OfflineQueue.getLinks();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (!queuedLinks.isEmpty()) {
            Log.d(this.toString(), "Trying to save " + queuedLinks.size() + " queued links...");
        }

        for (Link link : queuedLinks) {
            try {
                storage.saveLink(link);
            } catch (JSONException | UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        if (!queuedLinks.isEmpty()) {
            Utils.showToast(this, "Saved queued links.");
            Log.d(this.toString(), "Saved queued links.");
        }
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


    public class ConnectionChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(this.toString(), "Connection changed!");
            if (Utils.networkAvailable(context)) {
                saveQueuedLinks();
            } else {
                Log.i(this.toString(), "Not connected.");
            }
        }
    }
}
