package org.odk.collect.android.application.initialization.migration;

import org.odk.collect.shared.Settings;

public interface Migration {
    void apply(Settings prefs);
}
