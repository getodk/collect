package org.odk.collect.forms

import java.io.InputStream

/**
 * A place where forms live (outside the app's storage). Ideally in future this would be
 * a common interface for getting forms from a server, Google Drive or even the disk.
 */
interface FormSource {
    @Throws(FormSourceException::class)
    fun fetchFormList(): List<FormListItem>

    @Throws(FormSourceException::class)
    fun fetchManifest(manifestURL: String): ManifestFile

    @Throws(FormSourceException::class)
    fun fetchForm(formURL: String): InputStream

    @Throws(FormSourceException::class)
    fun fetchMediaFile(mediaFileURL: String): InputStream
}
