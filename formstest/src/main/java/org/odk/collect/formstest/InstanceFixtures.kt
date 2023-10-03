package org.odk.collect.formstest

import org.odk.collect.forms.instances.Instance
import org.odk.collect.shared.TempFiles

object InstanceFixtures {

    @JvmStatic
    @JvmOverloads
    fun instance(status: String = Instance.STATUS_INCOMPLETE, lastStatusChangeDate: Long = 0): Instance {
        val instancesDir = TempFiles.createTempDir()
        return InstanceUtils.buildInstance("formId", "version", instancesDir.absolutePath)
            .status(status)
            .lastStatusChangeDate(lastStatusChangeDate)
            .build()
    }
}
