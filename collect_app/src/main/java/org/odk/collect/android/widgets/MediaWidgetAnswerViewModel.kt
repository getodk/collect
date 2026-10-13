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
import org.odk.collect.maplibre.staticmap.StaticMapCache
import org.odk.collect.maplibre.staticmap.StaticMapConfiguration
import java.io.File
import javax.inject.Provider

class MediaWidgetAnswerViewModel(
    private val scheduler: Scheduler,
    private val questionMediaManager: QuestionMediaManager,
    private val mediaUtils: MediaUtils,
    private val staticMapConfiguration: Provider<StaticMapConfiguration>,
    val staticMapCache: StaticMapCache
) : ViewModel() {
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
        return staticMapConfiguration.get()
    }

    fun openFile(activity: Activity, answer: String?, mimeType: String? = null) {
        val file = questionMediaManager.getExistingAnswerFile(answer)
        if (file != null) {
            mediaUtils.openFile(activity, file, mimeType)
        }
    }
}
