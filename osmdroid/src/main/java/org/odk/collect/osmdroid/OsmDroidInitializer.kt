package org.odk.collect.osmdroid

import org.osmdroid.config.Configuration

class OsmDroidInitializer(private val userAgent: String) {

    fun initialize() {
        Configuration.getInstance().userAgentValue = userAgent
    }
}
