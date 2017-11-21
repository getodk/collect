package org.odk.collect.android.location.domain;


import org.odk.collect.android.injection.scopes.ActivityScope;

import javax.inject.Inject;

@ActivityScope
public class ShowLocation {

    @Inject
    public ShowLocation() {
    }

    public void showLocation() {
//        showZoomDialog();
    }
}
