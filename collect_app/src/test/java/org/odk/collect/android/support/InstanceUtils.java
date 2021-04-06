package org.odk.collect.android.support;

import org.odk.collect.android.instances.Instance;

public class InstanceUtils {

    private InstanceUtils() {

    }

    public static Instance.Builder buildInstance(String formId, String version) {
        return buildInstance(formId, version, "display name", Instance.STATUS_INCOMPLETE, null);
    }

    public static Instance.Builder buildInstance(String formId, String version, String displayName, String status, Long deletedDate) {
        return new Instance.Builder()
                .formId(formId)
                .formVersion(version)
                .displayName(displayName)
                .instanceFilePath(formId + version + Math.random())
                .status(status)
                .lastStatusChangeDate(System.currentTimeMillis())
                .status(status)
                .deletedDate(deletedDate);
    }
}
