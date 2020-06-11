package org.odk.collect.android.utilities;

import android.content.Intent;

/**
 * Delegate responsible for handling Activity results in {@link android.app.Activity}. Allows this
 * logic to be defined and tested separately from the Activity itself. Methods should be called
 * from their corresponding Activity lifecycle methods.
 */
public interface ActivityResultDelegate {

    void onActivityResult(int requestCode, int resultCode, Intent data);
}
