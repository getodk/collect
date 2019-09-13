package org.odk.collect.android.analytics.google;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import org.odk.collect.android.analytics.Analytics;

public class GoogleAnalytics implements Analytics {

    private final Tracker tracker;

    public GoogleAnalytics(Tracker tracker) {
        this.tracker = tracker;
    }

    @Override
    public void logEvent(String category, String action) {
        tracker.send(new HitBuilders.EventBuilder()
                .setCategory(category)
                .setAction(action)
                .build());
    }

    @Override
    public void logEvent(String category, String action, String label) {
        tracker.send(new HitBuilders.EventBuilder()
                .setCategory(category)
                .setAction(action)
                .setLabel(label)
                .build());
    }
}
