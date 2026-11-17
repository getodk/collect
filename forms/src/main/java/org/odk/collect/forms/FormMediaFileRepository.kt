package org.odk.collect.forms

/**
 * Gives access to the media files (images, audio, video, CSV, XML, GeoJSON…) of the form that is
 * currently loaded.
 */
fun interface FormMediaFileRepository {
    /**
     * @return true if the form has a media file with the given name.
     */
    fun hasFileWithName(name: String): Boolean
}
