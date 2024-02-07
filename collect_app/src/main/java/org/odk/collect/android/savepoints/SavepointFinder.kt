package org.odk.collect.android.savepoints

import android.net.Uri
import org.odk.collect.android.external.FormsContract
import org.odk.collect.android.utilities.ContentUriHelper
import org.odk.collect.forms.FormsRepository
import org.odk.collect.forms.instances.InstancesRepository
import org.odk.collect.forms.savepoints.Savepoint
import org.odk.collect.forms.savepoints.SavepointsRepository
import java.io.File

object SavepointFinder {
    fun getSavepoint(
        uri: Uri,
        uriMimeType: String,
        formsRepository: FormsRepository,
        instanceRepository: InstancesRepository,
        savepointsRepository: SavepointsRepository
    ): Savepoint? {
        return if (uriMimeType == FormsContract.CONTENT_ITEM_TYPE) {
            val selectedForm = formsRepository.get(ContentUriHelper.getIdFromUri(uri))!!

            formsRepository.getAllByFormId(selectedForm.formId)
                .filter { it.date <= selectedForm.date }
                .sortedByDescending { it.date }
                .forEach { form ->
                    val savepoint = savepointsRepository.get(form.dbId, null)
                    if (savepoint != null && File(savepoint.savepointFilePath).exists()) {
                        return savepoint
                    }
                }
            null
        } else {
            val instance = instanceRepository.get(ContentUriHelper.getIdFromUri(uri))!!
            val form = formsRepository.getLatestByFormIdAndVersion(instance.formId, instance.formVersion)!!

            val savepoint = savepointsRepository.get(form.dbId, instance.dbId)
            if (savepoint != null &&
                File(savepoint.savepointFilePath).exists() &&
                File(savepoint.savepointFilePath).lastModified() > File(instance.instanceFilePath).lastModified()
            ) {
                savepoint
            } else {
                null
            }
        }
    }
}
