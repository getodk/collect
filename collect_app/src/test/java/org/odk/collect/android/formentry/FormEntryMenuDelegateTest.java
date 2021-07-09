package org.odk.collect.android.formentry;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.TestSettingsProvider;
import org.odk.collect.android.activities.FormHierarchyActivity;
import org.odk.collect.android.formentry.backgroundlocation.BackgroundLocationViewModel;
import org.odk.collect.android.formentry.questions.AnswersProvider;
import org.odk.collect.android.formentry.saving.FormSaveViewModel;
import org.odk.collect.android.javarosawrapper.FormController;
import org.odk.collect.android.preferences.screens.ProjectPreferencesActivity;
import org.odk.collect.android.utilities.ApplicationConstants;
import org.odk.collect.androidshared.livedata.MutableNonNullLiveData;
import org.odk.collect.audiorecorder.recording.AudioRecorder;
import org.odk.collect.testshared.RobolectricHelpers;
import org.robolectric.Robolectric;
import org.robolectric.annotation.LooperMode;
import org.robolectric.fakes.RoboMenu;
import org.robolectric.fakes.RoboMenuItem;
import org.robolectric.shadows.ShadowActivity;

import java.util.HashMap;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.odk.collect.testshared.RobolectricHelpers.getFragmentByClass;
import static org.robolectric.Shadows.shadowOf;

@RunWith(AndroidJUnit4.class)
@LooperMode(LooperMode.Mode.PAUSED)
public class FormEntryMenuDelegateTest {

    private FormEntryMenuDelegate formEntryMenuDelegate;
    private AppCompatActivity activity;
    private FormEntryViewModel formEntryViewModel;
    private AnswersProvider answersProvider;
    private FormSaveViewModel formSaveViewModel;
    private AudioRecorder audioRecorder;
    private BackgroundAudioViewModel backgroundAudioViewModel;

    @Before
    public void setup() {
        activity = RobolectricHelpers.createThemedActivity(AppCompatActivity.class, R.style.Theme_MaterialComponents);
        FormController formController = mock(FormController.class);
        answersProvider = mock(AnswersProvider.class);
        formSaveViewModel = mock(FormSaveViewModel.class);

        audioRecorder = mock(AudioRecorder.class);
        when(audioRecorder.isRecording()).thenReturn(false);

        formEntryViewModel = mock(FormEntryViewModel.class);
        when(formEntryViewModel.hasBackgroundRecording()).thenReturn(new MutableNonNullLiveData<>(false));

        BackgroundLocationViewModel backgroundLocationViewModel = mock(BackgroundLocationViewModel.class);
        backgroundAudioViewModel = mock(BackgroundAudioViewModel.class);
        when(backgroundAudioViewModel.isBackgroundRecordingEnabled()).thenReturn(new MutableNonNullLiveData<>(true));

        formEntryMenuDelegate = new FormEntryMenuDelegate(
                activity,
                answersProvider,
                mock(FormIndexAnimationHandler.class),
                formSaveViewModel,
                formEntryViewModel,
                audioRecorder,
                backgroundLocationViewModel,
                backgroundAudioViewModel,
                TestSettingsProvider.getSettingsProvider()
        );
        formEntryMenuDelegate.formLoaded(formController);
    }

    @Test
    public void onPrepare_inRepeatQuestion_showsAddRepeat() {
        when(formEntryViewModel.canAddRepeat()).thenReturn(true);

        RoboMenu menu = new RoboMenu();
        formEntryMenuDelegate.onCreateOptionsMenu(Robolectric.setupActivity(FragmentActivity.class).getMenuInflater(), menu);
        formEntryMenuDelegate.onPrepareOptionsMenu(menu);

        assertThat(menu.findItem(R.id.menu_add_repeat).isVisible(), equalTo(true));
    }

    @Test
    public void onPrepare_notInRepeatQuestion_hidesAddRepeat() {
        when(formEntryViewModel.canAddRepeat()).thenReturn(false);

        RoboMenu menu = new RoboMenu();
        formEntryMenuDelegate.onCreateOptionsMenu(Robolectric.setupActivity(FragmentActivity.class).getMenuInflater(), menu);
        formEntryMenuDelegate.onPrepareOptionsMenu(menu);

        assertThat(menu.findItem(R.id.menu_add_repeat).isVisible(), equalTo(false));
    }

