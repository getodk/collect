package org.odk.collect.android.analytics;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;

import org.odk.collect.android.preferences.AdminPreferencesActivity;

/**
 * Handles all org.odk.collect.android.analytics functionality
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

    public void logScreenView(ScreenType type) {
        Bundle bundle = new Bundle();
        bundle.putString(EventConfig.SCREEN_VIEW_SCREEN_ID, getScreenId(type));
        firebase.logEvent(EventConfig.SCREEN_VIEW, bundle);
    }

    private String getScreenId(ScreenType type) {
        switch(type) {
            case Main:                      return "main";
            case FillBlankFormList:         return "fill_blank";
            case FillBlankFormSelected:     return "fill_start";
            case FormFinalized:             return "fill_end";
            case EditSavedForm:             return "edit";
            case StartedEditingSavedForm:   return "edit_start";
            case SendFinalizedForm:         return "send_finalized";
            case GetBlankForm:              return "get_blank";
            case DeleteSavedForm:           return "delete_saved";
            case DeleteBlankForm:           return "delete_blank";
            case GeneralSettings:           return "settings_general";
            case AdminSettings:             return "settings_admin";
        }

        return "unknown";
    }

    /**
     * Holds keys passed to org.odk.collect.android.analytics.
     */
    private class EventConfig {
        final static String SCREEN_VIEW = "screenview";
        final static String SCREEN_VIEW_SCREEN_ID= "screen";
    }
}
