package org.odk.collect.android.application.initialization.migration;

import android.content.SharedPreferences;

public interface Migration {
    void apply(SharedPreferences prefs);
}
