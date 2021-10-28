package org.odk.collect.android.widgets.utilities;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.utilities.ApplicationConstants.RequestCodes.GEOSHAPE_CAPTURE;
import static org.odk.collect.android.utilities.ApplicationConstants.RequestCodes.GEOTRACE_CAPTURE;
import static org.odk.collect.android.utilities.ApplicationConstants.RequestCodes.LOCATION_CAPTURE;
import static org.odk.collect.android.widgets.support.GeoWidgetHelpers.assertGeoPointBundleArgumentEquals;
import static org.odk.collect.android.widgets.support.GeoWidgetHelpers.assertGeoPolyBundleArgumentEquals;
import static org.odk.collect.android.widgets.support.GeoWidgetHelpers.getRandomDoubleArray;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithAnswer;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.widgetTestActivity;
import static org.odk.collect.geo.Constants.EXTRA_RETAIN_MOCK_ACCURACY;
import static org.robolectric.Shadows.shadowOf;
import static java.util.Arrays.asList;

import android.content.ComponentName;
import android.content.Intent;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.GeoPointData;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.form.api.FormEntryPrompt;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.geo.GeoPolyActivity;
import org.odk.collect.android.fakes.FakePermissionsProvider;
import org.odk.collect.android.support.WidgetTestActivity;
import org.odk.collect.android.utilities.Appearances;
import org.odk.collect.android.widgets.support.FakeWaitingForDataRegistry;
import org.odk.collect.geo.GeoPointActivity;
import org.odk.collect.geo.GeoPointMapActivity;
import org.robolectric.shadows.ShadowActivity;

@RunWith(AndroidJUnit4.class)
public class ActivityGeoDataRequesterTest {
    private final FakePermissionsProvider permissionsProvider = new FakePermissionsProvider();
    private final ActivityGeoDataRequester activityGeoDataRequester = new ActivityGeoDataRequester(permissionsProvider);
    private final FakeWaitingForDataRegistry waitingForDataRegistry = new FakeWaitingForDataRegistry();
    private final GeoPointData answer = new GeoPointData(getRandomDoubleArray());

    private WidgetTestActivity testActivity;
    private ShadowActivity shadowActivity;
    private FormEntryPrompt prompt;
    private FormIndex formIndex;
    private QuestionDef questionDef;

    @Before
    public void setUp() {
        testActivity = widgetTestActivity();
        shadowActivity = shadowOf(testActivity);

        prompt = promptWithAnswer(null);
        formIndex = mock(FormIndex.class);
        questionDef = mock(QuestionDef.class);

        permissionsProvider.setPermissionGranted(true);
        when(prompt.getQuestion()).thenReturn(questionDef);
        when(prompt.getIndex()).thenReturn(formIndex);
    }

    @Test
    public void whenPermissionIsNotGranted_requestGeoPoint_doesNotLaunchAnyIntent() {
        permissionsProvider.setPermissionGranted(false);
        activityGeoDataRequester.requestGeoPoint(testActivity, prompt, "", waitingForDataRegistry);

        assertNull(shadowActivity.getNextStartedActivity());
        assertTrue(waitingForDataRegistry.waiting.isEmpty());
    }

    @Test
    public void whenPermissionIsNotGranted_requestGeoShape_doesNotLaunchAnyIntent() {
        permissionsProvider.setPermissionGranted(false);
        activityGeoDataRequester.requestGeoShape(testActivity, prompt, "", waitingForDataRegistry);

        assertNull(shadowActivity.getNextStartedActivity());
        assertTrue(waitingForDataRegistry.waiting.isEmpty());
    }

    @Test
    public void whenPermissionIsNotGranted_requestGeoTrace_doesNotLaunchAnyIntent() {
        permissionsProvider.setPermissionGranted(false);
        activityGeoDataRequester.requestGeoTrace(testActivity, prompt, "", waitingForDataRegistry);

        assertNull(shadowActivity.getNextStartedActivity());
        assertTrue(waitingForDataRegistry.waiting.isEmpty());
    }

    @Test
    public void whenPermissionIGranted_requestGeoPoint_setsFormIndexWaitingForData() {
        activityGeoDataRequester.requestGeoPoint(testActivity, prompt, "", waitingForDataRegistry);
        assertTrue(waitingForDataRegistry.waiting.contains(formIndex));
    }

    @Test
    public void whenPermissionIGranted_requestGeoShape_setsFormIndexWaitingForData() {
        activityGeoDataRequester.requestGeoShape(testActivity, prompt, "", waitingForDataRegistry);
        assertTrue(waitingForDataRegistry.waiting.contains(formIndex));
    }

    @Test
    public void whenPermissionIGranted_requestGeoTrace_setsFormIndexWaitingForData() {
        activityGeoDataRequester.requestGeoTrace(testActivity, prompt, "", waitingForDataRegistry);
        assertTrue(waitingForDataRegistry.waiting.contains(formIndex));
    }

