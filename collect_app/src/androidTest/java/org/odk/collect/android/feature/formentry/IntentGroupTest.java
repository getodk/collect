/*
 * Copyright 2019 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.odk.collect.android.feature.formentry;

import android.Manifest;
import android.app.Activity;
import android.app.Instrumentation;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;

import androidx.core.content.FileProvider;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.odk.collect.android.BuildConfig;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.support.CopyFormRule;
import org.odk.collect.android.support.ResetStateRule;
import org.odk.collect.android.support.FormLoadingUtils;

import java.io.File;
import java.io.IOException;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.isInternal;
import static androidx.test.espresso.matcher.RootMatchers.withDecorView;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withTagValue;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.odk.collect.android.support.FileUtils.copyFileFromAssets;
import static org.odk.collect.android.support.actions.NestedScrollToAction.nestedScrollTo;

/**
 * Tests that intent groups work as documented at https://docs.getodk.org/launch-apps-from-collect/#launching-external-apps-to-populate-multiple-fields
 */
public class IntentGroupTest {
    private static final String INTENT_GROUP_FORM = "intent-group.xml";

    @Rule
    public ActivityTestRule<FormEntryActivity> activityTestRule = FormLoadingUtils.getFormActivityTestRuleFor(INTENT_GROUP_FORM);

    @Rule
    public RuleChain copyFormChain = RuleChain
            .outerRule(GrantPermissionRule.grant(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            ))
            .around(new ResetStateRule())
            .around(new CopyFormRule(INTENT_GROUP_FORM, true));

    // Verifies that a value given to the label text with form buttonText is used as the button text.
    @Test
    public void buttonName_ShouldComeFromSpecialFormText() {
        onView(withText(R.string.launch_app)).check(doesNotExist());
        onView(withText("This is buttonText")).check(matches(isDisplayed()));
    }

    // Verifies that a value given to the label text with form noAppErrorString is used as the toast
    // text if no app is found.
    @Test
    public void appMissingErrorText_ShouldComeFromSpecialFormText() {
        onView(withText("This is buttonText")).perform(click());
        onView(withText("This is noAppErrorString")).inRoot(withDecorView(not(activityTestRule.getActivity().getWindow().getDecorView()))).check(matches(isDisplayed()));
    }

    @Test
    public void externalApp_ShouldPopulateFields() throws IOException {
        // Check ImageWidget without answer
        onView(withTagValue(is("ImageView"))).check(doesNotExist());
        onView(withId(R.id.capture_image)).check(doesNotExist());
        onView(withId(R.id.choose_image)).check(doesNotExist());

        // Check AudioWidget without answer
        onView(withId(R.id.audio_controller)).check(matches(not(isDisplayed())));

        // Check VideoWidget without answer
        onView(withId(R.id.play_video)).check(matches(isDisplayed()));
        onView(withId(R.id.play_video)).check(matches(not(isEnabled())));

        // Check ArbitraryFileWidget without answer
        onView(withTagValue(is("ArbitraryFileWidgetAnswer"))).check(matches(not(isDisplayed())));

        Intent resultIntent = new Intent();

        resultIntent.putExtra("questionInteger", "25");
        resultIntent.putExtra("questionDecimal", "46.74");
        resultIntent.putExtra("questionText", "sampleAnswer");

        ClipData clipData = ClipData.newRawUri(null, null);
        clipData.addItem(new ClipData.Item("questionImage", null, createTempFile("famous", "jpg")));
        clipData.addItem(new ClipData.Item("questionAudio", null, createTempFile("sampleAudio", "wav")));
        clipData.addItem(new ClipData.Item("questionVideo", null, createTempFile("sampleVideo", "mp4")));
        clipData.addItem(new ClipData.Item("questionFile", null, createTempFile("fruits", "csv")));

        resultIntent.setClipData(clipData);
        intending(not(isInternal())).respondWith(new Instrumentation.ActivityResult(Activity.RESULT_OK, resultIntent));
        onView(withText("This is buttonText")).perform(click());

        // Check StringWidgets with answers
        onView(withText("25")).perform(nestedScrollTo()).check(matches(isDisplayed()));
        onView(withText("46.74")).perform(nestedScrollTo()).check(matches(isDisplayed()));
        onView(withText("sampleAnswer")).perform(nestedScrollTo()).check(matches(isDisplayed()));

        // Check ImageWidget with answer
        onView(withTagValue(is("ImageView"))).perform(nestedScrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.capture_image)).check(doesNotExist());
        onView(withId(R.id.choose_image)).check(doesNotExist());

        // Check AudioWidget with answer
        onView(withId(R.id.audio_controller)).perform(nestedScrollTo()).check(matches(isDisplayed()));

        // Check VideoWidget with answer
        onView(withId(R.id.play_video)).perform(nestedScrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.play_video)).check(matches(isEnabled()));

        // Check ArbitraryFileWidget with answer
        onView(withTagValue(is("ArbitraryFileWidgetAnswer"))).perform(nestedScrollTo()).check(matches(isDisplayed()));
    }

    @SuppressWarnings("PMD.DoNotHardCodeSDCard")
    private Uri createTempFile(String name, String extension) throws IOException {
        File file = File.createTempFile(name, extension, new File("/sdcard"));
        copyFileFromAssets("media" + File.separator + name + "." + extension, file.getPath());
        return getUriForFile(file);
    }

    private Uri getUriForFile(File file) {
        return FileProvider.getUriForFile(Collect.getInstance(), BuildConfig.APPLICATION_ID + ".provider", file);
    }
}
