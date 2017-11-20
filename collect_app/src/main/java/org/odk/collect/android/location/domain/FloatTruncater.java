package org.odk.collect.android.location.domain;

import android.support.annotation.NonNull;

import org.odk.collect.android.injection.scopes.ActivityScope;

import java.text.DecimalFormat;

import javax.inject.Inject;

@ActivityScope
public class FloatTruncater {

    @Inject
    public FloatTruncater() {
    }

    @NonNull
    private final DecimalFormat decimalFormat = new DecimalFormat("#.##");

    public String truncate(float valueToTruncate) {
        return decimalFormat.format(valueToTruncate);
    }
}
