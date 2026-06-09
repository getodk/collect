package org.odk.collect.forms

import java.io.File

/**
 * Resolves a form-media reference (a `jr://…` URI as used by a form's images, audio, video or
 * CSV external instances) to a local file.
 */
fun interface FormMediaFileRepository {
    /**
     * @return the local file the [uri] resolves to, or null if the reference can't be resolved.
     * The returned file is not guaranteed to exist.
     */
    fun get(uri: String): File?
}
