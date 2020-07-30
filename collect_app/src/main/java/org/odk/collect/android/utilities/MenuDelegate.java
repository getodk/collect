package org.odk.collect.android.utilities;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

/**
 * Delegate responsible for the menu (options menu) of an {@link android.app.Activity}. Allows the
 * menu to be defined and tested separately from the Activity itself. Methods should be called
 * from their corresponding Activity lifecycle methods.
 */
public interface MenuDelegate {

    void onCreateOptionsMenu(MenuInflater menuInflater, Menu menu);

    void onPrepareOptionsMenu(Menu menu);

    boolean onOptionsItemSelected(MenuItem item);
}
