package org.odk.collect.openrosa.parse

import org.kxml2.kdom.Document
import org.odk.collect.forms.FormListItem
import org.odk.collect.forms.MediaFile
import org.odk.collect.openrosa.forms.EntityIntegrity

interface OpenRosaResponseParser {
    fun parseFormList(document: Document): List<FormListItem>?
    fun parseManifest(document: Document): List<MediaFile>?
    fun parseIntegrityResponse(doc: Document): List<EntityIntegrity>?
}
