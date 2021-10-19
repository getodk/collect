package org.odk.collect.android.utilities

import android.os.Bundle
import org.odk.collect.android.geo.MapFragment
import org.odk.collect.android.geo.ReferenceLayerRepository
import java.io.File

object MapFragmentReferenceLayerUtils {

    @JvmStatic
    fun getReferenceLayerFile(
        config: Bundle,
        layerRepository: ReferenceLayerRepository
    ): File? {
        val filePath = config.getString(MapFragment.KEY_REFERENCE_LAYER)
        return if (filePath != null) {
            val referenceLayer = layerRepository.get(filePath)
            referenceLayer?.file
        } else {
            null
        }
    }
}
