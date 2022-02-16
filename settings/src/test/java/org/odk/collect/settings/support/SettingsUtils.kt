package org.odk.collect.settings.support

import org.junit.Assert.assertEquals
import org.odk.collect.shared.settings.Settings

object SettingsUtils {

    @JvmStatic
    fun initSettings(settings: Settings, vararg pairs: Any?) {
        settings.clear()

        for (i in pairs.indices.step(2)) {
            settings.save((pairs[i] as String?)!!, pairs[i + 1])
        }
    }

    @JvmStatic
    fun assertSettings(settings: Settings, vararg pairs: Any?) {
        for (i in pairs.indices.step(2)) {
            assertEquals(pairs[i + 1], settings.getAll()[pairs[i]])
        }

        assertEquals((pairs.size / 2).toLong(), settings.getAll().size.toLong())
    }

    @JvmStatic
    fun assertSettingsEmpty(settings: Settings) {
        assertEquals(0, settings.getAll().size.toLong())
    }
}
