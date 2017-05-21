package com.github.albalitz.save.persistence.export;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.View;

import com.github.albalitz.save.R;
import com.github.albalitz.save.SaveApplication;

import java.io.File;

/**
 * Created by albalitz on 5/21/17.
 */

public class ViewExportedFileListener implements View.OnClickListener {
    @Override
    public void onClick(View v) {
        Log.d(this.toString(), "Requesting to view export file.");
        Intent intent = new Intent(Intent.ACTION_EDIT);
        Uri uri = Uri.parse(new File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
            SaveApplication.getAppContext().getString(R.string.app_name)).getPath()
            + "/save-link-export.json");  // todo: read this from preferences
        intent.setDataAndType(uri, "text/plain");
        SaveApplication.getAppContext().startActivity(intent);
    }
}
