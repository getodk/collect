package org.odk.collect.entities.server

import org.odk.collect.forms.FormSourceException

interface EntitySource {
    @Throws(FormSourceException::class)
    fun fetchDeletedStates(integrityUrl: String, ids: List<String>): List<Pair<String, Boolean>>
}
