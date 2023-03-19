package org.odk.collect.android.draw

import android.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import org.odk.collect.androidshared.livedata.MutableNonNullLiveData
import org.odk.collect.androidshared.livedata.NonNullLiveData
import org.odk.collect.settings.keys.MetaKeys
import org.odk.collect.shared.settings.Settings

class PenColorPickerViewModel(private val metaSettings: Settings) : ViewModel() {
    private val lastUsedPenColor: Int
        get() {
            return if (metaSettings.contains(MetaKeys.LAST_USED_PEN_COLOR)) {
                metaSettings.getInt(MetaKeys.LAST_USED_PEN_COLOR)
            } else {
                Color.BLACK
            }
        }

    var isDefaultValue = true
        private set

    private val _penColor = MutableNonNullLiveData(lastUsedPenColor)
    val penColor: NonNullLiveData<Int> = _penColor

    fun setPenColor(color: Int) {
        isDefaultValue = false
        metaSettings.save(MetaKeys.LAST_USED_PEN_COLOR, color)
        _penColor.value = color
    }

    open class Factory(private val metaSettings: Settings) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PenColorPickerViewModel(metaSettings) as T
        }
    }
}