    @Test
    public void whenWidgetIsReadOnly_requestGeoPoint_launchesCorrectIntent() {
        when(prompt.isReadOnly()).thenReturn(true);
        activityGeoDataRequester.requestGeoPoint(testActivity, prompt, "", waitingForDataRegistry);
        Intent startedIntent = shadowActivity.getNextStartedActivity();

        assertEquals(startedIntent.getComponent(), new ComponentName(testActivity, GeoPointActivity.class));
        assertGeoPointBundleArgumentEquals(startedIntent.getExtras(), null, GeoWidgetUtils.DEFAULT_LOCATION_ACCURACY,
                true, false);

        assertEquals(shadowActivity.getNextStartedActivityForResult().requestCode, LOCATION_CAPTURE);
    }

    @Test
    public void whenWidgetHasAnswerAndAccuracyValue_requestGeoPoint_launchesCorrectIntent() {
        when(questionDef.getAdditionalAttribute(null, "accuracyThreshold")).thenReturn("10");

        activityGeoDataRequester.requestGeoPoint(testActivity, prompt, answer.getDisplayText(), waitingForDataRegistry);
        Intent startedIntent = shadowActivity.getNextStartedActivity();

        assertEquals(startedIntent.getComponent(), new ComponentName(testActivity, GeoPointActivity.class));
        assertGeoPointBundleArgumentEquals(startedIntent.getExtras(), GeoWidgetUtils.getLocationParamsFromStringAnswer(
                answer.getDisplayText()), 10.0, false, false);

        assertEquals(shadowActivity.getNextStartedActivityForResult().requestCode, LOCATION_CAPTURE);
    }

    @Test
    public void whenWidgetHasMapsAppearance_requestGeoPoint_launchesCorrectIntent() {
        when(prompt.getAppearanceHint()).thenReturn(Appearances.MAPS);

        activityGeoDataRequester.requestGeoPoint(testActivity, prompt, "", waitingForDataRegistry);
        Intent startedIntent = shadowActivity.getNextStartedActivity();

        assertEquals(startedIntent.getComponent(), new ComponentName(testActivity, GeoPointMapActivity.class));
        assertGeoPointBundleArgumentEquals(startedIntent.getExtras(), null, GeoWidgetUtils.DEFAULT_LOCATION_ACCURACY,
                false, false);

        assertEquals(shadowActivity.getNextStartedActivityForResult().requestCode, LOCATION_CAPTURE);
    }

    @Test
    public void whenWidgetHasPlacementMapAppearance_requestGeoPoint_launchesCorrectIntent() {
        when(prompt.getAppearanceHint()).thenReturn(Appearances.PLACEMENT_MAP);

        activityGeoDataRequester.requestGeoPoint(testActivity, prompt, "", waitingForDataRegistry);
        Intent startedIntent = shadowActivity.getNextStartedActivity();

        assertEquals(startedIntent.getComponent(), new ComponentName(testActivity, GeoPointMapActivity.class));
        assertGeoPointBundleArgumentEquals(startedIntent.getExtras(), null, GeoWidgetUtils.DEFAULT_LOCATION_ACCURACY,
                false, true);

        assertEquals(shadowActivity.getNextStartedActivityForResult().requestCode, LOCATION_CAPTURE);
    }

    @Test
    public void whenWidgetIsReadOnly_requestGeoShape_launchesCorrectIntent() {
        when(prompt.isReadOnly()).thenReturn(true);
        activityGeoDataRequester.requestGeoShape(testActivity, prompt, "", waitingForDataRegistry);
        Intent startedIntent = shadowActivity.getNextStartedActivity();

        assertEquals(startedIntent.getComponent(), new ComponentName(testActivity, GeoPolyActivity.class));
        assertGeoPolyBundleArgumentEquals(startedIntent.getExtras(), "", GeoPolyActivity.OutputMode.GEOSHAPE, true);

        assertEquals(shadowActivity.getNextStartedActivityForResult().requestCode, GEOSHAPE_CAPTURE);
    }

    @Test
    public void whenWidgetHasAnswerAndAccuracyValue_requestGeoShape_launchesCorrectIntent() {
        when(questionDef.getAdditionalAttribute(null, "accuracyThreshold")).thenReturn("10");

        activityGeoDataRequester.requestGeoShape(testActivity, prompt, "blah", waitingForDataRegistry);
        Intent startedIntent = shadowActivity.getNextStartedActivity();

        assertEquals(startedIntent.getComponent(), new ComponentName(testActivity, GeoPolyActivity.class));
        assertGeoPolyBundleArgumentEquals(startedIntent.getExtras(), "blah", GeoPolyActivity.OutputMode.GEOSHAPE, false);

        assertEquals(shadowActivity.getNextStartedActivityForResult().requestCode, GEOSHAPE_CAPTURE);
    }

