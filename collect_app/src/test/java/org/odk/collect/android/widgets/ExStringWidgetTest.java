package org.odk.collect.android.widgets;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.widget.Button;
import android.widget.EditText;

import net.bytebuddy.utility.RandomString;

import org.javarosa.core.model.data.StringData;
import org.junit.Test;
import org.mockito.Mock;
import org.odk.collect.android.external.ExternalAppsUtil;
import org.odk.collect.android.utilities.ActivityAvailability;
import org.odk.collect.android.widgets.base.GeneralExStringWidgetTest;
import org.robolectric.RuntimeEnvironment;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * @author James Knight
 */

public class ExStringWidgetTest extends GeneralExStringWidgetTest<ExStringWidget, StringData> {

    @Mock
    private ExternalAppsUtil externalAppsUtil;

    @Mock
    private ActivityAvailability activityAvailability;

    @NonNull
    @Override
    public ExStringWidget createWidget() {
        return new ExStringWidget(RuntimeEnvironment.application, formEntryPrompt,
                activityAvailability, externalAppsUtil);
    }

    @NonNull
    @Override
    public StringData getNextAnswer() {
        return new StringData(RandomString.make());
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        when(formEntryPrompt.getAppearanceHint()).thenReturn("");
    }

    @Test
    public void whenLaunchIntentButtonIsPressedAnswerFieldShouldBecomeEnabled() {
        // Get Views:
        ExStringWidget widget = getWidget();
        EditText answer = widget.getAnswerText();
        Button launchIntentButton = widget.getLaunchIntentButton();

        // Test default case:
        assertFalse(answer.isEnabled());
        assertFalse(answer.isFocusable());
        assertFalse(answer.isFocusableInTouchMode());
        assertNull(answer.getBackground());

        assertTrue(launchIntentButton.isEnabled());
        assertTrue(launchIntentButton.isFocusable());

        // Mock our dependencies:
        when(activityAvailability.isActivityAvailable(any(Intent.class)))
                .thenReturn(false);

        // Perform our click:
        widget.getLaunchIntentButton().performClick();

        // Test values changed:
        assertTrue(answer.isEnabled());
        assertTrue(answer.isFocusable());
        assertTrue(answer.isFocusableInTouchMode());
        assertThat(answer.getBackground(), equalTo(widget.getTextBackground()));

        assertFalse(launchIntentButton.isEnabled());
        assertFalse(launchIntentButton.isFocusable());
    }
}
