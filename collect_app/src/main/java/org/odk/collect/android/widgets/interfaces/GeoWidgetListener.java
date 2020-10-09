package org.odk.collect.android.widgets.interfaces;

import android.content.Context;
import android.os.Bundle;

import org.javarosa.core.model.FormIndex;
import org.odk.collect.android.databinding.GeoWidgetAnswerBinding;
import org.odk.collect.android.geo.MapConfigurator;
import org.odk.collect.android.utilities.PermissionUtils;
import org.odk.collect.android.widgets.utilities.WaitingForDataRegistry;

public interface GeoWidgetListener {
    void setButtonLabelAndVisibility(GeoWidgetAnswerBinding binding, boolean readOnly, boolean dataAvailable,
                                     int buttonTextReadOnly, int buttonTextDataAvailable, int defaultButtonText);

    void onButtonClicked(Context context, FormIndex index, PermissionUtils permissionUtils, MapConfigurator mapConfigurator,
                         WaitingForDataRegistry waitingForDataRegistry, Class activityClass, Bundle bundle, int requestCode);
}
