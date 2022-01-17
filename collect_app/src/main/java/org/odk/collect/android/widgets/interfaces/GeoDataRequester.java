package org.odk.collect.android.widgets.interfaces;

import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.widgets.utilities.WaitingForDataRegistry;

public interface GeoDataRequester {

    void requestGeoPoint(FormEntryPrompt prompt, String answerText, WaitingForDataRegistry waitingForDataRegistry);

    void requestGeoShape(FormEntryPrompt prompt, String answerText, WaitingForDataRegistry waitingForDataRegistry);

    void requestGeoTrace(FormEntryPrompt prompt, String answerText, WaitingForDataRegistry waitingForDataRegistry);
}
