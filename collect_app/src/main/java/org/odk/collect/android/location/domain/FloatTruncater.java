package org.odk.collect.android.location.domain;

import android.support.annotation.NonNull;

import java.text.DecimalFormat;

public class FloatTruncater {

    @NonNull
    private final DecimalFormat decimalFormat = new DecimalFormat("#.##");

    public String truncate(float valueToTruncate) {
        return decimalFormat.format(valueToTruncate);
    }
}
