package org.odk.collect.android.widgets.utilities;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.utilities.ApplicationConstants.RequestCodes.GEOSHAPE_CAPTURE;
import static org.odk.collect.android.utilities.ApplicationConstants.RequestCodes.GEOTRACE_CAPTURE;
import static org.odk.collect.android.utilities.ApplicationConstants.RequestCodes.LOCATION_CAPTURE;
import static org.odk.collect.android.widgets.support.GeoWidgetHelpers.getRandomDoubleArray;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithAnswer;
import static org.odk.collect.geo.Constants.EXTRA_DRAGGABLE_ONLY;
import static org.odk.collect.geo.Constants.EXTRA_READ_ONLY;
import static org.odk.collect.geo.Constants.EXTRA_RETAIN_MOCK_ACCURACY;
import static org.robolectric.Shadows.shadowOf;
import static java.util.Arrays.asList;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.GeoPointData;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.form.api.FormEntryPrompt;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.fakes.FakePermissionsProvider;
import org.odk.collect.android.utilities.Appearances;
import org.odk.collect.android.widgets.support.FakeWaitingForDataRegistry;
import org.odk.collect.geo.GeoPointActivity;
import org.odk.collect.geo.GeoPointMapActivity;
import org.odk.collect.geo.GeoPolyActivity;
import org.robolectric.Robolectric;
import org.robolectric.shadows.ShadowActivity;

@RunWith(AndroidJUnit4.class)
public class ActivityGeoDataRequesterTest {

    private final FakePermissionsProvider permissionsProvider = new FakePermissionsProvider();
    private final FakeWaitingForDataRegistry waitingForDataRegistry = new FakeWaitingForDataRegistry();
    private final GeoPointData answer = new GeoPointData(getRandomDoubleArray());

    private Activity testActivity;
    private ShadowActivity shadowActivity;
    private FormEntryPrompt prompt;
    private FormIndex formIndex;
    private QuestionDef questionDef;

    private ActivityGeoDataRequester activityGeoDataRequester;

    @Before
    public void setUp() {
        testActivity = Robolectric.buildActivity(Activity.class).get();
        shadowActivity = shadowOf(testActivity);

        prompt = promptWithAnswer(null);
        formIndex = mock(FormIndex.class);
        questionDef = mock(QuestionDef.class);

        permissionsProvider.setPermissionGranted(true);
        when(prompt.getQuestion()).thenReturn(questionDef);
        when(prompt.getIndex()).thenReturn(formIndex);

        activityGeoDataRequester = new ActivityGeoDataRequester(permissionsProvider, testActivity);
    }

    @Test
    public void whenPermissionIsNotGranted_requestGeoPoint_doesNotLaunchAnyIntent() {
        permissionsProvider.setPermissionGranted(false);
        activityGeoDataRequester.requestGeoPoint(prompt, "", waitingForDataRegistry);

        assertNull(shadowActivity.getNextStartedActivity());
        assertTrue(waitingForDataRegistry.waiting.isEmpty());
    }

    @Test
    public void whenPermissionIsNotGranted_requestGeoShape_doesNotLaunchAnyIntent() {
        permissionsProvider.setPermissionGranted(false);
        activityGeoDataRequester.requestGeoShape(prompt, "", waitingForDataRegistry);

        assertNull(shadowActivity.getNextStartedActivity());
        assertTrue(waitingForDataRegistry.waiting.isEmpty());
    }

    @Test
    public void whenPermissionIsNotGranted_requestGeoTrace_doesNotLaunchAnyIntent() {
        permissionsProvider.setPermissionGranted(false);
        activityGeoDataRequester.requestGeoTrace(prompt, "", waitingForDataRegistry);

        assertNull(shadowActivity.getNextStartedActivity());
        assertTrue(waitingForDataRegistry.waiting.isEmpty());
    }

    @Test
    public void whenPermissionIGranted_requestGeoPoint_setsFormIndexWaitingForData() {
        activityGeoDataRequester.requestGeoPoint(prompt, "", waitingForDataRegistry);
        assertTrue(waitingForDataRegistry.waiting.contains(formIndex));
    }

    @Test
    public void whenPermissionIGranted_requestGeoShape_setsFormIndexWaitingForData() {
        activityGeoDataRequester.requestGeoShape(prompt, "", waitingForDataRegistry);
        assertTrue(waitingForDataRegistry.waiting.contains(formIndex));
    }

