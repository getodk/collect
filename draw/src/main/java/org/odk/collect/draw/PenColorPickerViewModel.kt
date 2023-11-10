package org.odk.collect.draw

import android.graphics.Color
import androidx.lifecycle.ViewModel
import org.odk.collect.androidshared.livedata.MutableNonNullLiveData
import org.odk.collect.androidshared.livedata.NonNullLiveData
import org.odk.collect.shared.settings.Settings

class PenColorPickerViewModel(private val metaSettings: Settings, private val lastUsedKey: String) : ViewModel() {
    private val lastUsedPenColor: Int
        get() {
            return if (metaSettings.contains(lastUsedKey)) {
                metaSettings.getInt(lastUsedKey)
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
        metaSettings.save(lastUsedKey, color)
        _penColor.value = color
    }
}
