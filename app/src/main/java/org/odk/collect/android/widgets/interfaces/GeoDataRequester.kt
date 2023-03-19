package org.odk.collect.android.widgets.interfaces

import org.javarosa.form.api.FormEntryPrompt
import org.odk.collect.android.widgets.utilities.WaitingForDataRegistry

interface GeoDataRequester {
    fun requestGeoPoint(
        prompt: FormEntryPrompt,
        answerText: String?,
        waitingForDataRegistry: WaitingForDataRegistry
    )

    fun requestGeoShape(
        prompt: FormEntryPrompt,
        answerText: String?,
        waitingForDataRegistry: WaitingForDataRegistry
    )

    fun requestGeoTrace(
        prompt: FormEntryPrompt,
        answerText: String?,
        waitingForDataRegistry: WaitingForDataRegistry
    )
}