    @Test
    public void whenPermissionIGranted_requestGeoTrace_setsFormIndexWaitingForData() {
        activityGeoDataRequester.requestGeoTrace(prompt, "", waitingForDataRegistry);
        assertTrue(waitingForDataRegistry.waiting.contains(formIndex));
    }

    @Test
    public void requestGeoPoint_launchesCorrectIntent() {
        activityGeoDataRequester.requestGeoPoint(prompt, answer.getDisplayText(), waitingForDataRegistry);
        Intent startedIntent = shadowActivity.getNextStartedActivity();

        assertEquals(startedIntent.getComponent(), new ComponentName(testActivity, GeoPointActivity.class));
        assertEquals(shadowActivity.getNextStartedActivityForResult().requestCode, LOCATION_CAPTURE);

        Bundle bundle = startedIntent.getExtras();
        assertThat(bundle.getFloat(GeoPointActivity.EXTRA_ACCURACY_THRESHOLD), equalTo(ActivityGeoDataRequester.DEFAULT_ACCURACY_THRESHOLD));
        assertThat(bundle.getFloat(GeoPointActivity.EXTRA_UNACCEPTABLE_ACCURACY_THRESHOLD), equalTo(ActivityGeoDataRequester.DEFAULT_UNACCEPTABLE_ACCURACY_THRESHOLD));
    }

    @Test
    public void whenWidgetHasAccuracyValue_requestGeoPoint_launchesCorrectIntent() {
        when(questionDef.getAdditionalAttribute(null, "accuracyThreshold")).thenReturn("10");

        activityGeoDataRequester.requestGeoPoint(prompt, answer.getDisplayText(), waitingForDataRegistry);
        Intent startedIntent = shadowActivity.getNextStartedActivity();

        assertEquals(startedIntent.getComponent(), new ComponentName(testActivity, GeoPointActivity.class));
        assertEquals(shadowActivity.getNextStartedActivityForResult().requestCode, LOCATION_CAPTURE);

        Bundle bundle = startedIntent.getExtras();
        assertThat(bundle.getFloat(GeoPointActivity.EXTRA_ACCURACY_THRESHOLD), equalTo(10.0f));
    }

    @Test
    public void whenWidgetHasUnacceptableAccuracyValue_requestGeoPoint_launchesCorrectIntent() {
        when(questionDef.getAdditionalAttribute(null, "unacceptableAccuracyThreshold")).thenReturn("20");

        activityGeoDataRequester.requestGeoPoint(prompt, answer.getDisplayText(), waitingForDataRegistry);
        Intent startedIntent = shadowActivity.getNextStartedActivity();

        assertEquals(startedIntent.getComponent(), new ComponentName(testActivity, GeoPointActivity.class));
        assertEquals(shadowActivity.getNextStartedActivityForResult().requestCode, LOCATION_CAPTURE);

        Bundle bundle = startedIntent.getExtras();
        assertThat(bundle.getFloat(GeoPointActivity.EXTRA_UNACCEPTABLE_ACCURACY_THRESHOLD), equalTo(20.0f));
    }

    @Test
    public void whenWidgetHasMapsAppearance_requestGeoPoint_launchesCorrectIntent() {
        when(prompt.getAppearanceHint()).thenReturn(Appearances.MAPS);

        activityGeoDataRequester.requestGeoPoint(prompt, "", waitingForDataRegistry);
        Intent startedIntent = shadowActivity.getNextStartedActivity();

        assertEquals(startedIntent.getComponent(), new ComponentName(testActivity, GeoPointMapActivity.class));
        assertEquals(shadowActivity.getNextStartedActivityForResult().requestCode, LOCATION_CAPTURE);

        Bundle bundle = startedIntent.getExtras();
        assertThat(bundle.getDoubleArray(GeoPointMapActivity.EXTRA_LOCATION), equalTo(null));
        assertThat(bundle.getBoolean(EXTRA_READ_ONLY), equalTo(false));
        assertThat(bundle.getBoolean(EXTRA_DRAGGABLE_ONLY), equalTo((Object) false));
    }

    @Test
    public void whenWidgetHasMapsAppearance_andIsReadOnly_requestGeoPoint_launchesCorrectIntent() {
        when(prompt.getAppearanceHint()).thenReturn(Appearances.MAPS);

        when(prompt.isReadOnly()).thenReturn(true);
        activityGeoDataRequester.requestGeoPoint(prompt, "", waitingForDataRegistry);
        Intent startedIntent = shadowActivity.getNextStartedActivity();

        assertEquals(startedIntent.getComponent(), new ComponentName(testActivity, GeoPointMapActivity.class));
        assertEquals(shadowActivity.getNextStartedActivityForResult().requestCode, LOCATION_CAPTURE);

        Bundle bundle = startedIntent.getExtras();
        assertThat(bundle.getDoubleArray(GeoPointMapActivity.EXTRA_LOCATION), equalTo(null));
        assertThat(bundle.getBoolean(EXTRA_READ_ONLY), equalTo(true));
        assertThat(bundle.getBoolean(EXTRA_DRAGGABLE_ONLY), equalTo((Object) false));
    }

