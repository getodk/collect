package org.odk.collect.android.formentry

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.javarosa.core.model.FormIndex
import org.javarosa.core.model.data.IAnswerData
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.odk.collect.android.R
import org.odk.collect.android.TestSettingsProvider.getSettingsProvider
import org.odk.collect.android.activities.FormHierarchyActivity
import org.odk.collect.android.formentry.FormEntryMenuProvider.FormEntryMenuClickListener
import org.odk.collect.android.formentry.backgroundlocation.BackgroundLocationViewModel
import org.odk.collect.android.formentry.questions.AnswersProvider
import org.odk.collect.android.javarosawrapper.FormController
import org.odk.collect.android.preferences.screens.ProjectPreferencesActivity
import org.odk.collect.android.utilities.ApplicationConstants
import org.odk.collect.androidshared.livedata.MutableNonNullLiveData
import org.odk.collect.audiorecorder.recording.AudioRecorder
import org.odk.collect.settings.keys.ProtectedProjectKeys
import org.odk.collect.testshared.RobolectricHelpers.createThemedActivity
import org.odk.collect.testshared.RobolectricHelpers.getFragmentByClass
import org.robolectric.Robolectric
import org.robolectric.Shadows
import org.robolectric.annotation.LooperMode
import org.robolectric.fakes.RoboMenu
import org.robolectric.fakes.RoboMenuItem

@RunWith(AndroidJUnit4::class)
@LooperMode(LooperMode.Mode.PAUSED)
class FormEntryMenuProviderTest {
    private val activity = createThemedActivity(AppCompatActivity::class.java)
    private val formController = mock<FormController>()
    private val formEntryViewModel = mock<FormEntryViewModel>().apply {
        whenever(hasBackgroundRecording()).thenReturn(MutableNonNullLiveData(false))
        whenever(sessionId).thenReturn("blah")
        whenever(formController).thenReturn(this@FormEntryMenuProviderTest.formController)
    }
    private val answersProvider = mock<AnswersProvider>()
    private val audioRecorder = mock<AudioRecorder>().apply {
        whenever(isRecording()).thenReturn(false)
    }
    private val backgroundAudioViewModel = mock<BackgroundAudioViewModel>().apply {
        whenever(isBackgroundRecordingEnabled).thenReturn(MutableNonNullLiveData(true))
    }
    private val settingsProvider = getSettingsProvider()
    private val formEntryMenuClickListener = mock<FormEntryMenuClickListener>()
    private val backgroundLocationViewModel = mock<BackgroundLocationViewModel>()
    private val formEntryMenuProvider = FormEntryMenuProvider(
        activity,
        answersProvider,
        formEntryViewModel,
        audioRecorder,
        backgroundLocationViewModel,
        backgroundAudioViewModel,
        settingsProvider,
        formEntryMenuClickListener
    )

    @Test
    fun onPrepare_inRepeatQuestion_showsAddRepeat() {
        whenever(formEntryViewModel.canAddRepeat()).thenReturn(true)

        val menu = RoboMenu()
        formEntryMenuProvider.onCreateMenu(menu, Robolectric.setupActivity(FragmentActivity::class.java).menuInflater)
        formEntryMenuProvider.onPrepareMenu(menu)

        assertThat(menu.findItem(R.id.menu_add_repeat).isVisible, equalTo(true))
    }

    @Test
    fun onPrepare_notInRepeatQuestion_hidesAddRepeat() {
        whenever(formEntryViewModel.canAddRepeat()).thenReturn(false)

        val menu = RoboMenu()
        formEntryMenuProvider.onCreateMenu(menu, Robolectric.setupActivity(FragmentActivity::class.java).menuInflater)
        formEntryMenuProvider.onPrepareMenu(menu)

        assertThat(menu.findItem(R.id.menu_add_repeat).isVisible, equalTo(false))
    }

    @Test
    fun onPrepare_whenFormControllerIsNull_hidesAddRepeat() {
        whenever(formEntryViewModel.formController).thenReturn(null)

        val menu = RoboMenu()
        formEntryMenuProvider.onCreateMenu(menu, Robolectric.setupActivity(FragmentActivity::class.java).menuInflater)
        formEntryMenuProvider.onPrepareMenu(menu)

        assertThat(menu.findItem(R.id.menu_add_repeat).isVisible, equalTo(false))
    }

    @Test
    fun onPrepare_whenFormHasBackgroundRecording_showsRecordAudio() {
        whenever(formEntryViewModel.hasBackgroundRecording()).thenReturn(MutableNonNullLiveData(true))

        val menu = RoboMenu()
        formEntryMenuProvider.onCreateMenu(menu, Robolectric.setupActivity(FragmentActivity::class.java).menuInflater)
        formEntryMenuProvider.onPrepareMenu(menu)

        assertThat(menu.findItem(R.id.menu_record_audio).isVisible, equalTo(true))
    }

