package org.odk.collect.android.widgets.interfaces

interface WidgetDataReceiver {
    /**
     * Allows the answer to be set externally in [org.odk.collect.android.activities.FormFillingActivity].
     */
    fun setData(answer: Any)
}
