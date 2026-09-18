package org.odk.collect.entities.server

import org.odk.collect.forms.FormSourceException
import org.odk.collect.shared.result.Result

interface EntitySource {
    fun fetchDeletedStates(
        integrityUrl: String,
        ids: List<String>
    ): Result<List<Pair<String, Boolean>>, FormSourceException>
}
