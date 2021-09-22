package org.odk.collect.android.utilities

import android.os.Bundle
import org.odk.collect.android.geo.MapFragment
import org.odk.collect.shared.PathUtils.getAbsoluteFilePath
import java.io.File

object ReferenceLayerUtils {

    @JvmStatic
    fun getReferenceLayerFile(config: Bundle, layersPath: String?): File? {
        val filePath = config.getString(MapFragment.KEY_REFERENCE_LAYER)
        return if (filePath != null) {
            val file = File(getAbsoluteFilePath(layersPath!!, filePath))
            if (file.exists()) file else null
        } else {
            null
        }
    }
}
