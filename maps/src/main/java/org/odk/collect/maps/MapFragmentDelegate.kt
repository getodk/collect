package org.odk.collect.maps

import android.os.Bundle
import org.odk.collect.shared.settings.Settings
import org.odk.collect.shared.settings.Settings.OnSettingChangeListener
import java.util.function.Consumer

class MapFragmentDelegate(
    private val configurator: MapConfigurator,
    private val settings: Settings,
    private val onConfigChanged: Consumer<Bundle>,
) :
    OnSettingChangeListener {

    fun onStart() {
        onConfigChanged.accept(configurator.buildConfig(settings))
        settings.registerOnSettingChangeListener(this)
    }

    fun onStop() {
        settings.unregisterOnSettingChangeListener(this)
    }

    override fun onSettingChanged(key: String) {
        if (configurator.prefKeys.contains(key)) {
            onConfigChanged.accept(configurator.buildConfig(settings))
        }
    }
}
