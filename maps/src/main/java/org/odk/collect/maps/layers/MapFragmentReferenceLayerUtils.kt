package org.odk.collect.maps.layers

import java.io.File

object MapFragmentReferenceLayerUtils {

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
