package org.odk.collect.android.location.mapview;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.jakewharton.rxrelay2.PublishRelay;

import org.odk.collect.android.spatial.MapHelper;

import io.reactivex.Completable;
import io.reactivex.Observable;

public class GoogleMapView implements MapView, GoogleMap.OnMapLongClickListener,
        GoogleMap.OnMarkerDragListener {

    @NonNull
    private final Context context;

    @NonNull
    private final GoogleMap googleMap;

    private final boolean isDraggable;

    @Nullable
    private Marker marker = null;

    // Internal state:
    @NonNull
    private final PublishRelay<LatLng> longPressRelay =
            PublishRelay.create();

    @NonNull
    private final PublishRelay<LatLng> markerMovedRelay =
            PublishRelay.create();

    public GoogleMapView(@NonNull Context context,
                         @NonNull GoogleMap googleMap,
                         boolean isDraggable) {
        this.context = context;

        this.googleMap = googleMap;
        this.googleMap.setOnMarkerDragListener(this);
        this.googleMap.setOnMapLongClickListener(this);

        this.isDraggable = isDraggable;
    }

    @NonNull
    @Override
    public Observable<LatLng> observeLongPress() {
        return longPressRelay.hide();
    }

    @NonNull
    @Override
    public Observable<LatLng> observeMarkerMoved() {
        return markerMovedRelay.hide();
    }

    @NonNull
    @Override
    public Completable markLocation(@Nullable LatLng latLng) {
        return Completable.defer(() -> {
            if (marker != null) {
                marker.remove();
                marker = null;
            }

            if (latLng != null) {
                MarkerOptions markerOptions = new MarkerOptions();

                markerOptions.position(latLng);
                markerOptions.draggable(isDraggable);

                marker = googleMap.addMarker(markerOptions);
            }

            return Completable.complete();
        });
    }

    @NonNull
    @Override
    public Completable zoomToLocation(@NonNull LatLng latLng) {
        return Completable.defer(() -> {
            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(latLng, 16.0f);
            googleMap.animateCamera(update);

            return Completable.complete();
        });
    }

    @NonNull
    @Override
    public Completable showLayers() {
        return Completable.defer(() -> {
            new MapHelper(context, googleMap)
                    .showLayersDialog(context);

            return Completable.complete();
        });
    }

    @Override
    public void onMapLongClick(LatLng latLng) {
        if (isDraggable) {
            longPressRelay.accept(latLng);
        }
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        markerMovedRelay.accept(marker.getPosition());
    }

    @Override
    public void onMarkerDragStart(Marker marker) {
        // Do nothing.
    }

    @Override
    public void onMarkerDrag(Marker marker) {
        // Do nothing.
    }
}
