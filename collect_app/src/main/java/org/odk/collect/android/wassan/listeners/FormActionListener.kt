package org.odk.collect.android.wassan.listeners

interface FormActionListener {
    fun onDraftClick(formId: String)
    fun onReadyClick(formId: String)
    fun onSentClick(formId: String)
}
