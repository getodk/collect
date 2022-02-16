package org.odk.collect.settings.migration;

import org.odk.collect.shared.settings.Settings;

public interface Migration {
    void apply(Settings prefs);
}
