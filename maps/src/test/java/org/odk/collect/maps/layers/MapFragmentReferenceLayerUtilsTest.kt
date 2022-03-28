package org.odk.collect.maps.layers

import android.os.Bundle
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.maps.MapFragment
import org.odk.collect.shared.TempFiles.createTempDir
import org.robolectric.RobolectricTestRunner
import java.io.File

@RunWith(RobolectricTestRunner::class)
class MapFragmentReferenceLayerUtilsTest {

    @Test
    fun getReferenceLayerFile_whenPathIsNull_should_getReferenceLayerFileReturnNull() {
        val layersPath = createTempDir().absolutePath
        val config = Bundle()
        config.putString(MapFragment.KEY_REFERENCE_LAYER, null)
        assertNull(
            MapFragmentReferenceLayerUtils.getReferenceLayerFile(
                config,
                DirectoryReferenceLayerRepository(layersPath)
            )
        )
    }

    @Test
    fun whenOfflineLayerFileDoesNotExist_should_getReferenceLayerFileReturnNull() {
        val layersPath = createTempDir().absolutePath
        val config = Bundle()
        config.putString(MapFragment.KEY_REFERENCE_LAYER, "blah")
        assertNull(
            MapFragmentReferenceLayerUtils.getReferenceLayerFile(
                config,
                DirectoryReferenceLayerRepository(layersPath)
            )
        )
    }

    @Test
    fun whenOfflineLayerFileExist_should_getReferenceLayerFileReturnThatFile() {
        val layersPath = createTempDir().absolutePath
        File(layersPath, "blah").writeBytes(byteArrayOf())

        val config = Bundle()
        config.putString(MapFragment.KEY_REFERENCE_LAYER, "blah")

        assertNotNull(
            MapFragmentReferenceLayerUtils.getReferenceLayerFile(
                config,
                DirectoryReferenceLayerRepository(layersPath)
            )
        )
    }
}
