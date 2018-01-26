package org.odk.collect.android.location.domain.actions;

import android.graphics.Path;
import android.location.Location;

import com.google.android.gms.maps.model.LatLng;
import com.google.common.base.Optional;
import com.jakewharton.rxrelay2.BehaviorRelay;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.odk.collect.android.location.client.LocationClient;
import org.odk.collect.android.location.domain.state.CurrentLocation;
import org.odk.collect.android.location.domain.state.SelectedLocation;

import io.reactivex.Completable;
import io.reactivex.Observable;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.location.TestUtility.locationToLatLng;
import static org.odk.collect.android.location.TestUtility.randomLatLng;
import static org.odk.collect.android.location.TestUtility.randomLocation;

@RunWith(MockitoJUnitRunner.class)
public class AddLocationTest {

    @Mock
    CurrentLocation currentLocation;

    @Mock
    SelectedLocation selectedLocation;

    private BehaviorRelay<Optional<Location>> currentLocationRelay =
            BehaviorRelay.createDefault(Optional.absent());

    @Test
    public void shouldNotObserveOrSelectWhenReadOnly() throws Exception {
        AddLocation addLocation = new AddLocation(currentLocation, selectedLocation, true);
        addLocation.add().subscribe();

        verify(currentLocation, never()).observe();
        verify(selectedLocation, never()).select(any());
    }

    @Test
    public void shouldNotSelectWhenPositionIsNotPresent() {
        when(currentLocation.observe())
                .thenReturn(currentLocationRelay.hide());

        AddLocation addLocation = new AddLocation(currentLocation, selectedLocation, false);
        addLocation.add().subscribe();

        verify(currentLocation, times(1)).observe();
        verify(selectedLocation, never()).select(any());
    }

    @Test
    public void shouldSelectWhenPositionIsPresent() {
        when(currentLocation.observe())
                .thenReturn(currentLocationRelay.hide());

        when(selectedLocation.select(any()))
                .thenReturn(Completable.complete());

        AddLocation addLocation = new AddLocation(currentLocation, selectedLocation, false);
        addLocation.add().subscribe();

        Location location = randomLocation();
        currentLocationRelay.accept(Optional.of(location));

        addLocation.add().subscribe();

        LatLng latLng = locationToLatLng(location);
        verify(currentLocation, times(2)).observe();
        verify(selectedLocation, times(1)).select(latLng);
    }
}