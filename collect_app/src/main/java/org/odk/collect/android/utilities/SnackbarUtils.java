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

import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.TextView;

public final class SnackbarUtils {
    private static final int LONG_DURATION_MS = 3500;

    private SnackbarUtils() {

    }

    /**
     * Displays snackbar with {@param message} for {@link SnackbarUtils#LONG_DURATION_MS}
     * and multi-line message enabled.
     *
     * @param view    The view to find a parent from.
     * @param message The text to show.  Can be formatted text.
     */
    public static void showSnackbar(@NonNull View view, @NonNull String message) {
        if (message.isEmpty()) {
            return;
        }

        Snackbar snackbar = Snackbar.make(view, message.trim(), LONG_DURATION_MS);
        TextView textView = snackbar.getView().findViewById(android.support.design.R.id.snackbar_text);
        textView.setSingleLine(false);
        snackbar.show();
    }
}
