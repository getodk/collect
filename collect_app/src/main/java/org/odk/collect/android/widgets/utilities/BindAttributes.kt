package org.odk.collect.android.widgets.utilities

object BindAttributes {
    const val ALLOW_MOCK_ACCURACY = "allow-mock-accuracy"
    const val INCREMENTAL = "incremental"
    const val QUALITY = "quality"

    enum class Quality(val value: String) {
        NORMAL("normal"),
        LOW("low"),
        VOICE_ONLY("voice-only"),
        EXTERNAL("external")
    }
}
