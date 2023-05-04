package org.odk.collect.forms

fun Form.shouldFormBeSentAutomatically(isAutoSendEnabledInSettings: Boolean): Boolean {
    return if (isAutoSendEnabledInSettings) {
        autoSend == null || autoSend.trim().lowercase() != "false"
    } else {
        autoSend != null && autoSend.trim().lowercase() == "true"
    }
}
