package com.github.albalitz.save.persistence.offline_queue;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.github.albalitz.save.R;
import com.github.albalitz.save.SaveApplication;
import com.github.albalitz.save.activities.MainActivity;
import com.github.albalitz.save.activities.SnackbarActivity;
import com.github.albalitz.save.persistence.Link;
import com.github.albalitz.save.persistence.SavePersistenceOption;
import com.github.albalitz.save.utils.Utils;

import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by albalitz on 8/15/17.
 */
public class OfflineQueue {

    private Context callingActivity;
    private QueueDbHelper dbHelper;

    public OfflineQueue(Context callingActivity) {
        this.callingActivity = callingActivity;
        this.dbHelper = new QueueDbHelper(callingActivity);
    }

    public void addLink(Link link) {
        Log.d("OfflineQueue", "Queueing link: " + link.toString() + " ...");
        if (isLinkQueued(link)) {
            Log.d(this.toString(), "Link already queued! Not queueing again.");
            return;
        }

        SQLiteDatabase db = this.dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(QueueDbContract.LinkEntry.COLUMN_NAME_URL, link.url());
        values.put(QueueDbContract.LinkEntry.COLUM_NAME_ANNOTATION, link.annotation());

        long newRowId = db.insert(QueueDbContract.LinkEntry.TABLE_NAME, null, values);
        try {
            Utils.showSnackbar((SnackbarActivity) this.callingActivity, callingActivity.getString(R.string.link_queued_message));
        } catch (ClassCastException e) {
            // this happens, when sharing to the app and using this from the SaveLinkActivity.
            // using a toast instead, as they can be displayed on top of another app
            Utils.showToast(this.callingActivity, callingActivity.getString(R.string.link_queued_message));
        }
    }

    public void dropLink(Link link) {
        Log.d(this.toString(), "Dropping link from queue: " + link.toString() + "...");
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        String selection = QueueDbContract.LinkEntry.COLUMN_NAME_URL + " = ? AND " + QueueDbContract.LinkEntry.COLUM_NAME_ANNOTATION + " = ?";
        String[] selectionArgs = {
            link.url(),
            link.annotation()
        };

        db.delete(QueueDbContract.LinkEntry.TABLE_NAME, selection, selectionArgs);
        Log.d(this.toString(), "Dropped link from queue.");
    }

    public ArrayList<Link> getLinks() {
        Log.d(this.toString(), "Getting queued links ...");
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        String[] projection = {
            QueueDbContract.LinkEntry._ID,
            QueueDbContract.LinkEntry.COLUMN_NAME_URL,
            QueueDbContract.LinkEntry.COLUM_NAME_ANNOTATION
        };

        String sortDirection = "ASC";
        String sortOrder = QueueDbContract.LinkEntry._ID + " " + sortDirection;

        Cursor cursor = db.query(
            QueueDbContract.LinkEntry.TABLE_NAME,
            projection,
            null,  // all rows
            null,  // no selectionArgs
            null,  // don't group
            null,  // don't filter
            sortOrder
        );

        ArrayList<Link> savedLinks = new ArrayList<>();
        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndexOrThrow(QueueDbContract.LinkEntry._ID));
            String url = cursor.getString(cursor.getColumnIndexOrThrow(QueueDbContract.LinkEntry.COLUMN_NAME_URL));
            String annotation = cursor.getString(cursor.getColumnIndexOrThrow(QueueDbContract.LinkEntry.COLUM_NAME_ANNOTATION));

            Link link = new Link(id, url, annotation);
            savedLinks.add(link);
        }
        cursor.close();

        Log.d(this.toString(), "Found " + savedLinks.size() + " queued links.");
        return savedLinks;
    }

    public int queuedCount() {
        return getLinks().size();
    }

    public void saveQueuedLinks() {
        if (!Utils.storageSettingChoiceIsAPI()) {
            Log.w("OfflineQueue", "Not using API as persistence backend. Not trying to save queued links now!");
            return;
        }
        if (!Utils.networkAvailable(SaveApplication.getAppContext())) {
            Log.w("OfflineQueue", "No network available. Not trying to save queued links.");
            return;
        }

        SavePersistenceOption storage;
        try {
            storage = ((MainActivity) SaveApplication.getAppContext()).getStorage();
        } catch (ClassCastException e) {
            return;
        }

        List<Link> queuedLinks = getLinks();
        if (queuedLinks.isEmpty()) {
            Log.d("OfflineQueue", "Nothing to do. Queue is empty.");
            return;
        } else {
            Log.d("OfflineQueue", "Trying to save " + queuedLinks.size() + " queued links...");
            Utils.showSnackbar((SnackbarActivity) this.callingActivity, "Saving queued links...");
        }

        for (Link link : queuedLinks) {
            try {
                storage.saveLink(link);
            } catch (JSONException | UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        if (!queuedLinks.isEmpty()) {
            Utils.showToast(SaveApplication.getAppContext(), "Saved queued links.");
            Log.d("OfflineQueue", "Saved queued links.");
        }
    }

    private boolean isLinkQueued(Link link) {
        ArrayList<Link> queuedLinks = getLinks();
        for (Link queued : queuedLinks) {
            if (link.url().equals(queued.url())
                && link.annotation().equals(queued.annotation())) {
                return true;
            }
        }
        return false;
    }
}
