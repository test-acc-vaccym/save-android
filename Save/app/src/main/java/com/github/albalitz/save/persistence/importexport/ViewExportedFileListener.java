package com.github.albalitz.save.persistence.importexport;

import android.util.Log;
import android.view.View;

import com.github.albalitz.save.utils.Utils;

/**
 * Created by albalitz on 5/21/17.
 */

public class ViewExportedFileListener implements View.OnClickListener {
    @Override
    public void onClick(View v) {
        Log.d(this.toString(), "Requesting to view export file.");
        Utils.openExportedFileInEditor();
    }
}
