package org.odk.collect.settings.migration;

import org.odk.collect.shared.Settings;

public interface Migration {
    void apply(Settings prefs);
}
