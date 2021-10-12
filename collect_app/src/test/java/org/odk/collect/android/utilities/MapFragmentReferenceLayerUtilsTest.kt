package org.odk.collect.android.utilities

import android.os.Bundle
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.android.geo.DirectoryReferenceLayerRepository
import org.odk.collect.geo.maps.MapFragment
import org.odk.collect.shared.TempFiles.createTempDir
import java.io.File

@RunWith(AndroidJUnit4::class)
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
        FileUtils.write(File(layersPath, "blah"), byteArrayOf())
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
