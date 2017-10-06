package com.github.albalitz.save.persistence;

import java.util.ArrayList;

/**
 * Interface providing callbacks to a class from the API.
 */
public interface ApiUser {
    void onSavedLinksUpdate(ArrayList<Link> savedLinks);
}
