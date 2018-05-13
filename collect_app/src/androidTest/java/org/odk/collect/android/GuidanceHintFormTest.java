package org.odk.collect.android;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Environment;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.text.TextUtils;

import org.apache.commons.io.IOUtils;
import org.javarosa.form.api.FormEntryPrompt;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.preferences.GeneralSharedPreferences;
import org.odk.collect.android.preferences.GuidanceHint;
import org.odk.collect.android.preferences.PreferenceKeys;
import org.odk.collect.android.utilities.ActivityAvailability;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

import tools.fastlane.screengrab.Screengrab;
import tools.fastlane.screengrab.UiAutomatorScreenshotStrategy;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.Assert.assertFalse;
import static org.odk.collect.android.activities.FormEntryActivity.EXTRA_TESTING_PATH;
import static org.odk.collect.android.test.TestUtils.waitId;

@RunWith(AndroidJUnit4.class)
public class GuidanceHintFormTest {

    private static final String GUIDANCE_SAMPLE_FORM = "guidance_hint_form.xml";
    private static final String FORMS_DIRECTORY = "/odk/forms/";

    @Rule
    public FormEntryActivityTestRule activityTestRule = new FormEntryActivityTestRule();

    @Mock
    private ActivityAvailability activityAvailability;

    //region Test prep.
    @BeforeClass
    public static void copyFormToSdCard() throws IOException {
        String pathname = formPath();
        if (new File(pathname).exists()) {
            return;
        }

        AssetManager assetManager = InstrumentationRegistry.getContext().getAssets();
        InputStream inputStream = assetManager.open(GUIDANCE_SAMPLE_FORM);

        File outFile = new File(pathname);
        OutputStream outputStream = new FileOutputStream(outFile);

        IOUtils.copy(inputStream, outputStream);
    }

    @BeforeClass
    public static void beforeAll() {
        Screengrab.setDefaultScreenshotStrategy(new UiAutomatorScreenshotStrategy());
    }

    @Before
    public void prepareDependencies() {
        FormEntryActivity activity = activityTestRule.getActivity();
        activity.setActivityAvailability(activityAvailability);
        activity.setShouldOverrideAnimations(true);
    }

    @Ignore
    @Test
    public void guidanceVisibilityContentTest() {
        GeneralSharedPreferences.getInstance().save(PreferenceKeys.KEY_GUIDANCE_HINT, GuidanceHint.Yes.toString());

        FormEntryPrompt prompt = Collect.getInstance().getFormController().getQuestionPrompt();

        String guidance = prompt.getSpecialFormQuestionText(prompt.getQuestion().getHelpTextID(), "guidance");

        assertFalse(TextUtils.isEmpty(guidance));

        Screengrab.screenshot("guidance_hint");

        onView(isRoot()).perform(waitId(R.id.guidance_text_view, TimeUnit.SECONDS.toMillis(15)));
        onView(withId(R.id.guidance_text_view)).check(matches(withText(guidance)));
    }

    //region Helper methods.
    private static String formPath() {
        return Environment.getExternalStorageDirectory().getPath()
                + FORMS_DIRECTORY
                + GUIDANCE_SAMPLE_FORM;
    }

    //region Custom TestRule.
    private class FormEntryActivityTestRule extends IntentsTestRule<FormEntryActivity> {

        FormEntryActivityTestRule() {
            super(FormEntryActivity.class);
        }

        @Override
        protected Intent getActivityIntent() {
            Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
            Intent intent = new Intent(context, FormEntryActivity.class);

            intent.putExtra(EXTRA_TESTING_PATH, formPath());

            return intent;
        }
    }
    //endregion
}
