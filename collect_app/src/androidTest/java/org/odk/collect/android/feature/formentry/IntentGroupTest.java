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

import net.bytebuddy.utility.RandomString;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.odk.collect.android.BuildConfig;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.storage.StoragePathProvider;
import org.odk.collect.android.support.CopyFormRule;
import org.odk.collect.android.support.ResetStateRule;
import org.odk.collect.android.support.FormLoadingUtils;

import java.io.File;
import java.io.IOException;
import java.util.Random;

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
    public void externalApp_ShouldPopulateStringFields() {
        Intent resultIntent = new Intent();

        int randomInteger = new Random().nextInt(255);
        double scale = Math.pow(10, 2);
        double randomDecimal = Math.round(new Random().nextDouble() * scale) / scale;
        String randomText = RandomString.make();

        resultIntent.putExtra("questionInteger", randomInteger);
        resultIntent.putExtra("questionDecimal", randomDecimal);
        resultIntent.putExtra("questionText", randomText);

        intending(not(isInternal())).respondWith(new Instrumentation.ActivityResult(Activity.RESULT_OK, resultIntent));
        onView(withText("This is buttonText")).perform(click());

        onView(withText(Integer.toString(randomInteger))).check(matches(isDisplayed()));
        onView(withText(String.valueOf(randomDecimal))).check(matches(isDisplayed()));
        onView(withText(randomText)).check(matches(isDisplayed()));
    }

    @Test
    public void externalApp_ShouldPopulateImageField() throws IOException {
        onView(withTagValue(is("ImageView"))).check(doesNotExist());
        onView(withId(R.id.capture_image)).check(doesNotExist());
        onView(withId(R.id.choose_image)).check(doesNotExist());

        Intent resultIntent = new Intent();

        File tmpJpg = new File(new StoragePathProvider().getTmpFilePath());
        copyFileFromAssets("media" + File.separator + "famous.jpg", tmpJpg.getPath());

        ClipData clipData = ClipData.newRawUri(null, null);
        clipData.addItem(new ClipData.Item("questionImage", null, getUriForFile(tmpJpg)));

        resultIntent.setClipData(clipData);
        intending(not(isInternal())).respondWith(new Instrumentation.ActivityResult(Activity.RESULT_OK, resultIntent));
        onView(withText("This is buttonText")).perform(click());

        onView(withTagValue(is("ImageView"))).check(matches(isDisplayed()));
        onView(withId(R.id.capture_image)).check(doesNotExist());
        onView(withId(R.id.choose_image)).check(doesNotExist());
    }

    @Test
    public void externalApp_ShouldPopulateAudioField() throws IOException {
        onView(withId(R.id.audio_controller)).check(matches(not(isDisplayed())));

        Intent resultIntent = new Intent();

        File tmpJpg = new File(new StoragePathProvider().getTmpFilePath());
        copyFileFromAssets("media" + File.separator + "sampleAudio.wav", tmpJpg.getPath());

        ClipData clipData = ClipData.newRawUri(null, null);
        clipData.addItem(new ClipData.Item("questionAudio", null, getUriForFile(tmpJpg)));

        resultIntent.setClipData(clipData);
        intending(not(isInternal())).respondWith(new Instrumentation.ActivityResult(Activity.RESULT_OK, resultIntent));
        onView(withText("This is buttonText")).perform(click());

        onView(withId(R.id.audio_controller)).check(matches(isDisplayed()));
    }

    @Test
    public void externalApp_ShouldPopulateVideoField() throws IOException {
        onView(withId(R.id.play_video)).check(matches(isDisplayed()));
        onView(withId(R.id.play_video)).check(matches(not(isEnabled())));

        Intent resultIntent = new Intent();

        File tmpJpg = new File(new StoragePathProvider().getTmpFilePath());
        copyFileFromAssets("media" + File.separator + "sampleVideo.mp4", tmpJpg.getPath());

        ClipData clipData = ClipData.newRawUri(null, null);
        clipData.addItem(new ClipData.Item("questionVideo", null, getUriForFile(tmpJpg)));

        resultIntent.setClipData(clipData);
        intending(not(isInternal())).respondWith(new Instrumentation.ActivityResult(Activity.RESULT_OK, resultIntent));
        onView(withText("This is buttonText")).perform(click());

        onView(withId(R.id.play_video)).check(matches(isDisplayed()));
        onView(withId(R.id.play_video)).check(matches(isEnabled()));
    }

    @Test
    public void externalApp_ShouldPopulateArbitraryFileField() throws IOException {
        onView(withTagValue(is("ArbitraryFileWidgetAnswer"))).check(matches(not(isDisplayed())));

        Intent resultIntent = new Intent();

        File tmpJpg = new File(new StoragePathProvider().getTmpFilePath());
        copyFileFromAssets("media" + File.separator + "fruits.csv", tmpJpg.getPath());

        ClipData clipData = ClipData.newRawUri(null, null);
        clipData.addItem(new ClipData.Item("questionFile", null, getUriForFile(tmpJpg)));

        resultIntent.setClipData(clipData);
        intending(not(isInternal())).respondWith(new Instrumentation.ActivityResult(Activity.RESULT_OK, resultIntent));
        onView(withText("This is buttonText")).perform(click());

        onView(withTagValue(is("ArbitraryFileWidgetAnswer"))).check(matches(isDisplayed()));
    }

    private Uri getUriForFile(File file) {
        return FileProvider.getUriForFile(Collect.getInstance(), BuildConfig.APPLICATION_ID + ".provider", file);
    }
}
