package org.odk.collect.android.widgets;

import android.app.Application;
import android.content.Intent;
import android.support.annotation.NonNull;

import net.bytebuddy.utility.RandomString;

import org.javarosa.core.model.data.StringData;
import org.junit.Test;
import org.mockito.Mock;
import org.odk.collect.android.TestCollect;
import org.odk.collect.android.external.ExternalAppsUtil;
import org.odk.collect.android.utilities.ActivityAvailability;
import org.odk.collect.android.widgets.base.GeneralExStringWidgetTest;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.shadows.ShadowContextImpl;
import org.robolectric.shadows.ShadowIntent;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

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
        return new ExStringWidget(RuntimeEnvironment.application, formEntryPrompt, externalAppsUtil);
    }

    @NonNull
    @Override
    public StringData getNextAnswer() {
        return new StringData(RandomString.make());
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();

        TestCollect application = (TestCollect) RuntimeEnvironment.application;
        application.setActivityAvailability(activityAvailability);

        when(formEntryPrompt.getAppearanceHint()).thenReturn("");
    }

    @Test
    public void whenLaunchIntentButtonIsClickedExternalActivityShouldBeLaunched() {
        when(activityAvailability.isActivityAvailable(any(Intent.class)))
                .thenReturn(true);

        String intentName = RandomString.make();
        when(externalAppsUtil.extractIntentName(any(String.class)))
                .thenReturn(intentName);

        ExStringWidget widget = getWidget();
        widget.getLaunchIntentButton().performClick();

        ShadowApplication shadowApplication = shadowOf(RuntimeEnvironment.application);

        Intent nextStartedActivity = shadowApplication.getNextStartedActivity();
        ShadowIntent shadowIntent = shadowOf(nextStartedActivity);

        assertThat(shadowIntent.getIntentClass().toString(),
                equalTo(intentName));
    }
}
