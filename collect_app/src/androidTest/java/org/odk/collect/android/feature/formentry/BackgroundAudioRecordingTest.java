package org.odk.collect.android.feature.formentry;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.odk.collect.android.support.FileUtils.copyFileFromAssets;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.GrantPermissionRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.listeners.PermissionListener;
import org.odk.collect.android.permissions.PermissionsProvider;
import org.odk.collect.android.storage.StorageSubdirectory;
import org.odk.collect.android.support.CollectTestRule;
import org.odk.collect.android.support.TestDependencies;
import org.odk.collect.android.support.TestRuleChain;
import org.odk.collect.android.support.pages.FormEndPage;
import org.odk.collect.android.support.pages.FormEntryPage;
import org.odk.collect.android.support.pages.MainMenuPage;
import org.odk.collect.android.support.pages.SaveOrIgnoreDialog;
import org.odk.collect.androidshared.system.PermissionsChecker;
import org.odk.collect.audiorecorder.recording.AudioRecorder;
import org.odk.collect.audiorecorder.testsupport.StubAudioRecorder;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;

@RunWith(AndroidJUnit4.class)
public class BackgroundAudioRecordingTest {

    private StubAudioRecorder stubAudioRecorderViewModel;

    private RevokeableRecordAudioPermissionsChecker permissionsChecker;
    private ControllableRecordAudioPermissionsProvider permissionsProvider;
    public final TestDependencies testDependencies = new TestDependencies() {

        @Override
        public AudioRecorder providesAudioRecorder(Application application) {
            if (stubAudioRecorderViewModel == null) {
                try {
                    File stubRecording = File.createTempFile("test", ".m4a");
                    stubRecording.deleteOnExit();

                    copyFileFromAssets("media/test.m4a", stubRecording.getAbsolutePath());
                    stubAudioRecorderViewModel = new StubAudioRecorder(stubRecording.getAbsolutePath());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            return stubAudioRecorderViewModel;
        }

        @Override
        public PermissionsChecker providesPermissionsChecker(Context context) {
            if (permissionsChecker == null) {
                permissionsChecker = new RevokeableRecordAudioPermissionsChecker(context);
            }

            return permissionsChecker;
        }

        @Override
        public PermissionsProvider providesPermissionsProvider(PermissionsChecker permissionsChecker) {
            if (permissionsProvider == null) {
                permissionsProvider = new ControllableRecordAudioPermissionsProvider(permissionsChecker);
            }

            return permissionsProvider;
        }
    };

    public final CollectTestRule rule = new CollectTestRule();

    @Rule
    public final RuleChain chain = TestRuleChain.chain(testDependencies)
            .around(GrantPermissionRule.grant(Manifest.permission.RECORD_AUDIO))
            .around(rule);

    @Test
    public void fillingOutForm_recordsAudio() throws Exception {
        FormEntryPage formEntryPage = rule.startAtMainMenu()
                .copyForm("one-question-background-audio.xml")
                .startBlankForm("One Question");
        assertThat(stubAudioRecorderViewModel.isRecording(), is(true));

        FormEndPage formEndPage = formEntryPage
                .inputText("123")
                .swipeToEndScreen();
        assertThat(stubAudioRecorderViewModel.isRecording(), is(true));

        formEndPage.clickSaveAndExit();
        assertThat(stubAudioRecorderViewModel.isRecording(), is(false));

        File instancesDir = new File(testDependencies.storagePathProvider.getOdkDirPath(StorageSubdirectory.INSTANCES));
        File recording = Arrays.stream(instancesDir.listFiles()[0].listFiles()).filter(f -> f.getName().contains(".fake")).findAny().get();
        File instanceFile = Arrays.stream(instancesDir.listFiles()[0].listFiles()).filter(f -> f.getName().contains(".xml")).findAny().get();
        String instanceXml = new String(Files.readAllBytes(instanceFile.toPath()));
        assertThat(instanceXml, containsString("<recording>" + recording.getName() + "</recording>"));
    }

    @Test
    public void fillingOutForm_withMultipleRecordActions_recordsAudioOnceForAllOfThem() throws Exception {
        FormEntryPage formEntryPage = rule.startAtMainMenu()
                .copyForm("one-question-background-audio-multiple.xml")
                .startBlankForm("One Question");
        assertThat(stubAudioRecorderViewModel.isRecording(), is(true));

        FormEndPage formEndPage = formEntryPage
                .inputText("123")
                .swipeToEndScreen();
        assertThat(stubAudioRecorderViewModel.isRecording(), is(true));

        formEndPage.clickSaveAndExit();
        assertThat(stubAudioRecorderViewModel.isRecording(), is(false));

        File instancesDir = new File(testDependencies.storagePathProvider.getOdkDirPath(StorageSubdirectory.INSTANCES));
        File recording = Arrays.stream(instancesDir.listFiles()[0].listFiles()).filter(f -> f.getName().contains(".fake")).findAny().get();
        File instanceFile = Arrays.stream(instancesDir.listFiles()[0].listFiles()).filter(f -> f.getName().contains(".xml")).findAny().get();
        String instanceXml = new String(Files.readAllBytes(instanceFile.toPath()));
        assertThat(instanceXml, containsString("<recording1>" + recording.getName() + "</recording1>"));
        assertThat(instanceXml, containsString("<recording2>" + recording.getName() + "</recording2>"));
    }

    @Test
    public void pressingBackWhileRecording_andClickingSave_exitsForm() {
        rule.startAtMainMenu()
                .copyForm("one-question-background-audio.xml")
                .startBlankForm("One Question")
                .closeSoftKeyboard()
                .pressBack(new SaveOrIgnoreDialog<>("One Question", new MainMenuPage()))
                .clickSaveChanges();
    }

    @Test
    public void uncheckingRecordAudio_andConfirming_endsAndDeletesRecording() {
        FormEntryPage formEntryPage = rule.startAtMainMenu()
                .copyForm("one-question-background-audio.xml")
                .startBlankForm("One Question")
                .clickOptionsIcon()
                .clickRecordAudio()
                .clickOk();

        assertThat(stubAudioRecorderViewModel.isRecording(), is(false));
        assertThat(stubAudioRecorderViewModel.getLastRecording(), is(nullValue()));

        formEntryPage.closeSoftKeyboard()
                .pressBack(new SaveOrIgnoreDialog<>("One Question", new MainMenuPage()))
                .clickIgnoreChanges()
                .startBlankForm("One Question");

        assertThat(stubAudioRecorderViewModel.isRecording(), is(false));
    }

    @Test
    public void whenRecordAudioPermissionNotGranted_openingForm_andDenyingPermissions_closesForm() {
        permissionsChecker.revoke();
        permissionsProvider.makeControllable();

        rule.startAtMainMenu()
                .copyForm("one-question-background-audio.xml")
                .startBlankFormWithDialog("One Question")
                .assertText(R.string.background_audio_permission_explanation)
                .clickOK(new FormEntryPage("One Question"));

        permissionsProvider.deny();
        new MainMenuPage().assertOnPage();
    }

    private static class RevokeableRecordAudioPermissionsChecker extends PermissionsChecker {

        private boolean revoked;

        RevokeableRecordAudioPermissionsChecker(Context context) {
            super(context);
        }

        @Override
        public boolean isPermissionGranted(String... permissions) {
            if (permissions[0].equals(Manifest.permission.RECORD_AUDIO) && revoked) {
                return false;
            } else {
                return super.isPermissionGranted(permissions);
            }
        }

        public void revoke() {
            revoked = true;
        }
    }

    private static class ControllableRecordAudioPermissionsProvider extends PermissionsProvider {

        private PermissionListener action;
        private boolean controllable;

        ControllableRecordAudioPermissionsProvider(PermissionsChecker permissionsChecker) {
            super(permissionsChecker);
        }

        @Override
        public void requestRecordAudioPermission(Activity activity, @NonNull PermissionListener action) {
            if (controllable) {
                this.action = action;
            } else {
                super.requestRecordAudioPermission(activity, action);
            }
        }

        public void makeControllable() {
            controllable = true;
        }

        public void grant() {
            InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> action.granted());
        }

        public void deny() {
            InstrumentationRegistry.getInstrumentation().runOnMainSync(() -> action.denied());
        }
    }
}
