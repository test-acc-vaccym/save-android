package com.github.albalitz.save.persistence.offline_queue;

import android.provider.BaseColumns;

/**
 * Created by albalitz on 10/6/17.
 */

public class QueueDbContract {
    protected static final String SQL_CREATE_ENTRIES = "CREATE TABLE "
        + com.github.albalitz.save.persistence.offline_queue.QueueDbContract.LinkEntry.TABLE_NAME + " (" + com.github.albalitz.save.persistence.offline_queue.QueueDbContract.LinkEntry._ID + " INTEGER PRIMARY KEY,"
        + com.github.albalitz.save.persistence.offline_queue.QueueDbContract.LinkEntry.COLUMN_NAME_URL + " TEXT,"
        + com.github.albalitz.save.persistence.offline_queue.QueueDbContract.LinkEntry.COLUM_NAME_ANNOTATION + " TEXT)";
    protected static final String SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS " + com.github.albalitz.save.persistence.offline_queue.QueueDbContract.LinkEntry.TABLE_NAME;

    /**
     * prevent instantiating this.
     */
    private QueueDbContract() {}

    /**
     * Inner class defining table contents
     */
    public static class LinkEntry implements BaseColumns {
        public static final String TABLE_NAME = "offline_queue";
        public static final String COLUMN_NAME_URL = "url";
        public static final String COLUM_NAME_ANNOTATION = "annotation";
    }
}
