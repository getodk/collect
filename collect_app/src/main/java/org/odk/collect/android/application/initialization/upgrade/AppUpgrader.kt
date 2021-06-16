package org.odk.collect.android.application.initialization.upgrade

import org.odk.collect.shared.Settings

class AppUpgrader(private val metaSettings: Settings, private val upgrades: List<Upgrade>) {

    fun upgrade() {
        upgrades.forEach {
            val key = it.key()

            if (key == null) {
                it.run()
            } else if (!metaSettings.getBoolean(key)) {
                it.run()
                metaSettings.save(key, true)
            }
        }
    }
}

interface Upgrade {

    fun key(): String?

    fun run()
}
