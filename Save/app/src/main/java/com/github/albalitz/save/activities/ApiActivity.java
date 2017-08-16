package com.github.albalitz.save.activities;

import com.github.albalitz.save.persistence.Link;

import java.util.ArrayList;


/**
 * Interface providing callbacks to the Activity from the API.
 */
public interface ApiActivity {
    void onSavedLinksUpdate(ArrayList<Link> savedLinks);
    void onSaveDialogDone();

    void onRegistrationError(String errorMessage);
    void onRegistrationSuccess();
}
