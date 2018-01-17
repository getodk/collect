package org.odk.collect.android.location.usecases;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.google.common.base.Optional;
import com.jakewharton.rxrelay2.BehaviorRelay;

import org.odk.collect.android.injection.config.scopes.PerApplication;
import org.odk.collect.android.location.model.MapFunction;
import org.odk.collect.android.location.model.MapType;

import javax.inject.Inject;

import io.reactivex.Observable;

import static org.odk.collect.android.widgets.GeoPointWidget.DRAGGABLE_ONLY;
import static org.odk.collect.android.widgets.GeoPointWidget.LOCATION;
import static org.odk.collect.android.widgets.GeoPointWidget.READ_ONLY;

@PerApplication
public class InitialState {

    public static final String MAP_TYPE = "map_type";
    public static final String MAP_FUNCTION = "map_function";

    @NonNull
    private final BehaviorRelay<Boolean> isReadOnlyRelay = BehaviorRelay.create();

    @NonNull
    private final BehaviorRelay<Boolean> isDraggableRelay = BehaviorRelay.create();

    @NonNull
    private final BehaviorRelay<Optional<LatLng>> locationRelay = BehaviorRelay.create();

    @NonNull
    private final BehaviorRelay<MapType> typeRelay = BehaviorRelay.create();

    @NonNull
    private final BehaviorRelay<MapFunction> functionRelay = BehaviorRelay.create();

    @Inject
    InitialState() {

    }

    public void set(@Nullable Bundle bundle) {
        bundle = bundle != null
                ? bundle
                : Bundle.EMPTY;

        isReadOnlyRelay.accept(bundle.getBoolean(READ_ONLY, false));
        isDraggableRelay.accept(bundle.getBoolean(DRAGGABLE_ONLY, false));

        double[] location = bundle.getDoubleArray(LOCATION);
        LatLng latLng = location != null
                ? new LatLng(location[0], location[1])
                : null;

        locationRelay.accept(Optional.fromNullable(latLng));

        MapType mapType = (MapType) bundle.get(MAP_TYPE);
        typeRelay.accept(mapType != null
                ? mapType
                : MapType.GOOGLE);


        MapFunction mapFunction = (MapFunction) bundle.get(MAP_FUNCTION);
        functionRelay.accept(mapFunction != null
                ? mapFunction
                : MapFunction.TRACE);
    }

    @NonNull
    public Observable<Boolean> isReadOnly() {
        return isReadOnlyRelay.hide();
    }

    @NonNull
    public Observable<Boolean> isDraggable() {
        return isDraggableRelay.hide();
    }

    public Observable<Optional<LatLng>> location() {
        return locationRelay.hide();
    }
}
