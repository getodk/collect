package org.odk.collect.android.widgets.interfaces;

import android.content.Context;
import android.os.Bundle;

import org.javarosa.core.model.FormIndex;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.widgets.utilities.WaitingForDataRegistry;

public interface ActivityGeoDataRequester {

    Bundle requestGeoPoint(FormEntryPrompt prompt);

    Bundle requestGeoShape(FormEntryPrompt prompt);

    Bundle requestGeoTrace(FormEntryPrompt prompt);

    void requestGeoIntent(Context context, FormIndex index, WaitingForDataRegistry waitingForDataRegistry,
                          Class activityClass, Bundle bundle, int requestCode);
}
