package org.odk.collect.android.instancemanagement.autosend

import org.odk.collect.android.analytics.AnalyticsEvents
import org.odk.collect.android.analytics.AnalyticsUtils
import org.odk.collect.forms.Form

fun Form.shouldFormBeSentAutomatically(isAutoSendEnabledInSettings: Boolean): Boolean {
    if (!autoSend.isNullOrEmpty()) {
        AnalyticsUtils.logFormEvent(AnalyticsEvents.FORM_LEVEL_AUTO_SEND, formId, displayName)
    }

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

enum class FormAutoSendMode {
    OPT_OUT,
    FORCED,
    NEUTRAL
}
