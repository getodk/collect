package org.odk.collect.android.preferences;

import static org.odk.collect.android.preferences.AdminKeys.adminToGeneral;

/**
 * A pair of preference keys: An admin preference string, and the string of the general preference
 * that should be disabled when it is false.
 */
class AdminAndGeneralKeys {
    String adminKey;
    String generalKey;

    private AdminAndGeneralKeys(String adminKey, String generalKey) {
        this.adminKey = adminKey;
        this.generalKey = generalKey;
    }

    /** Creates a new AdminAndGeneralKeys object from the specified admin and general preference keys */
    static AdminAndGeneralKeys ag(String adminKey, String generalKey) {
        return new AdminAndGeneralKeys(adminKey, generalKey);
    }

    /** Creates a new AdminAndGeneralKeys object from the specified admin key */
    static AdminAndGeneralKeys ag(String adminKey) {
        return new AdminAndGeneralKeys(adminKey, adminToGeneral.get(adminKey));
    }
}
