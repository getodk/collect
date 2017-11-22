package org.odk.collect.android.location;

import android.databinding.ObservableField;
import android.support.annotation.NonNull;

import com.google.android.gms.maps.GoogleMap;
import com.jakewharton.rxrelay2.PublishRelay;

import org.odk.collect.android.location.domain.UpdateMap;
import org.odk.collect.android.viewmodel.RxViewModel;

import javax.inject.Inject;

import io.reactivex.Observable;

public class GeoViewModel extends RxViewModel {

    // Outputs:
    public final ObservableField<GeoProvider> provider = new ObservableField<>();
    public final ObservableField<GeoMode> mode = new ObservableField<>();
    public final ObservableField<String> status = new ObservableField<>();

    public final ObservableField<String> info = new ObservableField<>();

    // Inputs:
    private final PublishRelay<Object> acceptClicks = PublishRelay.create();
    private final PublishRelay<Object> reloadClicks = PublishRelay.create();


    @NonNull
    private final UpdateMap updateMap;

    @Inject
    public GeoViewModel(@NonNull UpdateMap updateMap) {
        this.updateMap = updateMap;
    }

    Observable<Object> observeAcceptLocation() {
        return acceptClicks.hide();
    }

    Observable<Object> observeReloadMap(@NonNull final GoogleMap map) {
        return Observable.just(map);
    }
}
