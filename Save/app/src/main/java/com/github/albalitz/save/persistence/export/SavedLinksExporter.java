package com.github.albalitz.save.persistence.export;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.github.albalitz.save.R;
import com.github.albalitz.save.SaveApplication;
import com.github.albalitz.save.persistence.Link;
import com.github.albalitz.save.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by albalitz on 4/29/17.
 */
public class SavedLinksExporter {
    public static boolean export(Activity context, ArrayList<Link> savedLinks) {
        int permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(context,
                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                SaveApplication.PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE);
            return false;  // done for now. This should be called again, when the user grants the permission.
        }

        File file = getExportStorageDir("save-link-export.json");  // todo: preferences

        if (savedLinks.isEmpty()) {
            Utils.showToast(SaveApplication.getAppContext(), "Nothing to export. Save something first. ;)");
            return false;
        }

        JSONArray savedLinksJson = new JSONArray();
        for (Link link : savedLinks) {
            try {
                savedLinksJson.put(link.json());
            } catch (JSONException e) {
                Log.e("SavedLinksExporter", e.toString());
            }
        }

        try {
            FileWriter fileWriter = new FileWriter(file.getAbsolutePath());
            fileWriter.write(savedLinksJson.toString(4));
            fileWriter.flush();
            fileWriter.close();
        } catch (IOException e) {
            Log.e("SavedLinksExporter", e.toString());
            return false;
        } catch (JSONException e) {
            Log.e("SavedLinksExporter", e.toString());
            return false;
        }

        Log.i("SavedLinksExporter", "Exported to " + file.getAbsolutePath());
        Utils.showToast(SaveApplication.getAppContext(), "Exported " + savedLinks.size() + " links to " + file.getPath());
        return true;
    }

    private static File getExportStorageDir(String filename) {
        File path = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), SaveApplication.getAppContext().getString(R.string.app_name));
        File file = new File(path, filename);
        file.setWritable(true);

        Log.i("SavedLinksExporter", "Creating path: " + path.getAbsolutePath() + "; for file: " + filename);
        if (!path.mkdirs() && !path.exists()) {
            Log.e("SavedLinksExporter", "Couldn't create directory.");
        }
        return file;
    }
}
