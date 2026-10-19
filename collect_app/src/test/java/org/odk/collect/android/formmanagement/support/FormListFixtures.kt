package org.odk.collect.android.formmanagement.support

import org.odk.collect.forms.FormListItem
import org.odk.collect.forms.ManifestFile
import org.odk.collect.forms.MediaFile
import org.odk.collect.shared.strings.Md5.getMd5Hash
import java.io.ByteArrayInputStream

object FormListFixtures {

    private val idGenerator = FixtureIdGenerator()

    fun formListItem(
        formId: String = idGenerator.nextInt().toString(),
        manifestUrl: String? = null,
        hash: String? = "$formId-hash"
    ): FormListItem {
        return FormListItem(
            "http://example.com/forms/$formId",
            formId,
            "1",
            hash,
            "Form $formId",
            manifestUrl
        )
    }

    fun manifestFile(mediaFiles: List<MediaFile> = emptyList()): ManifestFile {
        return ManifestFile("hash", mediaFiles = mediaFiles)
    }

    fun mediaFile(
        name: String = idGenerator.nextInt().toString(),
        hash: String = getMd5Hash(ByteArrayInputStream("blah".toByteArray()))!!
    ): MediaFile {
        return MediaFile(
            name,
            hash,
            "http://example.com/media-files/$name"
        )
    }
}

private class FixtureIdGenerator {
    private var nextInt = 0

    fun nextInt(): Int {
        return nextInt++
    }
}
