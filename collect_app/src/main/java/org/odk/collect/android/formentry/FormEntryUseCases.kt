package org.odk.collect.android.formentry

import org.apache.commons.io.FileUtils.readFileToByteArray
import org.javarosa.core.model.FormDef
import org.javarosa.core.model.instance.InstanceInitializationFactory
import org.javarosa.core.model.instance.TreeReference
import org.javarosa.core.model.instance.utils.DefaultAnswerResolver
import org.javarosa.core.reference.ReferenceManager
import org.javarosa.form.api.FormEntryController
import org.javarosa.xform.parse.XFormParser
import org.javarosa.xform.util.XFormUtils
import org.odk.collect.android.externaldata.ExternalAnswerResolver
import org.odk.collect.android.javarosawrapper.FailedValidationResult
import org.odk.collect.android.javarosawrapper.FormController
import org.odk.collect.android.javarosawrapper.JavaRosaFormController
import org.odk.collect.android.utilities.FileUtils
import org.odk.collect.android.utilities.FormDefCache
import org.odk.collect.android.utilities.FormUtils
import org.odk.collect.entities.EntitiesRepository
import org.odk.collect.forms.instances.Instance
import org.odk.collect.forms.instances.InstancesRepository
import java.io.File

object FormEntryUseCases {

    @JvmStatic
    fun loadFormDef(xForm: File, formMediaDir: File): FormDef? {
        FormUtils.setupReferenceManagerForForm(ReferenceManager.instance(), formMediaDir)
        return createFormDefFromCacheOrXml(xForm)
    }

    @JvmStatic
    fun loadDraft(
        formEntryController: FormEntryController,
        formMediaDir: File,
        instance: File
    ): FormController {
        val instanceInit = InstanceInitializationFactory()

        importInstance(instance, formEntryController)
        formEntryController.model.form.initialize(false, instanceInit)

        return JavaRosaFormController(
            formMediaDir,
            formEntryController,
            instance
        )
    }

    @JvmStatic
    fun finalizeDraft(
        formController: FormController,
        entitiesRepository: EntitiesRepository,
        instancesRepository: InstancesRepository
    ): Instance? {
        val valid = finalizeInstance(formController, entitiesRepository)

        return if (valid) {
            saveFormToDisk(formController)
            markInstanceAsComplete(formController, instancesRepository)
        } else {
            null
        }
    }

    private fun markInstanceAsComplete(
        formController: FormController,
        instancesRepository: InstancesRepository
    ): Instance {
        val instancePath = formController.getInstanceFile()!!.absolutePath
        val instance = instancesRepository.getOneByPath(instancePath)

        return instancesRepository.save(
            Instance.Builder(instance).also { it.status(Instance.STATUS_COMPLETE) }.build()
        )
    }

    private fun saveFormToDisk(formController: FormController) {
        val payload = formController.getFilledInFormXml()
        FileUtils.write(formController.getInstanceFile(), payload!!.payloadBytes)
    }

    @JvmStatic
    private fun finalizeInstance(
        formController: FormController,
        entitiesRepository: EntitiesRepository
    ): Boolean {
        val validationResult = formController.validateAnswers(true)
        if (validationResult is FailedValidationResult) {
            return false
        }

        formController.finalizeForm()
        formController.getEntities().forEach { entity -> entitiesRepository.save(entity) }
        return true
    }

    private fun createFormDefFromCacheOrXml(xForm: File): FormDef? {
        val formDefFromCache = FormDefCache.readCache(xForm)
        if (formDefFromCache != null) {
            return formDefFromCache
        }

        val lastSavedSrc = FileUtils.getOrCreateLastSavedSrc(xForm)
        return XFormUtils.getFormFromFormXml(xForm.absolutePath, lastSavedSrc)?.also {
            FormDefCache.writeCache(it, xForm.path)
        }
    }

    private fun importInstance(instanceFile: File, formEntryController: FormEntryController) {
        // convert files into a byte array
        val fileBytes = readFileToByteArray(instanceFile)

        // get the root of the saved and template instances
        val savedRoot = XFormParser.restoreDataModel(fileBytes, null).root
        val templateRoot = formEntryController.model.form.instance.root.deepCopy(true)

        // weak check for matching forms
        if (savedRoot.name != templateRoot.name || savedRoot.mult != 0) {
            return
        }

        // populate the data model
        val tr = TreeReference.rootRef()
        tr.add(templateRoot.name, TreeReference.INDEX_UNBOUND)

        // Here we set the Collect's implementation of the IAnswerResolver.
        // We set it back to the default after select choices have been populated.
        XFormParser.setAnswerResolver(ExternalAnswerResolver())
        templateRoot.populate(savedRoot, formEntryController.model.form)
        XFormParser.setAnswerResolver(DefaultAnswerResolver())

        // FormInstanceParser.parseInstance is responsible for initial creation of instances. It explicitly sets the
        // main instance name to null so we force this again on deserialization because some code paths rely on the main
        // instance not having a name. Must be before the call on setRoot because setRoot also sets the root's name.
        formEntryController.model.form.instance.name = null

        // populated model to current form
        formEntryController.model.form.instance.root = templateRoot

        // fix any language issues
        // :
        // http://bitbucket.org/javarosa/main/issue/5/itext-n-appearing-in-restored-instances
        if (formEntryController.model.languages != null) {
            formEntryController.model.form
                .localeChanged(
                    formEntryController.model.language,
                    formEntryController.model.form.localizer
                )
        }
    }
}