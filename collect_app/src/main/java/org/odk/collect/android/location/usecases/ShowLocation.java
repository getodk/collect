package org.odk.collect.android.location.usecases;

import android.support.annotation.NonNull;

import org.odk.collect.android.injection.config.scopes.PerActivity;

import javax.inject.Inject;

@PerActivity
public class ShowLocation {

    @NonNull
    private final ShowZoomDialog showZoomDialog;

    @Inject
    public ShowLocation(@NonNull ShowZoomDialog showZoomDialog) {
        this.showZoomDialog = showZoomDialog;
    }

    public void show() {
        showZoomDialog.show();
    }
}