    @Test
    fun onPrepare_whenBackgroundRecodingEnabled_checksRecordAudio() {
        whenever(backgroundAudioViewModel.isBackgroundRecordingEnabled).thenReturn(MutableNonNullLiveData(true))

        val menu = RoboMenu()
        formEntryMenuProvider.onCreateMenu(menu, Robolectric.setupActivity(FragmentActivity::class.java).menuInflater)
        formEntryMenuProvider.onPrepareMenu(menu)

        assertThat(menu.findItem(R.id.menu_record_audio).isChecked, equalTo(true))
    }

    @Test
    fun onPrepare_whenNotRecordingInBackground_unchecksRecordAudio() {
        whenever(backgroundAudioViewModel.isBackgroundRecordingEnabled).thenReturn(MutableNonNullLiveData(false))

        val menu = RoboMenu()
        formEntryMenuProvider.onCreateMenu(menu, Robolectric.setupActivity(FragmentActivity::class.java).menuInflater)
        formEntryMenuProvider.onPrepareMenu(menu)

        assertThat(menu.findItem(R.id.menu_record_audio).isChecked, equalTo(false))
    }

    @Test
    fun onPrepare_whenFormDoesNotHaveBackgroundRecording_hidesRecordAudio() {
        whenever(formEntryViewModel.hasBackgroundRecording()).thenReturn(MutableNonNullLiveData(false))

        val menu = RoboMenu()
        formEntryMenuProvider.onCreateMenu(menu, Robolectric.setupActivity(FragmentActivity::class.java).menuInflater)
        formEntryMenuProvider.onPrepareMenu(menu)

        assertThat(menu.findItem(R.id.menu_record_audio).isVisible, equalTo(false))
    }

    @Test
    fun onPrepare_whenSavingFormInTheMiddleEnabled_showsSave() {
        settingsProvider.getProtectedSettings().save(ProtectedProjectKeys.KEY_SAVE_MID, true)

        val menu = RoboMenu()
        formEntryMenuProvider.onCreateMenu(menu, Robolectric.setupActivity(FragmentActivity::class.java).menuInflater)
        formEntryMenuProvider.onPrepareMenu(menu)

        assertThat(menu.findItem(R.id.menu_save).isVisible, equalTo(true))
    }

    @Test
    fun onPrepare_whenSavingFormInTheMiddleDisabled_hidesSave() {
        settingsProvider.getProtectedSettings().save(ProtectedProjectKeys.KEY_SAVE_MID, false)

        val menu = RoboMenu()
        formEntryMenuProvider.onCreateMenu(menu, Robolectric.setupActivity(FragmentActivity::class.java).menuInflater)
        formEntryMenuProvider.onPrepareMenu(menu)

        assertThat(menu.findItem(R.id.menu_save).isVisible, equalTo(false))
    }

    @Test
    fun onPrepare_whenThereAreMoreThanOneDefinedLanguages_showsChangeLanguage() {
        whenever(formController.getLanguages()).thenReturn(arrayOf("English", "Spanish"))

        val menu = RoboMenu()
        formEntryMenuProvider.onCreateMenu(menu, Robolectric.setupActivity(FragmentActivity::class.java).menuInflater)
        formEntryMenuProvider.onPrepareMenu(menu)

        assertThat(menu.findItem(R.id.menu_languages).isVisible, equalTo(true))
    }

    @Test
    fun onPrepare_whenThereIsJustOneDefinedLanguage_hidesChangeLanguage() {
        val menu = RoboMenu()
        formEntryMenuProvider.onCreateMenu(menu, Robolectric.setupActivity(FragmentActivity::class.java).menuInflater)
        formEntryMenuProvider.onPrepareMenu(menu)

        assertThat(menu.findItem(R.id.menu_languages).isVisible, equalTo(false))
    }

    @Test
    fun onItemSelected_whenAddRepeat_callsPromptForNewRepeat() {
        val menu = RoboMenu()
        formEntryMenuProvider.onCreateMenu(menu, Robolectric.setupActivity(FragmentActivity::class.java).menuInflater)
        formEntryMenuProvider.onPrepareMenu(menu)
        formEntryMenuProvider.onMenuItemSelected(RoboMenuItem(R.id.menu_add_repeat))

        verify(formEntryViewModel).promptForNewRepeat()
    }

