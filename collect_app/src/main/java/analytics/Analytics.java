package analytics;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.firebase.analytics.FirebaseAnalytics;

import org.odk.collect.android.preferences.AdminPreferencesActivity;

/**
 * Handles all analytics functionality
 */
public class Analytics {
    private static Analytics instance;

    private Context context;
    private FirebaseAnalytics firebase;

    private Analytics(Context context) {
        this.context = context;
        this.firebase = FirebaseAnalytics.getInstance(context);
    }

    /**
     * Initialize singleton and adjust collection of usage data
     */
    public static void init(Context context) {
        if (instance == null) {
            instance = new Analytics(context);
            instance.adjustCollectionSettings();
        }
    }

    public static Analytics getInstance() {
        return instance;
    }

    private void adjustCollectionSettings() {
        SharedPreferences preferences = context.getSharedPreferences(AdminPreferencesActivity.ADMIN_PREFERENCES, 0);
        boolean allowedToCollect = preferences.getBoolean(AdminPreferencesActivity.KEY_COLLECT_USAGE, true);

        enableDataCollection(allowedToCollect);
    }

    public void enableDataCollection(boolean enabled) {
        firebase.setAnalyticsCollectionEnabled(enabled);
    }
}
