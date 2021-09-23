package org.odk.collect.android.utilities

import android.os.Bundle
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.android.geo.MapFragment
import org.odk.collect.shared.TempFiles.createTempDir
import java.io.File

@RunWith(AndroidJUnit4::class)
class ReferenceLayerUtilsTest {

    @Test
    fun getReferenceLayerFile_whenPathIsNull_should_getReferenceLayerFileReturnNull() {
        val layersPath = createTempDir().absolutePath
        val config = Bundle()
        config.putString(MapFragment.KEY_REFERENCE_LAYER, null)
        assertNull(ReferenceLayerUtils.getReferenceLayerFile(config, layersPath))
    }

    @Test
    fun whenOfflineLayerFileDoesNotExist_should_getReferenceLayerFileReturnNull() {
        val layersPath = createTempDir().absolutePath
        val config = Bundle()
        config.putString(MapFragment.KEY_REFERENCE_LAYER, "blah")
        assertNull(ReferenceLayerUtils.getReferenceLayerFile(config, layersPath))
    }

    @Test
    fun whenOfflineLayerFileExist_should_getReferenceLayerFileReturnThatFile() {
        val layersPath = createTempDir().absolutePath
        FileUtils.write(File(layersPath, "blah"), byteArrayOf())
        val config = Bundle()
        config.putString(MapFragment.KEY_REFERENCE_LAYER, "blah")
        assertNotNull(ReferenceLayerUtils.getReferenceLayerFile(config, layersPath))
    }
}
