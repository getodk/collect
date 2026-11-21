package org.odk.collect.android.feature.maps

import android.app.Application
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.storage.StorageSubdirectory
import org.odk.collect.android.support.pages.Page
import org.odk.collect.android.support.rules.CollectTestRule
import org.odk.collect.android.support.rules.TestRuleChain.chain
import org.odk.collect.android.widgets.geo.StaticMapSemantics
import org.odk.collect.maplibre.MapLibreSupport
import org.odk.collect.settings.keys.ProjectKeys
import org.odk.collect.strings.R.string
import org.odk.collect.testshared.AssertionFramework
import org.odk.collect.testshared.WaitFor.waitFor
import java.io.ByteArrayOutputStream
import java.io.File

@RunWith(AndroidJUnit4::class)
class OfflineLayerTest {
    private val rule = CollectTestRule()

    @get:Rule
    val chain: RuleChain = chain().around(rule)

    private val component by lazy {
        DaggerUtils.getComponent(ApplicationProvider.getApplicationContext<Application>())
    }

    /**
     * The layer only has one solid color tile at zoom 0 (and declares maxzoom 0), so at any zoom
     * above 0 it only shows up if the map knows the layer's max zoom and scales up the zoom 0 tile
     * instead of asking for tiles the layer doesn't have.
     */
    @Test
    fun showsOfflineLayerZoomedInBeyondItsMaxZoom() {
        assertThat("MapLibre isn't available on this device (x86?)", MapLibreSupport.isAvailable(), equalTo(true))

        rule.startAtMainMenu()
            .also {
                setUpOsmBasemap()
                setUpOfflineLayer()
            }
            .copyForm("geotrace-with-answer.xml")
            .startBlankForm("Geotrace with answer")
            .also { assertPreviewShowsLayer() }
            .clickOnString(string.view_or_change_line, AssertionFramework.COMPOSE)
            .checkIsIdDisplayed(org.odk.collect.geo.R.id.zoom)
            .also { assertInteractiveMapShowsLayer() }
    }

    private fun setUpOsmBasemap() {
        component.settingsProvider()
            .getUnprotectedSettings()
            .save(ProjectKeys.KEY_BASEMAP_SOURCE, ProjectKeys.BASEMAP_SOURCE_OSM)
    }

    private fun setUpOfflineLayer() {
        val layersDir = File(component.storagePathProvider().getOdkDirPath(StorageSubdirectory.LAYERS))
        createSolidColorMbtiles(File(layersDir, LAYER_FILE_NAME))
        component.settingsProvider()
            .getUnprotectedSettings()
            .save(ProjectKeys.KEY_REFERENCE_LAYER, LAYER_FILE_NAME)
    }

    private fun createSolidColorMbtiles(file: File) {
        val tile = ByteArrayOutputStream().also {
            Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888)
                .apply { eraseColor(COLOR) }
                .compress(Bitmap.CompressFormat.PNG, 100, it)
        }.toByteArray()

        SQLiteDatabase.openOrCreateDatabase(file, null).use { db ->
            db.execSQL("CREATE TABLE metadata (name TEXT, value TEXT)")
            db.execSQL("CREATE TABLE tiles (zoom_level INTEGER, tile_column INTEGER, tile_row INTEGER, tile_data BLOB)")
            db.execSQL("INSERT INTO metadata VALUES ('minzoom', '0'), ('maxzoom', '0')")
            db.execSQL("INSERT INTO tiles VALUES (0, 0, 0, ?)", arrayOf(tile))
        }
    }

    private fun assertPreviewShowsLayer() {
        waitFor {
            val preview = Page.composeRule!!.onNodeWithTag(StaticMapSemantics.TAG).captureToImage().asAndroidBitmap()
            assertThat(preview.leftEdgeColor(), equalTo(COLOR))
        }
    }

    private fun assertInteractiveMapShowsLayer() {
        waitFor {
            val screen = InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()
            assertThat(screen.leftEdgeColor(), equalTo(COLOR))
        }
    }

    /**
     * Halfway down the left edge, the map is far from the line (bottom left to top right),
     * the map's buttons (right side) and the scale bar (top left).
     */
    private fun Bitmap.leftEdgeColor() = getPixel(width / 20, height / 2)

    companion object {
        private const val LAYER_FILE_NAME = "layer.mbtiles"
        private const val COLOR = Color.RED
    }
}
