package org.odk.collect.android.widgets.warnings;

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.odk.collect.android.widgets.warnings.SpacesInUnderlyingValuesWarning.UnderlyingValuesChecker;
import org.odk.collect.android.widgets.warnings.SpacesInUnderlyingValuesWarning.WarningRenderer;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SpacesInUnderlyingValuesWarningTest {

    SpacesInUnderlyingValuesWarning subject;

    @Mock
    UnderlyingValuesChecker checker;

    @Mock
    WarningRenderer renderer;


    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        subject = new SpacesInUnderlyingValuesWarning(checker, renderer);
    }

    @Test
    public void renderWarningWhenHasInvalidValues() {
        when(checker.hasInvalidValues()).thenReturn(true);

        subject.renderWarningIfNecessary(Lists.newArrayList());

        verify(renderer, times(1)).render(any());
    }

    @Test
    public void doesNotRenderWhenNoInvalidValuesDetected() {
        when(checker.hasInvalidValues()).thenReturn(false);

        subject.renderWarningIfNecessary(Lists.newArrayList());

        verify(renderer, never()).render(any());
    }


}