    @Test
    public void onPrepare_whenFormControllerIsNull_hidesAddRepeat() {
        formEntryMenuDelegate.formLoaded(null);

        RoboMenu menu = new RoboMenu();
        formEntryMenuDelegate.onCreateOptionsMenu(Robolectric.setupActivity(FragmentActivity.class).getMenuInflater(), menu);
        formEntryMenuDelegate.onPrepareOptionsMenu(menu);

        assertThat(menu.findItem(R.id.menu_add_repeat).isVisible(), equalTo(false));
    }

    @Test
    public void onPrepare_whenFormHasBackgroundRecording_showsRecordAudio() {
        when(formEntryViewModel.hasBackgroundRecording()).thenReturn(new MutableNonNullLiveData<>(true));

        RoboMenu menu = new RoboMenu();
        formEntryMenuDelegate.onCreateOptionsMenu(Robolectric.setupActivity(FragmentActivity.class).getMenuInflater(), menu);
        formEntryMenuDelegate.onPrepareOptionsMenu(menu);

        assertThat(menu.findItem(R.id.menu_record_audio).isVisible(), equalTo(true));
    }

    @Test
    public void onPrepare_whenBackgroundRecodingEnabled_checksRecordAudio() {
        when(backgroundAudioViewModel.isBackgroundRecordingEnabled()).thenReturn(new MutableNonNullLiveData<>(true));

        RoboMenu menu = new RoboMenu();
        formEntryMenuDelegate.onCreateOptionsMenu(Robolectric.setupActivity(FragmentActivity.class).getMenuInflater(), menu);
        formEntryMenuDelegate.onPrepareOptionsMenu(menu);

        assertThat(menu.findItem(R.id.menu_record_audio).isChecked(), equalTo(true));
    }

    @Test
    public void onPrepare_whenNotRecordingInBackground_unchecksRecordAudio() {
        when(backgroundAudioViewModel.isBackgroundRecordingEnabled()).thenReturn(new MutableNonNullLiveData<>(false));

        RoboMenu menu = new RoboMenu();
        formEntryMenuDelegate.onCreateOptionsMenu(Robolectric.setupActivity(FragmentActivity.class).getMenuInflater(), menu);
        formEntryMenuDelegate.onPrepareOptionsMenu(menu);

        assertThat(menu.findItem(R.id.menu_record_audio).isChecked(), equalTo(false));
    }

    @Test
    public void onPrepare_whenFormDoesNotHaveBackgroundRecording_hidesRecordAudio() {
        when(formEntryViewModel.hasBackgroundRecording()).thenReturn(new MutableNonNullLiveData<>(false));

        RoboMenu menu = new RoboMenu();
        formEntryMenuDelegate.onCreateOptionsMenu(Robolectric.setupActivity(FragmentActivity.class).getMenuInflater(), menu);
        formEntryMenuDelegate.onPrepareOptionsMenu(menu);

        assertThat(menu.findItem(R.id.menu_record_audio).isVisible(), equalTo(false));
    }

    @Test
    public void onItemSelected_whenAddRepeat_callsPromptForNewRepeat() {
        RoboMenu menu = new RoboMenu();
        formEntryMenuDelegate.onCreateOptionsMenu(Robolectric.setupActivity(FragmentActivity.class).getMenuInflater(), menu);
        formEntryMenuDelegate.onPrepareOptionsMenu(menu);

        formEntryMenuDelegate.onOptionsItemSelected(new RoboMenuItem(R.id.menu_add_repeat));
        verify(formEntryViewModel).promptForNewRepeat();
    }

    @Test
    public void onItemSelected_whenAddRepeat_savesScreenAnswers() {
        RoboMenu menu = new RoboMenu();
        formEntryMenuDelegate.onCreateOptionsMenu(Robolectric.setupActivity(FragmentActivity.class).getMenuInflater(), menu);
        formEntryMenuDelegate.onPrepareOptionsMenu(menu);

        HashMap answers = new HashMap();
        when(answersProvider.getAnswers()).thenReturn(answers);
        formEntryMenuDelegate.onOptionsItemSelected(new RoboMenuItem(R.id.menu_add_repeat));
        verify(formSaveViewModel).saveAnswersForScreen(answers);
    }

