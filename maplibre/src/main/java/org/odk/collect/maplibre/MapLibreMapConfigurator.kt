package org.odk.collect.maplibre

import android.content.Context
import androidx.preference.Preference
import org.odk.collect.androidshared.system.OpenGLVersionChecker.isOpenGLv3Supported
import org.odk.collect.androidshared.ui.PrefUtils
import org.odk.collect.androidshared.ui.ToastUtils.showLongToast
import org.odk.collect.maps.MapConfigurator
import org.odk.collect.maps.layers.MbtilesFile
import org.odk.collect.shared.settings.Settings
import org.odk.collect.strings.R
import java.io.File

class MapLibreMapConfigurator(private val configuration: Configuration) : MapConfigurator {

    constructor(configuration: String) : this(Configurations.all.getValue(configuration))

    override fun isAvailable(context: Context): Boolean {
        /*
         * MapLibre requires OpenGL ES version 3 since 11.0.0.
         * See: https://github.com/maplibre/maplibre-native/blob/main/platform/android/CHANGELOG.md#1100
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
        return if (configuration.styleSetting != null) {
            listOf<Preference>(
                PrefUtils.createListPref(
                    context,
                    configuration.styleSetting,
                    context.getString(
                        R.string.map_style_label,
                        context.getString(configuration.name)
                    ),
                    configuration.styleOptions.map { it.value.name }.toIntArray(),
                    configuration.styleOptions.map { it.key }.toTypedArray(),
                    settings
                )
            )
        } else {
            emptyList()
        }
    }

    override fun supportsLayer(file: File): Boolean {
        // MapLibreMapFragment supports any file that MbtilesFile can read.
        return MbtilesFile.readLayerType(file) != null
    }

    override fun getDisplayName(file: File): String {
        val name = MbtilesFile.readName(file)
        return name ?: file.name
    }
}