    @Test
    fun onItemSelected_whenAddRepeat_savesScreenAnswers() {
        val answers: HashMap<FormIndex, IAnswerData> = HashMap()
        whenever(answersProvider.answers).thenReturn(answers)

        val menu = RoboMenu()
        formEntryMenuProvider.onCreateMenu(menu, Robolectric.setupActivity(FragmentActivity::class.java).menuInflater)
        formEntryMenuProvider.onPrepareMenu(menu)
        formEntryMenuProvider.onMenuItemSelected(RoboMenuItem(R.id.menu_add_repeat))

        verify(formEntryViewModel).updateAnswersForScreen(answers, false)
    }

    @Test
    fun onItemSelected_whenAddRepeat_whenRecording_showsWarning() {
        whenever(audioRecorder.isRecording()).thenReturn(true)

        val menu = RoboMenu()
        formEntryMenuProvider.onCreateMenu(menu, Robolectric.setupActivity(FragmentActivity::class.java).menuInflater)
        formEntryMenuProvider.onPrepareMenu(menu)
        formEntryMenuProvider.onMenuItemSelected(RoboMenuItem(R.id.menu_add_repeat))

        verify(formEntryViewModel, never()).promptForNewRepeat()
        val dialog = getFragmentByClass(activity.supportFragmentManager, RecordingWarningDialogFragment::class.java)
        assertThat(dialog, notNullValue())
        assertThat(dialog!!.dialog!!.isShowing, equalTo(true))
    }

    @Test
    fun onItemSelected_whenAddRepeat_whenRecordingInTheBackground_doesNotShowWarning() {
        whenever(audioRecorder.isRecording()).thenReturn(true)
        whenever(backgroundAudioViewModel.isBackgroundRecording).thenReturn(true)

        val menu = RoboMenu()
        formEntryMenuProvider.onCreateMenu(menu, Robolectric.setupActivity(FragmentActivity::class.java).menuInflater)
        formEntryMenuProvider.onPrepareMenu(menu)
        formEntryMenuProvider.onMenuItemSelected(RoboMenuItem(R.id.menu_add_repeat))

        verify(formEntryViewModel).promptForNewRepeat()
        val dialog = getFragmentByClass(activity.supportFragmentManager, RecordingWarningDialogFragment::class.java)
        assertThat(dialog, nullValue())
    }

    @Test
    fun onItemSelected_whenPreferences_startsPreferencesActivityWithChangeSettingsRequest() {
        val menu = RoboMenu()
        formEntryMenuProvider.onCreateMenu(menu, Robolectric.setupActivity(FragmentActivity::class.java).menuInflater)
        formEntryMenuProvider.onPrepareMenu(menu)
        formEntryMenuProvider.onMenuItemSelected(RoboMenuItem(R.id.menu_preferences))
        val nextStartedActivity = Shadows.shadowOf(activity).nextStartedActivityForResult

        assertThat(nextStartedActivity, notNullValue())
        assertThat(nextStartedActivity.intent.component!!.className, equalTo(ProjectPreferencesActivity::class.java.name))
        assertThat(nextStartedActivity.requestCode, equalTo(ApplicationConstants.RequestCodes.CHANGE_SETTINGS))
    }

    @Test
    fun onItemSelected_whenPreferences_whenRecording_showsWarning() {
        whenever(audioRecorder.isRecording()).thenReturn(true)

        val menu = RoboMenu()
        formEntryMenuProvider.onCreateMenu(menu, Robolectric.setupActivity(FragmentActivity::class.java).menuInflater)
        formEntryMenuProvider.onPrepareMenu(menu)
        formEntryMenuProvider.onMenuItemSelected(RoboMenuItem(R.id.menu_preferences))

        assertThat(Shadows.shadowOf(activity).nextStartedActivityForResult, nullValue())
        val dialog = getFragmentByClass(activity.supportFragmentManager, RecordingWarningDialogFragment::class.java)
        assertThat(dialog, notNullValue())
        assertThat(dialog!!.dialog!!.isShowing, equalTo(true))
    }

    @Test
    fun onItemSelected_whenHierarchy_startsHierarchyActivity() {
        val menu = RoboMenu()
        formEntryMenuProvider.onCreateMenu(menu, Robolectric.setupActivity(FragmentActivity::class.java).menuInflater)
        formEntryMenuProvider.onPrepareMenu(menu)
        formEntryMenuProvider.onMenuItemSelected(RoboMenuItem(R.id.menu_goto))
        val nextStartedActivity = Shadows.shadowOf(activity).nextStartedActivityForResult

        assertThat(nextStartedActivity, notNullValue())
        assertThat(nextStartedActivity.intent.component!!.className, equalTo(FormHierarchyActivity::class.java.name))
        assertThat(nextStartedActivity.requestCode, equalTo(ApplicationConstants.RequestCodes.HIERARCHY_ACTIVITY))
    }

