package org.odk.collect.android.formmanagement.finalization

import org.javarosa.core.model.FormDef
import org.javarosa.core.model.data.StringData
import org.javarosa.core.model.instance.TreeElement
import org.javarosa.core.util.PropertyUtils
import org.javarosa.form.api.FormEntryFinalizationProcessor
import org.javarosa.form.api.FormEntryModel
import org.odk.collect.android.instancemanagement.isEdit
import org.odk.collect.forms.instances.Instance

class EditedFormFinalizationProcessor(
    private val instance: Instance?
) : FormEntryFinalizationProcessor {
    override fun processForm(formEntryModel: FormEntryModel) {
        if (instance?.isEdit() == true) {
            addDeprecatedId(formEntryModel.form)
        }
    }

    private fun addDeprecatedId(formDef: FormDef) {
        val mainInstance = formDef.mainInstance
        val metaSection = mainInstance.root.getFirstChild("meta")!!
        val instanceId = metaSection.getFirstChild("instanceID")!!
        val deprecatedId = TreeElement("deprecatedID")

        metaSection.addChild(deprecatedId)
        deprecatedId.setAnswer(instanceId.value)
        instanceId.setAnswer(StringData("uuid:" + PropertyUtils.genUUID()))
    }
}
