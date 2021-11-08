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

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.isInternal;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withTagValue;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.core.StringEndsWith.endsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.support.CustomMatchers.withIndex;
import static org.odk.collect.android.support.FileUtils.copyFileFromAssets;
import static org.odk.collect.android.support.actions.NestedScrollToAction.nestedScrollTo;

import android.Manifest;
import android.app.Activity;
import android.app.Instrumentation;
import android.content.ClipData;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;

import androidx.core.content.FileProvider;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.rule.GrantPermissionRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.odk.collect.android.BuildConfig;
import org.odk.collect.android.R;
import org.odk.collect.android.RecordedIntentsRule;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.support.CopyFormRule;
import org.odk.collect.android.support.FormActivityTestRule;
import org.odk.collect.android.support.TestRuleChain;

import java.io.File;
import java.io.IOException;

/**
 * Tests that intent groups work as documented at https://docs.getodk.org/launch-apps-from-collect/#launching-external-apps-to-populate-multiple-fields
 */
public class IntentGroupTest {
    private static final String INTENT_GROUP_FORM = "intent-group.xml";

    public FormActivityTestRule rule = new FormActivityTestRule(INTENT_GROUP_FORM, "intent-group");

    @Rule
    public RuleChain copyFormChain = TestRuleChain.chain()
            .around(new RecordedIntentsRule())
            .around(GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE)) // Needed to write temp file
            .around(new CopyFormRule(INTENT_GROUP_FORM, true))
            .around(rule);

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
        rule.startInFormEntry()
                .clickOnText("This is buttonText")
                .checkIsToastWithMessageDisplayed("This is noAppErrorString");
    }

    @Test
    public void externalApp_ShouldPopulateFields() throws IOException {
        assertImageWidgetWithoutAnswer();
        assertAudioWidgetWithoutAnswer();
        assertVideoWidgetWithoutAnswer();
        assertFileWidgetWithoutAnswer();

        Intent resultIntent = new Intent();

        Uri imageUri = createTempFile("famous", "jpg");
        Uri audioUri = createTempFile("sampleAudio", "wav");
        Uri videoUri = createTempFile("sampleVideo", "mp4");
        Uri fileUri = createTempFile("fruits", "csv");

        resultIntent.putExtra("questionInteger", "25");
        resultIntent.putExtra("questionDecimal", "46.74");
        resultIntent.putExtra("questionText", "sampleAnswer");
        resultIntent.putExtra("questionImage", imageUri);
        resultIntent.putExtra("questionAudio", audioUri);
        resultIntent.putExtra("questionVideo", videoUri);
        resultIntent.putExtra("questionFile", fileUri);

        ClipData clipData = ClipData.newRawUri(null, null);
        clipData.addItem(new ClipData.Item("questionImage", null, imageUri));
        clipData.addItem(new ClipData.Item("questionAudio", null, audioUri));
        clipData.addItem(new ClipData.Item("questionVideo", null, videoUri));
        clipData.addItem(new ClipData.Item("questionFile", null, fileUri));

        resultIntent.setClipData(clipData);
        resultIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intending(not(isInternal())).respondWith(new Instrumentation.ActivityResult(Activity.RESULT_OK, resultIntent));
        onView(withText("This is buttonText")).perform(nestedScrollTo(), click());

        assertImageWidgetWithAnswer();
        assertAudioWidgetWithAnswer();
        assertVideoWidgetWithAnswer();
        assertFileWidgetWithAnswer();
    }

    @Test
    public void externalApp_ShouldNotPopulateFieldsIfAnswersAreNull() {
        assertImageWidgetWithoutAnswer();
        assertAudioWidgetWithoutAnswer();
        assertVideoWidgetWithoutAnswer();
        assertFileWidgetWithoutAnswer();

        Intent resultIntent = new Intent();

        resultIntent.putExtra("questionInteger", (Bundle) null);
        resultIntent.putExtra("questionDecimal", (Bundle) null);
        resultIntent.putExtra("questionText", (Bundle) null);
        resultIntent.putExtra("questionImage", (Bundle) null);
        resultIntent.putExtra("questionAudio", (Bundle) null);
        resultIntent.putExtra("questionVideo", (Bundle) null);
        resultIntent.putExtra("questionFile", (Bundle) null);

        ClipData clipData = ClipData.newRawUri(null, null);
        clipData.addItem(new ClipData.Item("questionImage", null, null));
        clipData.addItem(new ClipData.Item("questionAudio", null, null));
        clipData.addItem(new ClipData.Item("questionVideo", null, null));
        clipData.addItem(new ClipData.Item("questionFile", null, null));

        resultIntent.setClipData(clipData);
        resultIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intending(not(isInternal())).respondWith(new Instrumentation.ActivityResult(Activity.RESULT_OK, resultIntent));
        onView(withText("This is buttonText")).perform(nestedScrollTo(), click());

        onView(withIndex(withClassName(endsWith("EditText")), 0)).check(matches(withText("")));
        onView(withIndex(withClassName(endsWith("EditText")), 1)).check(matches(withText("")));
        onView(withIndex(withClassName(endsWith("EditText")), 2)).check(matches(withText("")));

        assertImageWidgetWithoutAnswer();
        assertAudioWidgetWithoutAnswer();
        assertVideoWidgetWithoutAnswer();
        assertFileWidgetWithoutAnswer();
    }

    @Test
    public void collect_shouldNotCrashWhenAnyExceptionIsThrownWhileReceivingAnswer() {
        assertImageWidgetWithoutAnswer();

        Intent resultIntent = new Intent();

        Uri uri = mock(Uri.class);
        when(uri.getScheme()).thenThrow(new RuntimeException());

        resultIntent.putExtra("questionImage", uri);

        intending(not(isInternal())).respondWith(new Instrumentation.ActivityResult(Activity.RESULT_OK, resultIntent));

        onView(withText("This is buttonText")).perform(click());

        assertImageWidgetWithoutAnswer();
    }

    @Test
    public void collect_shouldNotCrashWhenAnyErrorIsThrownWhileReceivingAnswer() {
        assertImageWidgetWithoutAnswer();

        Intent resultIntent = new Intent();

        Uri uri = mock(Uri.class);
        when(uri.getScheme()).thenThrow(new Error());

        resultIntent.putExtra("questionImage", uri);

        intending(not(isInternal())).respondWith(new Instrumentation.ActivityResult(Activity.RESULT_OK, resultIntent));

        onView(withText("This is buttonText")).perform(click());

        assertImageWidgetWithoutAnswer();
    }

    private void assertImageWidgetWithoutAnswer() {
        onView(withTagValue(is("ImageView"))).check(doesNotExist());
        onView(withId(R.id.capture_image)).check(doesNotExist());
        onView(withId(R.id.choose_image)).check(doesNotExist());
    }

    private void assertAudioWidgetWithoutAnswer() {
        onView(withId(R.id.audio_controller)).check(matches(not(isDisplayed())));
    }

    private void assertVideoWidgetWithoutAnswer() {
        onView(withText(is("Video external"))).perform(nestedScrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.play_video)).check(matches(not(isDisplayed())));
    }

    private void assertFileWidgetWithoutAnswer() {
        onView(withTagValue(is("ArbitraryFileWidgetAnswer"))).check(matches(not(isDisplayed())));
    }

    private void assertImageWidgetWithAnswer() {
        onView(withTagValue(is("ImageView"))).perform(nestedScrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.capture_image)).check(doesNotExist());
        onView(withId(R.id.choose_image)).check(doesNotExist());
    }

    private void assertAudioWidgetWithAnswer() {
        onView(withId(R.id.audio_controller)).perform(nestedScrollTo()).check(matches(isDisplayed()));
    }

    private void assertVideoWidgetWithAnswer() {
        onView(withId(R.id.play_video)).perform(nestedScrollTo()).check(matches(isDisplayed()));
        onView(withId(R.id.play_video)).check(matches(isEnabled()));
    }

    private void assertFileWidgetWithAnswer() {
        onView(withTagValue(is("ArbitraryFileWidgetAnswer"))).perform(nestedScrollTo()).check(matches(isDisplayed()));
    }

    private Uri createTempFile(String name, String extension) throws IOException {
        // Use the phones downloads dir for temp files
        File downloadsDir = ApplicationProvider
                .getApplicationContext()
                .getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);

        File file = File.createTempFile(name, extension, downloadsDir);
        copyFileFromAssets("media" + File.separator + name + "." + extension, file.getPath());
        return getUriForFile(file);
    }

    private Uri getUriForFile(File file) {
        return FileProvider.getUriForFile(Collect.getInstance(), BuildConfig.APPLICATION_ID + ".provider", file);
    }
}