    @Test
    public void whenWidgetHasPlacementMapAppearance_requestGeoPoint_launchesCorrectIntent() {
        when(prompt.getAppearanceHint()).thenReturn(Appearances.PLACEMENT_MAP);

        activityGeoDataRequester.requestGeoPoint(prompt, "", waitingForDataRegistry);
        Intent startedIntent = shadowActivity.getNextStartedActivity();

        assertEquals(startedIntent.getComponent(), new ComponentName(testActivity, GeoPointMapActivity.class));
        assertEquals(shadowActivity.getNextStartedActivityForResult().requestCode, LOCATION_CAPTURE);

        Bundle bundle = startedIntent.getExtras();
        assertThat(bundle.getDoubleArray(GeoPointMapActivity.EXTRA_LOCATION), equalTo(null));
        assertThat(bundle.getBoolean(EXTRA_READ_ONLY), equalTo(false));
        assertThat(bundle.getBoolean(EXTRA_DRAGGABLE_ONLY), equalTo((Object) true));
    }

    @Test
    public void requestGeoShape_launchesCorrectIntent() {
        activityGeoDataRequester.requestGeoShape(prompt, "blah", waitingForDataRegistry);
        Intent startedIntent = shadowActivity.getNextStartedActivity();

        assertEquals(startedIntent.getComponent(), new ComponentName(testActivity, GeoPolyActivity.class));
        assertEquals(shadowActivity.getNextStartedActivityForResult().requestCode, GEOSHAPE_CAPTURE);

        Bundle bundle = startedIntent.getExtras();
        assertThat(bundle.getString(GeoPolyActivity.ANSWER_KEY), equalTo("blah"));
        assertThat(bundle.get(GeoPolyActivity.OUTPUT_MODE_KEY), equalTo(GeoPolyActivity.OutputMode.GEOSHAPE));
        assertThat(bundle.getBoolean(EXTRA_READ_ONLY), equalTo(false));
    }

    @Test
    public void whenWidgetIsReadOnly_requestGeoShape_launchesCorrectIntent() {
        when(prompt.isReadOnly()).thenReturn(true);
        activityGeoDataRequester.requestGeoShape(prompt, "blah", waitingForDataRegistry);
        Intent startedIntent = shadowActivity.getNextStartedActivity();

        assertEquals(startedIntent.getComponent(), new ComponentName(testActivity, GeoPolyActivity.class));
        assertEquals(shadowActivity.getNextStartedActivityForResult().requestCode, GEOSHAPE_CAPTURE);

        Bundle bundle = startedIntent.getExtras();
        assertThat(bundle.getString(GeoPolyActivity.ANSWER_KEY), equalTo("blah"));
        assertThat(bundle.get(GeoPolyActivity.OUTPUT_MODE_KEY), equalTo(GeoPolyActivity.OutputMode.GEOSHAPE));
        assertThat(bundle.getBoolean(EXTRA_READ_ONLY), equalTo(true));
    }

    @Test
    public void requestGeoTrace_launchesCorrectIntent() {
        activityGeoDataRequester.requestGeoTrace(prompt, "blah", waitingForDataRegistry);
        Intent startedIntent = shadowActivity.getNextStartedActivity();

        assertEquals(startedIntent.getComponent(), new ComponentName(testActivity, GeoPolyActivity.class));
        assertEquals(shadowActivity.getNextStartedActivityForResult().requestCode, GEOTRACE_CAPTURE);

        Bundle bundle = startedIntent.getExtras();
        assertThat(bundle.getString(GeoPolyActivity.ANSWER_KEY), equalTo("blah"));
        assertThat(bundle.get(GeoPolyActivity.OUTPUT_MODE_KEY), equalTo(GeoPolyActivity.OutputMode.GEOTRACE));
        assertThat(bundle.getBoolean(EXTRA_READ_ONLY), equalTo(false));
    }

