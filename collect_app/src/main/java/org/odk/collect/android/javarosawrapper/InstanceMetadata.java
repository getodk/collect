package org.odk.collect.android.javarosawrapper;

import org.odk.collect.android.formentry.audit.AuditConfig;
import org.odk.collect.android.utilities.FormNameUtils;

/**
 * OpenRosa metadata of a form instance.
 * <p>
 * Contains the values for the required metadata
 * fields and nothing else.
 *
 * @author mitchellsundt@gmail.com
 */
public class InstanceMetadata {
    public final String instanceId;
    public final String instanceName;
    public final AuditConfig auditConfig;

    public InstanceMetadata(String instanceId, String instanceName, AuditConfig auditConfig) {
        this.instanceId = instanceId;
        this.instanceName = FormNameUtils.normalizeFormName(instanceName, false);
        this.auditConfig = auditConfig;
    }
}
