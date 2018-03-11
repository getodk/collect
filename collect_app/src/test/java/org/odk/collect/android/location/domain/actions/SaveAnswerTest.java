package org.odk.collect.android.location.domain.actions;

import android.app.Activity;
import android.content.Intent;

import com.google.android.gms.maps.model.LatLng;
import com.google.common.base.Optional;

import net.bytebuddy.utility.RandomString;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.database.ActivityLogger;
import org.odk.collect.android.location.domain.state.SelectedLocation;

import javax.inject.Inject;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.activities.FormEntryActivity.LOCATION_RESULT;
import static org.odk.collect.android.location.TestUtility.randomLatLng;
import static org.odk.collect.android.location.domain.actions.SaveAnswer.SAVE_ACTION;
import static org.odk.collect.android.location.domain.actions.SaveAnswer.SAVE_CONTEXT;

@RunWith(MockitoJUnitRunner.class)
public class SaveAnswerTest {
    @Mock
    SelectedLocation selectedLocation;

    @Mock
    SetActivityResult setActivityResult;

    @InjectMocks
    SaveAnswer saveAnswer;

    @Before
    public void prepareMocks() {
    }

    @Test
    public void shouldSaveEmptyStringWhenSelectedLocationIsNull() {
        when(selectedLocation.get())
                .thenReturn(Single.just(Optional.absent()));

        when(setActivityResult.setAnswerForKey(any(), any(), any(), any()))
                .thenReturn(Completable.complete());

        saveAnswer.save().subscribe();

        verify(setActivityResult, times(1))
                .setAnswerForKey(LOCATION_RESULT, "", SAVE_CONTEXT, SAVE_ACTION);
    }

    @Test
    public void shouldSaveProperlyFormattedAnswerWhenNotNull() {
        LatLng latLng = randomLatLng();
        when(selectedLocation.get())
                .thenReturn(Single.just(Optional.of(latLng)));

        when(setActivityResult.setAnswerForKey(any(), any(), any(), any()))
                .thenReturn(Completable.complete());

        saveAnswer.save().subscribe();

        String answer = String.format("%s %s 0 0",
                latLng.latitude,
                latLng.longitude);

        verify(setActivityResult, times(1))
                .setAnswerForKey(LOCATION_RESULT, answer, SAVE_CONTEXT, SAVE_ACTION);
    }
}