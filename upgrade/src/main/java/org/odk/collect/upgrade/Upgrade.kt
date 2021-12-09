package org.odk.collect.upgrade

interface Upgrade {

    /**
     * If this returns a non-`null` value, the [Upgrade] will only be run once (the next app
     * upgrade). [Upgrade] implementations that return `null` from [Upgrade.key] will be run on
     * every app upgrade.
     */
    fun key(): String?

    fun run()
}