    @Test
    public void whenWidgetIsReadOnly_requestGeoTrace_launchesCorrectIntent() {
        when(prompt.isReadOnly()).thenReturn(true);
        activityGeoDataRequester.requestGeoTrace(testActivity, prompt, "", waitingForDataRegistry);
        Intent startedIntent = shadowActivity.getNextStartedActivity();

        assertEquals(startedIntent.getComponent(), new ComponentName(testActivity, GeoPolyActivity.class));
        assertGeoPolyBundleArgumentEquals(startedIntent.getExtras(), "", GeoPolyActivity.OutputMode.GEOTRACE, true);

        assertEquals(shadowActivity.getNextStartedActivityForResult().requestCode, GEOTRACE_CAPTURE);
    }

    @Test
    public void whenWidgetHasAnswerAndAccuracyValue_requestGeoTrace_launchesCorrectIntent() {
        when(questionDef.getAdditionalAttribute(null, "accuracyThreshold")).thenReturn("10");

        activityGeoDataRequester.requestGeoTrace(testActivity, prompt, "blah", waitingForDataRegistry);
        Intent startedIntent = shadowActivity.getNextStartedActivity();

        assertEquals(startedIntent.getComponent(), new ComponentName(testActivity, GeoPolyActivity.class));
        assertGeoPolyBundleArgumentEquals(startedIntent.getExtras(), "blah", GeoPolyActivity.OutputMode.GEOTRACE, false);

        assertEquals(shadowActivity.getNextStartedActivityForResult().requestCode, GEOTRACE_CAPTURE);
    }

    @Test
    public void requestGeoPoint_whenWidgetHasAllowMockAccuracy_addsItToIntent() {
        when(prompt.getBindAttributes())
                .thenReturn(asList(TreeElement.constructAttributeElement("odk", "allow-mock-accuracy", "true")));

        activityGeoDataRequester.requestGeoPoint(testActivity, prompt, "blah", waitingForDataRegistry);

        Intent startedIntent = shadowActivity.getNextStartedActivity();
        assertEquals(startedIntent.getComponent(), new ComponentName(testActivity, GeoPointActivity.class));
        assertTrue(startedIntent.getBooleanExtra(EXTRA_RETAIN_MOCK_ACCURACY, false));

        when(prompt.getBindAttributes())
                .thenReturn(asList(TreeElement.constructAttributeElement("odk", "allow-mock-accuracy", "false")));

        activityGeoDataRequester.requestGeoPoint(testActivity, prompt, "blah", waitingForDataRegistry);

        startedIntent = shadowActivity.getNextStartedActivity();
        assertEquals(startedIntent.getComponent(), new ComponentName(testActivity, GeoPointActivity.class));
        assertFalse(startedIntent.getBooleanExtra(EXTRA_RETAIN_MOCK_ACCURACY, true));
    }

    @Test
    public void requestGeoShape_whenWidgetHasAllowMockAccuracy_addsItToIntent() {
        when(prompt.getBindAttributes())
                .thenReturn(asList(TreeElement.constructAttributeElement("odk", "allow-mock-accuracy", "true")));

        activityGeoDataRequester.requestGeoShape(testActivity, prompt, "blah", waitingForDataRegistry);

        Intent startedIntent = shadowActivity.getNextStartedActivity();
        assertEquals(startedIntent.getComponent(), new ComponentName(testActivity, GeoPolyActivity.class));
        assertTrue(startedIntent.getBooleanExtra(EXTRA_RETAIN_MOCK_ACCURACY, false));

        when(prompt.getBindAttributes())
                .thenReturn(asList(TreeElement.constructAttributeElement("odk", "allow-mock-accuracy", "false")));

        activityGeoDataRequester.requestGeoShape(testActivity, prompt, "blah", waitingForDataRegistry);

        startedIntent = shadowActivity.getNextStartedActivity();
        assertEquals(startedIntent.getComponent(), new ComponentName(testActivity, GeoPolyActivity.class));
        assertFalse(startedIntent.getBooleanExtra(EXTRA_RETAIN_MOCK_ACCURACY, true));
    }

    @Test
    public void requestGeoTrace_whenWidgetHasAllowMockAccuracy_addsItToIntent() {
        when(prompt.getBindAttributes())
                .thenReturn(asList(TreeElement.constructAttributeElement("odk", "allow-mock-accuracy", "true")));

        activityGeoDataRequester.requestGeoTrace(testActivity, prompt, "blah", waitingForDataRegistry);

        Intent startedIntent = shadowActivity.getNextStartedActivity();
        assertEquals(startedIntent.getComponent(), new ComponentName(testActivity, GeoPolyActivity.class));
        assertTrue(startedIntent.getBooleanExtra(EXTRA_RETAIN_MOCK_ACCURACY, false));

        when(prompt.getBindAttributes())
                .thenReturn(asList(TreeElement.constructAttributeElement("odk", "allow-mock-accuracy", "false")));

        activityGeoDataRequester.requestGeoTrace(testActivity, prompt, "blah", waitingForDataRegistry);

        startedIntent = shadowActivity.getNextStartedActivity();
        assertEquals(startedIntent.getComponent(), new ComponentName(testActivity, GeoPolyActivity.class));
        assertFalse(startedIntent.getBooleanExtra(EXTRA_RETAIN_MOCK_ACCURACY, true));
    }
}
