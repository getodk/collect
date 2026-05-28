package org.odk.collect.maps.layers

import android.os.Bundle
import org.odk.collect.maps.MapFragment
import java.io.File

object MapFragmentReferenceLayerUtils {

    @JvmStatic
    fun getReferenceLayerFile(
        config: Bundle,
        layerRepository: ReferenceLayerRepository
    ): File? {
        val filePath = config.getString(MapFragment.KEY_REFERENCE_LAYER)
        return getReferenceLayerFile(filePath, layerRepository)
    }

    fun getReferenceLayerFile(
        filePath: String?,
        layerRepository: ReferenceLayerRepository
    ): File? {
        return if (filePath != null) {
            val referenceLayer = layerRepository.get(filePath)
            referenceLayer?.file
        } else {
            null
        }
    }
}
