package org.odk.collect.android.instancemanagement.autosend

import org.odk.collect.forms.Form

fun Form.shouldFormBeSentAutomatically(isAutoSendEnabledInSettings: Boolean): Boolean {
    return if (isAutoSendEnabledInSettings) {
        getAutoSendMode() != FormAutoSendMode.OPT_OUT
    } else {
        getAutoSendMode() == FormAutoSendMode.FORCED
    }
}

fun Form.getAutoSendMode(): FormAutoSendMode {
    return if (autoSend?.trim()?.lowercase() == "false") {
        FormAutoSendMode.OPT_OUT
    } else if (autoSend?.trim()?.lowercase() == "true") {
        FormAutoSendMode.FORCED
    } else {
        FormAutoSendMode.NEUTRAL
    }
}

fun Form.getLastUpdated(): Long {
    return lastDetectedAttachmentsUpdateDate ?: date
}

enum class FormAutoSendMode {
    OPT_OUT,
    FORCED,
    NEUTRAL
}
