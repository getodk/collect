package org.odk.collect.android.instancemanagement.autosend

import org.odk.collect.forms.Form

fun Form.shouldFormBeSentAutomatically(isAutoSendEnabledInSettings: Boolean): Boolean {
    return if (isAutoSendEnabledInSettings) {
        autoSend == null || autoSend.trim().lowercase() != "false"
    } else {
        autoSend != null && autoSend.trim().lowercase() == "true"
    }
}
