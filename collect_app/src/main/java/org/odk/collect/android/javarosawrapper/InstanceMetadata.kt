package org.odk.collect.android.javarosawrapper

import org.odk.collect.android.formentry.audit.AuditConfig
import org.odk.collect.android.utilities.FormNameUtils

data class InstanceMetadata(
    @JvmField val instanceId: String?,
    private val rawInstanceName: String?,
    @JvmField val auditConfig: AuditConfig?
) {
    @JvmField val instanceName: String? = FormNameUtils.normalizeFormName(rawInstanceName, false)
}
