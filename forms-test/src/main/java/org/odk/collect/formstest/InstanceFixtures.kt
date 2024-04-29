package org.odk.collect.formstest

import org.odk.collect.forms.Form
import org.odk.collect.forms.instances.Instance
import org.odk.collect.shared.TempFiles

object InstanceFixtures {

    fun instance(
        status: String = Instance.STATUS_INCOMPLETE,
        lastStatusChangeDate: Long = 0,
        displayName: String? = null,
        dbId: Long? = null,
        form: Form? = null,
        deletedDate: Long? = null
    ): Instance {
        val instancesDir = TempFiles.createTempDir()
        return InstanceUtils.buildInstance("formId", "version", instancesDir.absolutePath)
            .status(status)
            .lastStatusChangeDate(lastStatusChangeDate)
            .displayName(displayName)
            .dbId(dbId).also {
                if (form != null) {
                    it.formId(form.formId)
                    it.formVersion(form.version)
                }
            }
            .deletedDate(deletedDate)
            .build()
    }
}
