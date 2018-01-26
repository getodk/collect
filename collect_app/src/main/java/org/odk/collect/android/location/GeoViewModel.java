package org.odk.collect.android.location;

import org.odk.collect.android.architecture.rx.RxViewModel;
import org.odk.collect.android.injection.config.scopes.PerActivity;

import javax.inject.Inject;


@PerActivity
public class GeoViewModel extends RxViewModel implements GeoViewModelType {

    @Inject
    GeoViewModel() {

    }
}
