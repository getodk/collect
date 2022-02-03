package org.odk.collect.settings.support

import org.junit.Assert
import org.odk.collect.shared.settings.Settings

object SettingsUtils {

    @JvmStatic
    fun initSettings(settings: Settings, vararg pairs: Any?) {
        settings.clear()
        var i = 0
        while (i + 1 < pairs.size) {
            settings.save((pairs[i] as String?)!!, pairs[i + 1])
            i += 2
        }
    }

    @JvmStatic
    fun assertSettings(settings: Settings, vararg pairs: Any?) {
        var i = 0
        while (i + 1 < pairs.size) {
            Assert.assertEquals(pairs[i + 1], settings.getAll()[pairs[i]])
            i += 2
        }
        Assert.assertEquals((pairs.size / 2).toLong(), settings.getAll().size.toLong())
    }

    @JvmStatic
    fun assertSettingsEmpty(settings: Settings) {
        Assert.assertEquals(0, settings.getAll().size.toLong())
    }
}
