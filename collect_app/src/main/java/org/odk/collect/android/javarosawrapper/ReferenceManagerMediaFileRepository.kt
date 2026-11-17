package org.odk.collect.android.javarosawrapper

import org.javarosa.core.reference.InvalidReferenceException
import org.javarosa.core.reference.ReferenceManager
import org.odk.collect.forms.FormMediaFileRepository
import java.io.File

class ReferenceManagerMediaFileRepository(
    private val referenceManager: ReferenceManager
) : FormMediaFileRepository {
    override fun hasFileWithName(name: String): Boolean {
        return try {
            File(referenceManager.deriveReference("jr://file/$name").localURI).exists()
        } catch (_: InvalidReferenceException) {
            false
        }
    }
}