    @Test
    public void onItemSelected_whenAddRepeat_whenRecording_showsWarning() {
        RoboMenu menu = new RoboMenu();
        formEntryMenuDelegate.onCreateOptionsMenu(Robolectric.setupActivity(FragmentActivity.class).getMenuInflater(), menu);
        formEntryMenuDelegate.onPrepareOptionsMenu(menu);

        when(audioRecorder.isRecording()).thenReturn(true);

        formEntryMenuDelegate.onOptionsItemSelected(new RoboMenuItem(R.id.menu_add_repeat));
        verify(formEntryViewModel, never()).promptForNewRepeat();

        RecordingWarningDialogFragment dialog = getFragmentByClass(activity.getSupportFragmentManager(), RecordingWarningDialogFragment.class);
        assertThat(dialog, is(notNullValue()));
        assertThat(dialog.getDialog().isShowing(), is(true));
    }

    @Test
    public void onItemSelected_whenAddRepeat_whenRecordingInTheBackground_doesNotShowWarning() {
        RoboMenu menu = new RoboMenu();
        formEntryMenuDelegate.onCreateOptionsMenu(Robolectric.setupActivity(FragmentActivity.class).getMenuInflater(), menu);
        formEntryMenuDelegate.onPrepareOptionsMenu(menu);

        when(audioRecorder.isRecording()).thenReturn(true);
        when(backgroundAudioViewModel.isBackgroundRecording()).thenReturn(true);

        formEntryMenuDelegate.onOptionsItemSelected(new RoboMenuItem(R.id.menu_add_repeat));
        verify(formEntryViewModel).promptForNewRepeat();

        RecordingWarningDialogFragment dialog = getFragmentByClass(activity.getSupportFragmentManager(), RecordingWarningDialogFragment.class);
        assertThat(dialog, is(nullValue()));
    }

    @Test
    public void onItemSelected_whenPreferences_startsPreferencesActivityWithChangeSettingsRequest() {
        RoboMenu menu = new RoboMenu();
        formEntryMenuDelegate.onCreateOptionsMenu(Robolectric.setupActivity(FragmentActivity.class).getMenuInflater(), menu);
        formEntryMenuDelegate.onPrepareOptionsMenu(menu);

        formEntryMenuDelegate.onOptionsItemSelected(new RoboMenuItem(R.id.menu_preferences));
        ShadowActivity.IntentForResult nextStartedActivity = shadowOf(activity).getNextStartedActivityForResult();
        assertThat(nextStartedActivity, not(nullValue()));
        assertThat(nextStartedActivity.intent.getComponent().getClassName(), is(ProjectPreferencesActivity.class.getName()));
        assertThat(nextStartedActivity.requestCode, is(ApplicationConstants.RequestCodes.CHANGE_SETTINGS));
    }

    @Test
    public void onItemSelected_whenPreferences_whenRecording_showsWarning() {
        RoboMenu menu = new RoboMenu();
        formEntryMenuDelegate.onCreateOptionsMenu(Robolectric.setupActivity(FragmentActivity.class).getMenuInflater(), menu);
        formEntryMenuDelegate.onPrepareOptionsMenu(menu);

        when(audioRecorder.isRecording()).thenReturn(true);

        formEntryMenuDelegate.onOptionsItemSelected(new RoboMenuItem(R.id.menu_preferences));
        assertThat(shadowOf(activity).getNextStartedActivityForResult(), is(nullValue()));

        RecordingWarningDialogFragment dialog = getFragmentByClass(activity.getSupportFragmentManager(), RecordingWarningDialogFragment.class);
        assertThat(dialog, is(notNullValue()));
        assertThat(dialog.getDialog().isShowing(), is(true));
    }

    @Test
    public void onItemSelected_whenHierarchy_startsHierarchyActivity() {
        RoboMenu menu = new RoboMenu();
        formEntryMenuDelegate.onCreateOptionsMenu(Robolectric.setupActivity(FragmentActivity.class).getMenuInflater(), menu);
        formEntryMenuDelegate.onPrepareOptionsMenu(menu);

        formEntryMenuDelegate.onOptionsItemSelected(new RoboMenuItem(R.id.menu_goto));
        ShadowActivity.IntentForResult nextStartedActivity = shadowOf(activity).getNextStartedActivityForResult();
        assertThat(nextStartedActivity, not(nullValue()));
        assertThat(nextStartedActivity.intent.getComponent().getClassName(), is(FormHierarchyActivity.class.getName()));
        assertThat(nextStartedActivity.requestCode, is(ApplicationConstants.RequestCodes.HIERARCHY_ACTIVITY));
    }

