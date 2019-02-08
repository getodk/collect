package org.odk.collect.android.preferences;

import android.support.annotation.NonNull;

public enum GuidanceHint {
    Yes("yes"),
    YesCollapsed("yes_collapsed"),
    No("no");

    private final String name;

    GuidanceHint(String s) {
        name = s;
    }

    @NonNull
    public static GuidanceHint get(String name) {
        for (GuidanceHint hint : GuidanceHint.values()) {
            if (hint.name.equals(name)) {
                return hint;
            }
        }

        return No;
    }

    @NonNull
    public String toString() {
        return this.name;
    }
}
