package org.odk.collect.android.espressoutils;

import android.app.Activity;

/**
 * Base class for Page Objects (https://www.martinfowler.com/bliki/PageObject.html)
 * used in Espresso tests. Provides shared helpers/setup.
 */

abstract class Page {

    private final Activity activity;

    Page(Activity activity) {
        this.activity = activity;
    }

    String getString(Integer id) {
        return activity.getString(id);
    }
}
