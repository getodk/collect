package org.odk.collect.android.application.initialization.migration;

import android.content.SharedPreferences;

import java.util.List;

public class PreferenceMigrator {

    private final List<Migration> migrations;

    public PreferenceMigrator(List<Migration> migrations) {

        this.migrations = migrations;
    }

    public void migrate(SharedPreferences preferences) {
        for (Migration migration : migrations) {
            migration.apply(preferences);
        }
    }
}
