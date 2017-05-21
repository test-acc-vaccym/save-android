package com.github.albalitz.save.persistence.database;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.github.albalitz.save.SaveApplication;
import com.github.albalitz.save.activities.ApiActivity;
import com.github.albalitz.save.activities.SnackbarActivity;
import com.github.albalitz.save.persistence.Link;
import com.github.albalitz.save.persistence.SavePersistenceOption;
import com.github.albalitz.save.utils.Utils;

import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

/**
 * Created by albalitz on 4/6/17.
 */
public class Database implements SavePersistenceOption {

    private ApiActivity callingActivity;
    private SaveDbHelper dbHelper;
    private SharedPreferences prefs;

    public Database(ApiActivity callingActivity) {
        this.callingActivity = callingActivity;
        this.dbHelper = new SaveDbHelper((Context) callingActivity);
        this.prefs = SaveApplication.getSharedPreferences();
    }

    // todo: edit a link

    @Override
    public void updateSavedLinks() {
        Log.d(this.toString(), "Updating saved links list from local database...");
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] projection = {
                SaveDbContract.LinkEntry._ID,
                SaveDbContract.LinkEntry.COLUMN_NAME_URL,
                SaveDbContract.LinkEntry.COLUM_NAME_ANNOTATION
        };

        String sortDirection = "";
        if (prefs.getBoolean("pref_key_sort_ascending", true)) {
            sortDirection = "ASC";
        } else {
            sortDirection = "DESC";
        }
        String sortOrder = SaveDbContract.LinkEntry._ID + " " + sortDirection;

        Cursor cursor = db.query(
                SaveDbContract.LinkEntry.TABLE_NAME,
                projection,
                null,  // all rows
                null,  // no selectionArgs
                null,  // don't group
                null,  // don't filter
                sortOrder
        );

        ArrayList<Link> savedLinks = new ArrayList<>();
        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(SaveDbContract.LinkEntry._ID));
            String url = cursor.getString(cursor.getColumnIndexOrThrow(SaveDbContract.LinkEntry.COLUMN_NAME_URL));
            String annotation = cursor.getString(cursor.getColumnIndexOrThrow(SaveDbContract.LinkEntry.COLUM_NAME_ANNOTATION));

            Link link = new Link(id, url, annotation);
            savedLinks.add(link);
        }
        cursor.close();

        Log.d(this.toString(), "Found " + savedLinks.size() + " saved links.");
        callingActivity.onSavedLinksUpdate(savedLinks);
    }

    @Override
    public void saveLink(Link link) throws JSONException, UnsupportedEncodingException {
        Log.d(this.toString(), "Saving link: " + link.toString() + " ...");
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(SaveDbContract.LinkEntry.COLUMN_NAME_URL, link.url());
        values.put(SaveDbContract.LinkEntry.COLUM_NAME_ANNOTATION, link.annotation());

        long newRowId = db.insert(SaveDbContract.LinkEntry.TABLE_NAME, null, values);
        try {
            Utils.showSnackbar((SnackbarActivity) callingActivity, "Link saved.");
        } catch (ClassCastException e) {
            // this happens, when sharing to the app and using this from the SaveLinkActivity.
            // using a toast instead, as they can be displayed on top of another app
            Utils.showToast((Context) callingActivity, "Link saved.");
        }
        // also update the list view
        updateSavedLinks();
    }

    @Override
    public void deleteLink(Link link) {
        Log.d(this.toString(), "Deleting link: " + link.toString() + "...");
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String selection = SaveDbContract.LinkEntry._ID + " = ?";
        String[] selectionArgs = {Integer.toString(link.id())};

        db.delete(SaveDbContract.LinkEntry.TABLE_NAME, selection, selectionArgs);

        Utils.showSnackbar((SnackbarActivity) callingActivity, "Deleted link.");
        // also update the list view
        updateSavedLinks();
    }

    @Override
    public void registerUser(String username, String password) throws JSONException, UnsupportedEncodingException {
        Utils.showToast((Context) callingActivity, "Local storage doesn't require registration.");
    }
}