    @Test
    public void whenWidgetIsReadOnly_requestGeoTrace_launchesCorrectIntent() {
        when(prompt.isReadOnly()).thenReturn(true);

        activityGeoDataRequester.requestGeoTrace(prompt, "blah", waitingForDataRegistry);
        Intent startedIntent = shadowActivity.getNextStartedActivity();

        assertEquals(startedIntent.getComponent(), new ComponentName(testActivity, GeoPolyActivity.class));
        assertEquals(shadowActivity.getNextStartedActivityForResult().requestCode, GEOTRACE_CAPTURE);

        Bundle bundle = startedIntent.getExtras();
        assertThat(bundle.getString(GeoPolyActivity.ANSWER_KEY), equalTo("blah"));
        assertThat(bundle.get(GeoPolyActivity.OUTPUT_MODE_KEY), equalTo(GeoPolyActivity.OutputMode.GEOTRACE));
        assertThat(bundle.getBoolean(EXTRA_READ_ONLY), equalTo(true));
    }

    @Test
    public void requestGeoPoint_whenWidgetHasAllowMockAccuracy_addsItToIntent() {
        when(prompt.getBindAttributes())
                .thenReturn(asList(TreeElement.constructAttributeElement("odk", "allow-mock-accuracy", "true")));

        activityGeoDataRequester.requestGeoPoint(prompt, "blah", waitingForDataRegistry);

        Intent startedIntent = shadowActivity.getNextStartedActivity();
        assertEquals(startedIntent.getComponent(), new ComponentName(testActivity, GeoPointActivity.class));
        assertTrue(startedIntent.getBooleanExtra(EXTRA_RETAIN_MOCK_ACCURACY, false));

        when(prompt.getBindAttributes())
                .thenReturn(asList(TreeElement.constructAttributeElement("odk", "allow-mock-accuracy", "false")));

        activityGeoDataRequester.requestGeoPoint(prompt, "blah", waitingForDataRegistry);

        startedIntent = shadowActivity.getNextStartedActivity();
        assertEquals(startedIntent.getComponent(), new ComponentName(testActivity, GeoPointActivity.class));
        assertFalse(startedIntent.getBooleanExtra(EXTRA_RETAIN_MOCK_ACCURACY, true));
    }

    @Test
    public void requestGeoShape_whenWidgetHasAllowMockAccuracy_addsItToIntent() {
        when(prompt.getBindAttributes())
                .thenReturn(asList(TreeElement.constructAttributeElement("odk", "allow-mock-accuracy", "true")));

        activityGeoDataRequester.requestGeoShape(prompt, "blah", waitingForDataRegistry);

        Intent startedIntent = shadowActivity.getNextStartedActivity();
        assertEquals(startedIntent.getComponent(), new ComponentName(testActivity, GeoPolyActivity.class));
        assertTrue(startedIntent.getBooleanExtra(EXTRA_RETAIN_MOCK_ACCURACY, false));

        when(prompt.getBindAttributes())
                .thenReturn(asList(TreeElement.constructAttributeElement("odk", "allow-mock-accuracy", "false")));

        activityGeoDataRequester.requestGeoShape(prompt, "blah", waitingForDataRegistry);

        startedIntent = shadowActivity.getNextStartedActivity();
        assertEquals(startedIntent.getComponent(), new ComponentName(testActivity, GeoPolyActivity.class));
        assertFalse(startedIntent.getBooleanExtra(EXTRA_RETAIN_MOCK_ACCURACY, true));
    }

    @Test
    public void requestGeoTrace_whenWidgetHasAllowMockAccuracy_addsItToIntent() {
        when(prompt.getBindAttributes())
                .thenReturn(asList(TreeElement.constructAttributeElement("odk", "allow-mock-accuracy", "true")));

        activityGeoDataRequester.requestGeoTrace(prompt, "blah", waitingForDataRegistry);

        Intent startedIntent = shadowActivity.getNextStartedActivity();
        assertEquals(startedIntent.getComponent(), new ComponentName(testActivity, GeoPolyActivity.class));
        assertTrue(startedIntent.getBooleanExtra(EXTRA_RETAIN_MOCK_ACCURACY, false));

        when(prompt.getBindAttributes())
                .thenReturn(asList(TreeElement.constructAttributeElement("odk", "allow-mock-accuracy", "false")));

        activityGeoDataRequester.requestGeoTrace(prompt, "blah", waitingForDataRegistry);

        startedIntent = shadowActivity.getNextStartedActivity();
        assertEquals(startedIntent.getComponent(), new ComponentName(testActivity, GeoPolyActivity.class));
        assertFalse(startedIntent.getBooleanExtra(EXTRA_RETAIN_MOCK_ACCURACY, true));
    }
}
