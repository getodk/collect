package org.odk.collect.mapbox

import android.content.Context
import androidx.preference.Preference
import com.mapbox.maps.Style
import org.odk.collect.androidshared.system.OpenGLVersionChecker.isOpenGLv3Supported
import org.odk.collect.androidshared.ui.PrefUtils
import org.odk.collect.androidshared.ui.ToastUtils.showLongToast
import org.odk.collect.maps.MapConfigurator
import org.odk.collect.maps.layers.MbtilesFile
import org.odk.collect.settings.keys.ProjectKeys
import org.odk.collect.shared.settings.Settings
import org.odk.collect.strings.R
import java.io.File

class MapboxMapConfigurator(configuration: String) : MapConfigurator {

    private val configuration = configurations.getValue(configuration)

    override fun isAvailable(context: Context): Boolean {
        /*
         * The Mapbox SDK for Android requires OpenGL ES version 3.
         * See: https://github.com/mapbox/mapbox-maps-android/blob/main/CHANGELOG.md#1100-november-29-2023
         */
        return isOpenGLv3Supported(context)
    }

    override fun showUnavailableMessage(context: Context) {
        showLongToast(
            context.getString(
                R.string.basemap_source_unavailable,
                context.getString(configuration.name)
            )
        )
    }

    override fun createPrefs(context: Context, settings: Settings): List<Preference> {
        return if (configuration.settingsKey != null) {
            listOf<Preference>(
                PrefUtils.createListPref(
                    context,
                    configuration.settingsKey,
                    context.getString(
                        R.string.map_style_label,
                        context.getString(configuration.name)
                    ),
                    configuration.options.map { it.labelId }.toIntArray(),
                    configuration.options.map { it.url }.toTypedArray(),
                    settings
                )
            )
        } else {
            emptyList()
        }
    }

    override fun supportsLayer(file: File): Boolean {
        // MapboxMapFragment supports any file that MbtilesFile can read.
        return MbtilesFile.readLayerType(file) != null
    }

    override fun getDisplayName(file: File): String {
        val name = MbtilesFile.readName(file)
        return if (name != null) name else file.getName()
    }

    private class MapboxConfiguration(
        val name: Int,
        val settingsKey: String?,
        val options: Array<MapboxUrlOption> = emptyArray()
    )

    private class MapboxUrlOption(val url: String, val labelId: Int)

    companion object {
        private val configurations = mapOf(
            ProjectKeys.BASEMAP_SOURCE_MAPBOX to MapboxConfiguration(
                R.string.basemap_source_mapbox,
                ProjectKeys.KEY_MAPBOX_MAP_STYLE,
                arrayOf(
                    MapboxUrlOption(Style.MAPBOX_STREETS, R.string.streets),
                    MapboxUrlOption(Style.LIGHT, R.string.light),
                    MapboxUrlOption(Style.DARK, R.string.dark),
                    MapboxUrlOption(Style.SATELLITE, R.string.satellite),
                    MapboxUrlOption(Style.SATELLITE_STREETS, R.string.hybrid),
                    MapboxUrlOption(Style.OUTDOORS, R.string.outdoors)
                )
            ),
            ProjectKeys.BASEMAP_SOURCE_OSM to MapboxConfiguration(R.string.basemap_source_osm, null),
            ProjectKeys.BASEMAP_SOURCE_USGS to MapboxConfiguration(
                R.string.basemap_source_usgs,
                ProjectKeys.KEY_USGS_MAP_STYLE,
                arrayOf(
                    MapboxUrlOption("topographic", R.string.topographic),
                    MapboxUrlOption("hybrid", R.string.hybrid),
                    MapboxUrlOption("satellite", R.string.satellite),
                )
            ),
            ProjectKeys.BASEMAP_SOURCE_CARTO to MapboxConfiguration(
                R.string.basemap_source_carto,
                ProjectKeys.KEY_CARTO_MAP_STYLE,
                arrayOf(
                    MapboxUrlOption("positron", R.string.openmap_cartodb_positron),
                    MapboxUrlOption("dark_matter", R.string.openmap_cartodb_darkmatter),
                )
            )
        )
    }
}
