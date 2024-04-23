package org.odk.collect.settings.enums

enum class AutoSend(private val value: String) {
    OFF("off"),
    WIFI_ONLY("wifi_only"),
    CELLULAR_ONLY("cellular_only"),
    WIFI_AND_CELLULAR("wifi_and_cellular");

    companion object {

        @JvmStatic
        fun parse(value: String): AutoSend {
            return entries.find { it.value == value } ?: throw IllegalArgumentException()
        }
    }
}
