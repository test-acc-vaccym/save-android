package com.github.albalitz.save.activities;

import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
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
import com.github.albalitz.save.persistence.SavePersistenceOption;
import com.github.albalitz.save.persistence.api.Api;
import com.github.albalitz.save.persistence.Link;
import com.github.albalitz.save.fragments.LinkActionsDialogFragment;
import com.github.albalitz.save.fragments.SaveLinkDialogFragment;
import com.github.albalitz.save.persistence.database.Database;
import com.github.albalitz.save.persistence.export.SavedLinksExporter;
import com.github.albalitz.save.utils.ActivityUtils;
import com.github.albalitz.save.utils.LinkAdapter;
import com.github.albalitz.save.utils.Utils;

import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

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
        if (prefs.getBoolean("pref_key_use_api_or_local", false)) {
            storage = new Api(this);
        } else {
            storage = new Database(this);
        }
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

        storage.updateSavedLinks();

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
            Utils.showToast(context, "No data found.");
            finish();
        }

        Log.d(this.toString(), "Got shared text: " + sharedText);
        Link link = new Link(sharedText, "");  // todo: allow user to edit before saving

        Utils.showSnackbar(this, "Saving link...");
        try {
            storage.saveLink(link);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
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
                SavedLinksExporter.export(savedLinks);
                return true;
        }

        return super.onOptionsItemSelected(item);
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