    @Test
    fun onItemSelected_whenHierarchy_savesScreenAnswers() {
        val answers: HashMap<FormIndex, IAnswerData> = HashMap()
        whenever(answersProvider.answers).thenReturn(answers)

        val menu = RoboMenu()
        formEntryMenuProvider.onCreateMenu(menu, Robolectric.setupActivity(FragmentActivity::class.java).menuInflater)
        formEntryMenuProvider.onPrepareMenu(menu)
        formEntryMenuProvider.onMenuItemSelected(RoboMenuItem(R.id.menu_goto))

        verify(formEntryViewModel).updateAnswersForScreen(answers, false)
    }

    @Test
    fun onItemSelected_whenHierarchy_callsOpenHierarchy() {
        val menu = RoboMenu()
        formEntryMenuProvider.onCreateMenu(menu, Robolectric.setupActivity(FragmentActivity::class.java).menuInflater)
        formEntryMenuProvider.onPrepareMenu(menu)
        formEntryMenuProvider.onMenuItemSelected(RoboMenuItem(R.id.menu_goto))

        verify(formEntryViewModel).openHierarchy()
    }

    @Test
    fun onItemSelected_whenHierarchy_whenRecording_showsWarning() {
        whenever(audioRecorder.isRecording()).thenReturn(true)

        val menu = RoboMenu()
        formEntryMenuProvider.onCreateMenu(menu, Robolectric.setupActivity(FragmentActivity::class.java).menuInflater)
        formEntryMenuProvider.onPrepareMenu(menu)
        formEntryMenuProvider.onMenuItemSelected(RoboMenuItem(R.id.menu_goto))

        assertThat(Shadows.shadowOf(activity).nextStartedActivity, nullValue())
        val dialog = getFragmentByClass(activity.supportFragmentManager, RecordingWarningDialogFragment::class.java)
        assertThat(dialog, notNullValue())
        assertThat(dialog!!.dialog!!.isShowing, equalTo(true))
    }

    @Test
    fun onItemSelected_whenHierarchy_whenRecordingInBackground_doesNotShowWarning() {
        whenever(audioRecorder.isRecording()).thenReturn(true)
        whenever(backgroundAudioViewModel.isBackgroundRecording).thenReturn(true)

        val menu = RoboMenu()
        formEntryMenuProvider.onCreateMenu(menu, Robolectric.setupActivity(FragmentActivity::class.java).menuInflater)
        formEntryMenuProvider.onPrepareMenu(menu)
        formEntryMenuProvider.onMenuItemSelected(RoboMenuItem(R.id.menu_goto))

        assertThat(Shadows.shadowOf(activity).nextStartedActivity, notNullValue())
        val dialog = getFragmentByClass(activity.supportFragmentManager, RecordingWarningDialogFragment::class.java)
        assertThat(dialog, nullValue())
    }

    @Test
    fun onItemSelected_whenRecordAudio_whenBackgroundRecordingDisabled_enablesBackgroundRecording_andShowsDialog() {
        whenever(backgroundAudioViewModel.isBackgroundRecordingEnabled).thenReturn(MutableNonNullLiveData(false))

        val menu = RoboMenu()
        formEntryMenuProvider.onCreateMenu(menu, Robolectric.setupActivity(FragmentActivity::class.java).menuInflater)
        formEntryMenuProvider.onPrepareMenu(menu)
        formEntryMenuProvider.onMenuItemSelected(RoboMenuItem(R.id.menu_record_audio))

        verify(backgroundAudioViewModel).setBackgroundRecordingEnabled(true)
    }

    @Test
    fun onItemSelected_whenChangeLanguage_callsChangeLanguage() {
        val menu = RoboMenu()
        formEntryMenuProvider.onCreateMenu(menu, Robolectric.setupActivity(FragmentActivity::class.java).menuInflater)
        formEntryMenuProvider.onPrepareMenu(menu)
        formEntryMenuProvider.onMenuItemSelected(RoboMenuItem(R.id.menu_languages))

        verify(formEntryMenuClickListener).changeLanguage()
    }

    @Test
    fun onItemSelected_whenSave_callsSave() {
        val menu = RoboMenu()
        formEntryMenuProvider.onCreateMenu(menu, Robolectric.setupActivity(FragmentActivity::class.java).menuInflater)
        formEntryMenuProvider.onPrepareMenu(menu)
        formEntryMenuProvider.onMenuItemSelected(RoboMenuItem(R.id.menu_save))

        verify(formEntryMenuClickListener).save()
    }
}
