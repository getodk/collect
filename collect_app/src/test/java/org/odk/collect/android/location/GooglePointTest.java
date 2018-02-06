package org.odk.collect.android.location;

import android.content.Context;
import android.location.Location;
import android.os.Bundle;

import com.google.android.gms.maps.model.LatLng;
import com.google.common.base.Optional;
import com.jakewharton.rxrelay2.BehaviorRelay;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.odk.collect.android.location.viewmodel.LocationFormatter;
import org.odk.collect.android.location.viewmodel.WatchPosition;
import org.robolectric.RobolectricTestRunner;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

import static android.view.View.GONE;
import static java.lang.Math.random;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.location.GeoViewModel.MAP_FUNCTION;
import static org.odk.collect.android.location.GeoViewModel.MAP_TYPE;
import static org.odk.collect.android.location.model.MapFunction.POINT;
import static org.odk.collect.android.location.model.MapType.GOOGLE;
import static org.odk.collect.android.widgets.GeoPointWidget.DRAGGABLE_ONLY;
import static org.odk.collect.android.widgets.GeoPointWidget.LOCATION;
import static org.odk.collect.android.widgets.GeoPointWidget.READ_ONLY;

@RunWith(RobolectricTestRunner.class)
public class GooglePointTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    private final FailOnEventObserver failOnEventObserver = new FailOnEventObserver();

    @Mock
    private
    Context context;

    @Mock
    private
    WatchPosition watchPosition;

    @Mock
    private
    LocationFormatter locationFormatter;

    private GeoViewModel geoViewModel;

    // These are for manually controlling WatchPosition:
    private BehaviorRelay<Optional<Location>> locationRelay;
    private BehaviorRelay<Boolean> isLocationAvailable;

    @Before
    public void createViewModel() {
        locationRelay = BehaviorRelay.createDefault(Optional.absent());
        isLocationAvailable = BehaviorRelay.create();

        when(watchPosition.observeLocation())
                .thenReturn(locationRelay.hide());

        when(watchPosition.currentLocation())
                .thenReturn(locationRelay.hide().first(Optional.absent()));

        when(watchPosition.observeAvailability())
                .thenReturn(isLocationAvailable.hide());

        geoViewModel = new GeoViewModel(context, watchPosition, locationFormatter);
    }

    // State:
    @Test
    public void viewModelShouldHaveCorrectInitialStateForReadOnlyWithoutLocation() {
        startViewModel(true, false, null);

        thisStateShouldBeConsistentAcrossReadOnly();
    }

    @Test
    public void viewModelShouldHaveCorrectInitialStateForReadOnlyWithLocation() {
        double[] location = randomLocation();
        startViewModel(true, false, location);

        thisStateShouldBeConsistentAcrossReadOnly();
    }

    @Test
    public void viewModelShouldHaveCorrectInitialStateForReadOnlyWithoutLocationAndWithDraggable() {
        startViewModel(true, true, null);

        thisStateShouldBeConsistentAcrossReadOnly();
    }

    @Test
    public void viewModelShouldHaveCorrectInitialStateForReadOnlyWithLocationAndWithDraggable() {
        double[] location = randomLocation();
        startViewModel(true, true, location);

        thisStateShouldBeConsistentAcrossReadOnly();
    }

    // Behavior:
    @Test
    public void viewModelShouldHaveProperBehaviorWithReadOnlyWithoutLocation() {
        startViewModel(true, false, null);

        thisBehaviorShouldBeConsistentAcrossReadOnly();
        thisBehaviorShouldBeConsistentWithoutInitialLocation();
    }

    @Test
    public void viewModelShouldHaveProperBehaviorWithReadOnlyWithLocation() {
        double[] location = randomLocation();
        startViewModel(true, false, location);

        thisBehaviorShouldBeConsistentAcrossReadOnly();
        thisBehaviorShouldBeConsistentWithInitialLocation(location);
    }

    @Test
    public void viewModelShouldHaveProperBehaviorWithReadOnlyWithoutLocationWithDraggable() {
        startViewModel(true, true, null);

        thisBehaviorShouldBeConsistentAcrossReadOnly();
        thisBehaviorShouldBeConsistentWithoutInitialLocation();
    }

    @Test
    public void viewModelShouldHaveProperBehaviorWithReadOnlyWithLocationWithDraggable() {
        double[] location = randomLocation();
        startViewModel(true, true, location);

        thisBehaviorShouldBeConsistentAcrossReadOnly();
        thisBehaviorShouldBeConsistentWithInitialLocation(location);
    }

    private void thisStateShouldBeConsistentAcrossReadOnly() {

        // We don't care about the actual text values, just that they're not visible.
        int infoVisibility = geoViewModel.locationInfoVisibility().
                blockingFirst();
        assertEquals(infoVisibility, GONE);

        int statusVisibility = geoViewModel.locationStatusVisibility()
                .blockingFirst();
        assertEquals(statusVisibility, GONE);

        // Pause should be invisible for all Point modes:
        int pauseVisibility = geoViewModel.pauseButtonVisibility().blockingFirst();
        assertEquals(pauseVisibility, GONE);

        // Check button states:
        boolean isAddEnabled = geoViewModel.isAddLocationEnabled().blockingFirst();
        assertFalse(isAddEnabled);

        boolean isShowEnabled = geoViewModel.isShowLocationEnabled().blockingFirst();
        assertFalse(isShowEnabled);

        boolean isClearEnabled = geoViewModel.isClearLocationEnabled().blockingFirst();
        assertFalse(isClearEnabled);

        // Add Location shouldn't do anything in Read Only:
        boolean isDraggable = geoViewModel.isDraggable()
                .blockingFirst();
        assertFalse(isDraggable);
    }

    private void thisBehaviorShouldBeConsistentAcrossReadOnly() {
        geoViewModel.onLocationCleared()
                .subscribe(failOnEventObserver);

        geoViewModel.addLocation()
                .subscribe(this::shouldComplete, this::onError);

        geoViewModel.pause()
                .subscribe(this::shouldComplete, this::onError);

        geoViewModel.clearLocation()
                .subscribe(this::shouldComplete, this::onError);

        geoViewModel.selectLocation(new LatLng(0, 0))
                .subscribe(this::shouldComplete, this::onError);

        geoViewModel.clearSelectedLocation()
                .subscribe(this::shouldComplete, this::onError);

        String save = geoViewModel.saveLocation()
                .blockingGet();

        assertEquals(save, "");

        // Show Layers should not trigger an event if we have no current location.
        OnNextCounter showLayersCounter = new OnNextCounter();

        geoViewModel.onShowLayers()
                .subscribe(showLayersCounter);

        assertEquals(showLayersCounter.currentCount, 0);

        geoViewModel.showLayers()
                .subscribe(this::shouldComplete, this::onError);

        assertEquals(showLayersCounter.currentCount, 1);

        Location location = mock(Location.class);

        double i = random();
        double j = random();

        when(location.getLatitude()).thenReturn(i);
        when(location.getLongitude()).thenReturn(j);

        Disposable failOnZoom = geoViewModel.onShowZoomDialog()
                .subscribe(this::failOnEvent, this::onError);

        locationRelay.accept(Optional.of(location));
        failOnZoom.dispose();

        OnNextCounter showLocationCounter = new OnNextCounter();

        geoViewModel.onShowZoomDialog()
                .doOnNext(zoomData -> {
                    LatLng currentLocation = zoomData.getCurrentLocation();

                    assertNotNull(currentLocation);
                    assertEquals(i, currentLocation.latitude, 0.000000001);
                    assertEquals(j, currentLocation.longitude, 0.000000001);

                    assertNull(zoomData.getMarkedLocation());
                })
                .subscribe(showLocationCounter);

        assertEquals(showLocationCounter.currentCount, 0);
        geoViewModel.showLocation()
                .subscribe(this::shouldComplete, this::onError);

        assertEquals(showLocationCounter.currentCount, 1);

        // Test GPS alert:
        OnNextCounter showGpsAlertCounter = new OnNextCounter();
        geoViewModel.onShowGpsAlert()
                .subscribe(showGpsAlertCounter);

        assertEquals(showGpsAlertCounter.currentCount, 0);
        isLocationAvailable.accept(true);

        assertEquals(showGpsAlertCounter.currentCount, 0);
        isLocationAvailable.accept(false);

        assertEquals(showGpsAlertCounter.currentCount, 1);

    }

    private void thisBehaviorShouldBeConsistentWithInitialLocation(double[] initialLocation) {
        double i = initialLocation[0];
        double j = initialLocation[1];

        OnNextCounter locationAddedCounter = new OnNextCounter();
        assertEquals(locationAddedCounter.currentCount, 0);
        geoViewModel.onLocationAdded()
                .doOnNext(latLng -> {
                    assertEquals(i, latLng.latitude, 0.000000001);
                    assertEquals(j, latLng.longitude, 0.000000001);
                })
                .subscribe(locationAddedCounter);

        assertEquals(locationAddedCounter.currentCount, 1);

        OnNextCounter initialLocationCounter = new OnNextCounter();
        assertEquals(initialLocationCounter.currentCount, 0);
        geoViewModel.onInitialLocation()
                .doOnNext(latLng -> {
                    assertEquals(i, latLng.latitude, 0.000000001);
                    assertEquals(j, latLng.longitude, 0.000000001);
                })
                .subscribe(initialLocationCounter);
        assertEquals(initialLocationCounter.currentCount, 1);
    }

    private void thisBehaviorShouldBeConsistentWithoutInitialLocation() {
        geoViewModel.onInitialLocation()
                .subscribe(failOnEventObserver);

        geoViewModel.onLocationAdded()
                .subscribe(failOnEventObserver);

        geoViewModel.onShowLayers()
                .subscribe(failOnEventObserver);
    }

    private void startViewModel(boolean isReadOnly, boolean isDraggable, double[] location) {
        Bundle bundle = new Bundle();

        bundle.putSerializable(MAP_TYPE, GOOGLE);
        bundle.putSerializable(MAP_FUNCTION, POINT);

        bundle.putBoolean(READ_ONLY, isReadOnly);
        bundle.putBoolean(DRAGGABLE_ONLY, isDraggable);
        bundle.putDoubleArray(LOCATION, location);

        geoViewModel.onCreate(bundle);
    }

    @SuppressWarnings("unused")
    private void shouldComplete(Object... __) {
        System.out.println("Completed.");
    }

    private void failOnEvent(Object... objects) {
        fail(String.format("Shouldn't have emitted objects: %s", objects));
    }

    private void onError(Throwable e) {
        fail(e.getMessage());
    }

    private double[] randomLocation() {
        return new double[] {random(), random()};
    }

    public static class OnNextCounter implements Observer<Object> {
        private int currentCount = 0;

        @Override
        public void onSubscribe(Disposable d) {

        }

        @Override
        public void onNext(Object o) {
            currentCount++;
        }

        @Override
        public void onError(Throwable e) {
            fail(String.format("OnNextCounter received error: %s", e.getMessage()));
        }

        @Override
        public void onComplete() {

        }
    }

    public static class FailOnEventObserver implements Observer<Object> {
        @Override
        public void onSubscribe(Disposable d) {
            System.out.println("FailOnEventObserver subscribed to.");
        }

        @Override
        public void onNext(Object o) {
            fail("Shouldn't have received object.");
        }

        @Override
        public void onError(Throwable e) {
            fail(e.getMessage());
        }

        @Override
        public void onComplete() {
            System.out.println("FailOnEventObserver completed.");
        }
    }
}