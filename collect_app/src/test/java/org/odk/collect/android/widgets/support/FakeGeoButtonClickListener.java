package org.odk.collect.android.widgets.support;

import android.content.Context;
import android.os.Bundle;

import org.javarosa.core.model.FormIndex;
import org.odk.collect.android.utilities.PermissionUtils;
import org.odk.collect.android.widgets.interfaces.GeoButtonClickListener;
import org.odk.collect.android.widgets.utilities.WaitingForDataRegistry;

public class FakeGeoButtonClickListener implements GeoButtonClickListener {

    public Class activityClass;
    public Bundle geoBundle;
    public int requestCode;

    @Override
    public void requestGeoIntent(Context context, FormIndex formIndex, PermissionUtils permissionUtils,
                                 WaitingForDataRegistry waitingForDataRegistry, Class activityClass, Bundle geoBundle, int requestCode) {
        this.activityClass = activityClass;
        this.geoBundle = geoBundle;
        this.requestCode = requestCode;
    }
}
