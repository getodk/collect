package org.odk.collect.android.preferences.keys;

/**
 * A pair of preference keys: An admin preference string, and the string of the general preference
 * that should be disabled when it is false.
 */
public class AdminAndGeneralKeys {
    public String adminKey;
    public String generalKey;

    private AdminAndGeneralKeys(String adminKey, String generalKey) {
        this.adminKey = adminKey;
        this.generalKey = generalKey;
    }

    /** Creates a new AdminAndGeneralKeys object from the specified admin and general preference keys */
    static AdminAndGeneralKeys ag(String adminKey, String generalKey) {
        return new AdminAndGeneralKeys(adminKey, generalKey);
    }
}
