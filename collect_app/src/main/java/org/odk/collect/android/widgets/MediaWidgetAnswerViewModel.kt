package org.odk.collect.android.widgets

import android.app.Activity
import android.content.Context
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.odk.collect.android.utilities.MediaUtils
import org.odk.collect.android.utilities.QuestionMediaManager
import org.odk.collect.android.utilities.getExistingAnswerFile
import org.odk.collect.androidshared.utils.getVideoThumbnail
import org.odk.collect.async.Scheduler
import org.odk.collect.maplibre.Configurations
import org.odk.collect.maplibre.basemapStyle
import org.odk.collect.maplibre.staticmap.StaticMapCache
import org.odk.collect.maplibre.staticmap.StaticMapConfiguration
import org.odk.collect.maps.layers.MapFragmentReferenceLayerUtils
import org.odk.collect.maps.layers.ReferenceLayerRepository
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.settings.keys.ProjectKeys
import java.io.File

class MediaWidgetAnswerViewModel(
    private val scheduler: Scheduler,
    private val questionMediaManager: QuestionMediaManager,
    private val mediaUtils: MediaUtils,
    private val settingsProvider: SettingsProvider,
    private val referenceLayerRepository: ReferenceLayerRepository
) : ViewModel() {
    val staticMapCache = StaticMapCache()

    fun getFrame(answer: String?, context: Context): StateFlow<ImageBitmap?> {
        val bitmapState = MutableStateFlow<ImageBitmap?>(null)

        val file = questionMediaManager.getExistingAnswerFile(answer)
        if (file != null) {
            scheduler.immediate {
                val thumbnail = file.getVideoThumbnail(context)?.asImageBitmap()
                bitmapState.value = thumbnail
            }
        }

        return bitmapState
    }

    fun getImage(answer: String?): File? {
        return questionMediaManager.getExistingAnswerFile(answer)
    }

    fun getStaticMapConfiguration(): StaticMapConfiguration {
        val settings = settingsProvider.getUnprotectedSettings()
        val basemapSource = settings.getString(ProjectKeys.KEY_BASEMAP_SOURCE)!!

        return StaticMapConfiguration(
            basemapSource,
            Configurations.all[basemapSource]?.basemapStyle(settings),
            MapFragmentReferenceLayerUtils.getReferenceLayerFile(
                settings.getString(ProjectKeys.KEY_REFERENCE_LAYER),
                referenceLayerRepository
            )
        )
    }

    fun openFile(activity: Activity, answer: String?, mimeType: String? = null) {
        val file = questionMediaManager.getExistingAnswerFile(answer)
        if (file != null) {
            mediaUtils.openFile(activity, file, mimeType)
        }
    }
}
