package com.github.albalitz.save.persistence.importexport;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.github.albalitz.save.SaveApplication;
import com.github.albalitz.save.persistence.Link;
import com.github.albalitz.save.persistence.SavePersistenceOption;
import com.github.albalitz.save.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by albalitz on 10/17/17.
 */
public class SavedLinksImporter {
    public static ArrayList<Link> importLinks(Context context, SavePersistenceOption persistenceOption) throws JSONException, IOException {
        int permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) context,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    SaveApplication.PERMISSION_REQUEST_READ_EXTERNAL_STORAGE);
            return null;  // done for now. This should be called again, when the user grants the permission.
        }

        Utils.showToast(context, "Importing links...");
        Log.d("SavedLinksImporter", "Importing links...");

        File exportFile = Utils.getExportFile();
        exportFile.setReadable(true);

        FileInputStream fileInputStream = new FileInputStream(exportFile);
        StringBuffer fileContent = new StringBuffer("");
        byte[] buffer = new byte[1024];
        int readBytes;
        while ((readBytes = fileInputStream.read(buffer)) != -1) {
            fileContent.append(new String(buffer, 0, readBytes));
        }

        JSONArray linksJSON;
        try {
            Log.d("SavedLinksImporter", "Trying to parse Links from JSON: " + fileContent.toString());
            linksJSON = new JSONArray(fileContent.toString());
        } catch (JSONException e) {
            e.printStackTrace();
            return new ArrayList<>();
        }

        ArrayList<Link> links = new ArrayList<>();
        for (int i = 0; i < linksJSON.length(); i++) {
            Link link = new Link((JSONObject) linksJSON.get(i));
            persistenceOption.saveLink(link);  // TODO: save multiple links with one call to save API requests
            links.add(link);
        }

        return links;
    }
}
