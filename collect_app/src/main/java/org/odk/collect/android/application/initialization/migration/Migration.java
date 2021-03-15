package org.odk.collect.android.application.initialization.migration;

import org.odk.collect.android.preferences.source.Settings;

public interface Migration {
    void apply(Settings prefs);
}
