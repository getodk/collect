package org.odk.collect.android.location.domain.actions;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.odk.collect.android.location.domain.state.SelectedLocation;

import io.reactivex.Completable;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ClearLocationTest {

    @Mock
    SelectedLocation selectedLocation;

    @Test
    public void shouldNotClearWhenReadOnly() {
        ClearLocation clearLocation = new ClearLocation(selectedLocation, true);
        clearLocation.clear().subscribe();

        verify(selectedLocation, never()).select(any());
    }

    @Test
    public void shouldClearWhenPossible() {
        when(selectedLocation.select(null))
                .thenReturn(Completable.complete());

        ClearLocation clearLocation = new ClearLocation(selectedLocation, false);
        clearLocation.clear().subscribe();

        verify(selectedLocation, times(1))
                .select(null);
    }
}