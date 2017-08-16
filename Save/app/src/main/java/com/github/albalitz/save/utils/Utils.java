package com.github.albalitz.save.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.github.albalitz.save.R;
import com.github.albalitz.save.SaveApplication;
import com.github.albalitz.save.activities.ApiActivity;
import com.github.albalitz.save.activities.SnackbarActivity;
import com.github.albalitz.save.persistence.SavePersistenceOption;
import com.github.albalitz.save.persistence.api.Api;
import com.github.albalitz.save.persistence.database.Database;

import java.io.File;

/**
 * Created by albalitz on 3/24/17.
 */
public class Utils {

    public static void showSnackbar(SnackbarActivity callingActivity, String text) {
        View viewFromActivity = callingActivity.viewFromActivity();
        Snackbar.make(viewFromActivity, text, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    public static void showToast(Context context, String text) {
        Toast.makeText(context, text, Toast.LENGTH_LONG).show();
    }

    public static void openInExternalBrowser(Context context, String url) {
        if (!url.startsWith("http")) {
            url = "https://" + url;
        }

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        context.startActivity(intent);
    }

    public static void openExportedFileInEditor() {
        Intent intent = new Intent(Intent.ACTION_EDIT);
        Uri uri = Uri.parse(getExportFile().getPath());
        intent.setDataAndType(uri, "text/plain");
        SaveApplication.getAppContext().startActivity(intent);
    }

    public static File getExportFile() {
        SharedPreferences preferences = SaveApplication.getSharedPreferences();
        String filename = preferences.getString("pref_key_export_location", null);
        if (filename == null) {
            throw new IllegalStateException("The export filename is not available!");
        }

        File exportDirectory = new File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
            SaveApplication.getAppContext().getString(R.string.app_name));
        File exportFile = new File(exportDirectory, filename);
        exportFile.setWritable(true);
        if (!exportDirectory.mkdirs() && !exportDirectory.exists()) {
            Log.e("SavedLinksExporter", "Couldn't create directory.");
        }

        Log.d("getExportFile", "Export file: " + exportFile.getAbsolutePath());
        return exportFile;
    }

    public static boolean networkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

    public static boolean storageSettingChoiceIsAPI() {
        return SaveApplication.getSharedPreferences().getBoolean("pref_key_use_api_or_local", false);
    }
}
