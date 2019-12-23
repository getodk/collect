package org.odk.collect.android.widgets.base;

import android.view.View;

import org.javarosa.core.model.data.IAnswerData;
import org.junit.Test;
import org.odk.collect.android.R;
import org.odk.collect.android.widgets.BaseGeoWidget;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

public abstract class BaseGeoWidgetTest<W extends BaseGeoWidget, A extends IAnswerData>
        extends BinaryWidgetTest<W, A> {

    @Test
    public void buttonsShouldNotLaunchIntentsWhenPermissionsDenied() {
        stubAllRuntimePermissionsGranted(false);

        assertIntentNotStarted(activity, getIntentLaunchedByClick(R.id.simple_button));
    }

    @Test
    public void usingReadOnlyOptionShouldMakeAllClickableElementsDisabled() {
        when(formEntryPrompt.isReadOnly()).thenReturn(true);

        assertThat(getWidget().startGeoButton.getVisibility(), is(View.GONE));
    }
}
