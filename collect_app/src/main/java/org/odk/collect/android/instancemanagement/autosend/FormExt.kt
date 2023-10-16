package org.odk.collect.android.instancemanagement.autosend

import org.odk.collect.android.analytics.AnalyticsEvents
import org.odk.collect.android.analytics.AnalyticsUtils
import org.odk.collect.forms.Form

fun Form.shouldFormBeSentAutomatically(isAutoSendEnabledInSettings: Boolean): Boolean {
    if (!autoSend.isNullOrEmpty()) {
        AnalyticsUtils.logFormEvent(AnalyticsEvents.FORM_LEVEL_AUTO_SEND, formId, displayName)
    }

    return if (isAutoSendEnabledInSettings) {
        autoSend == null || autoSend.trim().lowercase() != "false"
    } else {
        autoSend != null && autoSend.trim().lowercase() == "true"
    }
}
