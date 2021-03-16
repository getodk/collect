package org.odk.collect.android.preferences.keys;

import java.util.HashMap;
import java.util.Map;

public class MetaKeys {

    public static final String KEY_INSTALL_ID = "metadata_installid";
    public static final String KEY_MAPBOX_INITIALIZED = "mapbox_initialized";
    public static final String KEY_GOOGLE_BUG_154855417_FIXED = "google_bug_154855417_fixed";
    public static final String LAST_UPDATED_NOTIFICATION = "last_updated_notification";
    public static final String SERVER_LIST = "server_list";
    public static final String CURRENT_PROJECT_ID = "current_project_id";

    public static final String DEFAULT_PROJECT_ID = "default";

    public static Map<String, Object> getDefaults() {
        Map<String, Object> defaults = new HashMap<>();
        defaults.put(CURRENT_PROJECT_ID, DEFAULT_PROJECT_ID);
        return defaults;
    }

    private MetaKeys() {

    }
}
