package org.odk.collect.android.javarosawrapper

import org.javarosa.core.reference.InvalidReferenceException
import org.javarosa.core.reference.ReferenceManager
import org.odk.collect.forms.FormMediaFileRepository
import java.io.File

class ReferenceManagerMediaFileRepository(
    private val referenceManager: ReferenceManager
) : FormMediaFileRepository {
    override fun get(uri: String): File? {
        return try {
            File(referenceManager.deriveReference(uri).localURI)
        } catch (_: InvalidReferenceException) {
            null
        }
    }
}
