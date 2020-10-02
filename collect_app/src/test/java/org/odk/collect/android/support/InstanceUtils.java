package org.odk.collect.android.support;

import org.odk.collect.android.instances.Instance;

public class InstanceUtils {

    private InstanceUtils() {

    }

    public static Instance.Builder buildInstance(long id, String jrFormId, String jrFormVersion) {
        return new Instance.Builder()
                .id(id)
                .jrFormId(jrFormId)
                .jrVersion(jrFormVersion)
                .status(Instance.STATUS_INCOMPLETE);
    }
}
