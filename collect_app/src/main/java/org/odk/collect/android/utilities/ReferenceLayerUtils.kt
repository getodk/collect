package org.odk.collect.android.utilities

import android.os.Bundle
import org.odk.collect.android.geo.DirectoryReferenceLayerRepository
import org.odk.collect.android.geo.MapFragment
import java.io.File

object ReferenceLayerUtils {

    @JvmStatic
    fun getReferenceLayerFile(config: Bundle, layersPath: String?): File? {
        val filePath = config.getString(MapFragment.KEY_REFERENCE_LAYER)
        return if (filePath != null) {
            val referenceLayer = DirectoryReferenceLayerRepository(layersPath!!).get(filePath)
            return referenceLayer?.file
        } else {
            null
        }
    }
}