    @Test
    public void onItemSelected_whenHierarchy_savesScreenAnswers() {
        RoboMenu menu = new RoboMenu();
        formEntryMenuDelegate.onCreateOptionsMenu(Robolectric.setupActivity(FragmentActivity.class).getMenuInflater(), menu);
        formEntryMenuDelegate.onPrepareOptionsMenu(menu);

        HashMap answers = new HashMap();
        when(answersProvider.getAnswers()).thenReturn(answers);
        formEntryMenuDelegate.onOptionsItemSelected(new RoboMenuItem(R.id.menu_goto));
        verify(formSaveViewModel).saveAnswersForScreen(answers);
    }

    @Test
    public void onItemSelected_whenHierarchy_callsOpenHierarchy() {
        RoboMenu menu = new RoboMenu();
        formEntryMenuDelegate.onCreateOptionsMenu(Robolectric.setupActivity(FragmentActivity.class).getMenuInflater(), menu);
        formEntryMenuDelegate.onPrepareOptionsMenu(menu);

        formEntryMenuDelegate.onOptionsItemSelected(new RoboMenuItem(R.id.menu_goto));
        verify(formEntryViewModel).openHierarchy();
    }

    @Test
    public void onItemSelected_whenHierarchy_whenRecording_showsWarning() {
        RoboMenu menu = new RoboMenu();
        formEntryMenuDelegate.onCreateOptionsMenu(Robolectric.setupActivity(FragmentActivity.class).getMenuInflater(), menu);
        formEntryMenuDelegate.onPrepareOptionsMenu(menu);

        when(audioRecorder.isRecording()).thenReturn(true);

        formEntryMenuDelegate.onOptionsItemSelected(new RoboMenuItem(R.id.menu_goto));
        assertThat(shadowOf(activity).getNextStartedActivity(), is(nullValue()));

        RecordingWarningDialogFragment dialog = getFragmentByClass(activity.getSupportFragmentManager(), RecordingWarningDialogFragment.class);
        assertThat(dialog, is(notNullValue()));
        assertThat(dialog.getDialog().isShowing(), is(true));
    }

    @Test
    public void onItemSelected_whenHierarchy_whenRecordingInBackground_doesNotShowWarning() {
        RoboMenu menu = new RoboMenu();
        formEntryMenuDelegate.onCreateOptionsMenu(Robolectric.setupActivity(FragmentActivity.class).getMenuInflater(), menu);
        formEntryMenuDelegate.onPrepareOptionsMenu(menu);

        when(audioRecorder.isRecording()).thenReturn(true);
        when(backgroundAudioViewModel.isBackgroundRecording()).thenReturn(true);

        formEntryMenuDelegate.onOptionsItemSelected(new RoboMenuItem(R.id.menu_goto));
        assertThat(shadowOf(activity).getNextStartedActivity(), is(notNullValue()));

        RecordingWarningDialogFragment dialog = getFragmentByClass(activity.getSupportFragmentManager(), RecordingWarningDialogFragment.class);
        assertThat(dialog, is(nullValue()));
    }

    @Test
    public void onItemSelected_whenRecordAudio_whenBackgroundRecordingDisabled_enablesBackgroundRecording_andShowsDialog() {
        RoboMenu menu = new RoboMenu();
        formEntryMenuDelegate.onCreateOptionsMenu(Robolectric.setupActivity(FragmentActivity.class).getMenuInflater(), menu);
        formEntryMenuDelegate.onPrepareOptionsMenu(menu);

        when(backgroundAudioViewModel.isBackgroundRecordingEnabled()).thenReturn(new MutableNonNullLiveData<>(false));

        formEntryMenuDelegate.onOptionsItemSelected(new RoboMenuItem(R.id.menu_record_audio));
        verify(backgroundAudioViewModel).setBackgroundRecordingEnabled(true);
    }
}
