package org.odk.collect.android.formentry.audit

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.odk.collect.android.javarosawrapper.FormController
import org.odk.collect.android.utilities.FileUtils
import org.odk.collect.forms.instances.Instance
import org.odk.collect.shared.strings.StringUtils.isBlank

class IdentityPromptViewModel : ViewModel() {
    private val _formEntryCancelled = MutableLiveData(false)
    val formEntryCancelled: LiveData<Boolean> = _formEntryCancelled

    private val _requiresIdentity = MutableLiveData(false)
    val requiresIdentity: LiveData<Boolean> = _requiresIdentity

    private var auditEventLogger: AuditEventLogger? = null

    private lateinit var formController: FormController
    private var instance: Instance? = null

    var user: String? = null
        private set

    var formTitle: String? = null
        private set

    init {
        updateRequiresIdentity()
    }

    fun formLoaded(formController: FormController, instance: Instance?) {
        this.formController = formController
        this.instance = instance
        formTitle = formController.getFormTitle()
        auditEventLogger = formController.getAuditEventLogger()
        updateRequiresIdentity()
    }

    fun setIdentity(identity: String?) {
        user = identity
    }

    fun done() {
        auditEventLogger?.let {
            it.user = user
        }

        updateRequiresIdentity()
    }

    fun promptDismissed() {
        if (instance == null || instance!!.status == Instance.STATUS_NEW_EDIT) {
            FileUtils.purgeMediaPath(formController.getInstanceFile()?.parent)
        }
        _formEntryCancelled.value = true
    }

    private fun updateRequiresIdentity() {
        _requiresIdentity.value = auditEventLogger != null &&
            auditEventLogger!!.isUserRequired &&
            !userIsValid(auditEventLogger!!.user)
    }

    private fun userIsValid(user: String?): Boolean {
        return user != null && !user.isEmpty() && !isBlank(user)
    }
}
