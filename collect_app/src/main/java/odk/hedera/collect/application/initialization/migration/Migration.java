package odk.hedera.collect.application.initialization.migration;

import android.content.SharedPreferences;

public interface Migration {
    void apply(SharedPreferences prefs);
}
