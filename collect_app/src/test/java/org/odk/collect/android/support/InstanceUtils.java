package org.odk.collect.android.support;

import org.odk.collect.android.instances.Instance;

import java.io.File;

public class InstanceUtils {

    private InstanceUtils() {

    }

    public static Instance.Builder buildInstance(String formId, String version, String instancesDir) {
        return buildInstance(formId, version, "display name", Instance.STATUS_INCOMPLETE, null, instancesDir);
    }

    public static Instance.Builder buildInstance(String formId, String version, String displayName, String status, Long deletedDate, String instancesDir) {
        File instanceDir = new File(instancesDir + File.separator + System.currentTimeMillis() + Math.random());
        instanceDir.mkdir();

        return new Instance.Builder()
                .formId(formId)
                .formVersion(version)
                .displayName(displayName)
                .instanceFilePath(instanceDir.getAbsolutePath())
                .status(status)
                .lastStatusChangeDate(System.currentTimeMillis())
                .status(status)
                .deletedDate(deletedDate);
    }
}
