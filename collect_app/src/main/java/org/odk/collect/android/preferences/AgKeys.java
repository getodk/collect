package org.odk.collect.android.preferences;

import static org.odk.collect.android.preferences.AdminKeys.adminToGeneral;

/** A pair of preference keys: An admin preference string, and the string of the general preference
 * that should be enabled when it is true.
  */
class AgKeys {
    String adminKey;
    String generalKey;

    private AgKeys(String adminKey, String generalKey) {
        this.adminKey = adminKey;
        this.generalKey = generalKey;
    }

    /** Creates a new AgKeys object from the specified admin and general preference keys */
    static AgKeys ag(String adminKey, String generalKey) {
        return new AgKeys(adminKey, generalKey);
    }

    /** Creates a new AgKeys object from the specified admin key */
    static AgKeys ag(String adminKey) {
        return new AgKeys(adminKey, adminToGeneral.get(adminKey));
    }
}
