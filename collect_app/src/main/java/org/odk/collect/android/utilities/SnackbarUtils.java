/*

Copyright 2018 Shobhit
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package org.odk.collect.android.utilities;

import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.TextView;

import org.odk.collect.android.R;

public final class SnackbarUtils {
    private static final int DURATION_3500_MS = 3500;

    private SnackbarUtils() {

    }

    public static void showSnackbar(@NonNull View view, @NonNull String message) {
        showSnackbar(view, message, DURATION_3500_MS);
    }

    /**
     * Displays snackbar with {@param message}
     * and multi-line message enabled.
     *
     * @param view    The view to find a parent from.
     * @param message The text to show.  Can be formatted text.
     */
    public static void showSnackbar(@NonNull View view, @NonNull String message, int duration) {
        if (message.isEmpty()) {
            return;
        }

        Snackbar snackbar = Snackbar.make(view, message.trim(), duration);
        TextView textView = snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
        textView.setSingleLine(false);
        snackbar.show();
    }

    public static void showLocationSnackbar(@NonNull Context context, @NonNull View view) {
        Snackbar snackbar
                = Snackbar.make(view, R.string.background_location_collecting_message, 10000)
                .setAction(R.string.settings, v -> context.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)));
        TextView textView = snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
        textView.setSingleLine(false);
        snackbar.show();
    }
}
