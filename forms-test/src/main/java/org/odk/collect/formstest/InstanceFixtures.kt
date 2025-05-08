package org.odk.collect.formstest

import org.odk.collect.forms.Form
import org.odk.collect.forms.instances.Instance
import org.odk.collect.shared.TempFiles
import java.io.File

object InstanceFixtures {

    fun instance(
        status: String = Instance.STATUS_INCOMPLETE,
        lastStatusChangeDate: Long = 0,
        displayName: String = "Form",
        dbId: Long? = null,
        form: Form? = null,
        deletedDate: Long? = null,
        canDeleteBeforeSend: Boolean = true,
        instancesDir: File = TempFiles.createTempDir(),
        formId: String = "formId",
        formVersion: String = "version",
        editOf: Long? = null,
        editNumber: Long? = null
    ): Instance {
        return InstanceUtils.buildInstance(formId, formVersion, instancesDir.absolutePath)
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
            .canDeleteBeforeSend(canDeleteBeforeSend)
            .editOf(editOf)
            .editNumber(editNumber)
            .build()
    }
}
