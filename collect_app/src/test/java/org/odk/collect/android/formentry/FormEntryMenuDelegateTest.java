package org.odk.collect.android.formentry;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.formentry.javarosawrapper.FormController;
import org.odk.collect.android.formentry.questions.AnswersProvider;
import org.odk.collect.android.formentry.saving.FormSaveViewModel;
import org.odk.collect.android.support.RobolectricHelpers;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.fakes.RoboMenu;
import org.robolectric.fakes.RoboMenuItem;

import java.util.HashMap;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.support.RobolectricHelpers.mockViewModelProvider;

@RunWith(RobolectricTestRunner.class)
public class FormEntryMenuDelegateTest {

    private FormEntryMenuDelegate formEntryMenuDelegate;
    private FormController formController;
    private AppCompatActivity activity;
    private FormEntryViewModel formEntryViewModel;
    private AnswersProvider answersProvider;
    private FormSaveViewModel formSaveViewModel;

    @Before
    public void setup() {
        activity = RobolectricHelpers.createThemedActivity(AppCompatActivity.class, R.style.Theme_AppCompat);
        formController = mock(FormController.class);
        answersProvider = mock(AnswersProvider.class);
        formEntryViewModel = mockViewModelProvider(activity, FormEntryViewModel.class).get(FormEntryViewModel.class);

        formEntryMenuDelegate = new FormEntryMenuDelegate(activity, () -> formController, answersProvider, mock(FormIndexAnimationHandler.class));
    }

    @Test
    public void onPrepare_inRepeatQuestion_showsAddRepeat() {
        when(formController.indexContainsRepeatableGroup()).thenReturn(true);

        RoboMenu menu = new RoboMenu();
        formEntryMenuDelegate.onCreate(Robolectric.setupActivity(FragmentActivity.class).getMenuInflater(), menu);
        formEntryMenuDelegate.onPrepare(menu);

        assertThat(menu.findItem(R.id.menu_add_repeat).isVisible(), equalTo(true));
    }

    @Test
    public void onPrepare_notInRepeatQuestion_hidesAddRepeat() {
        when(formController.indexContainsRepeatableGroup()).thenReturn(false);

        RoboMenu menu = new RoboMenu();
        formEntryMenuDelegate.onCreate(Robolectric.setupActivity(FragmentActivity.class).getMenuInflater(), menu);
        formEntryMenuDelegate.onPrepare(menu);

        assertThat(menu.findItem(R.id.menu_add_repeat).isVisible(), equalTo(false));
    }

    @Test
    public void onPrepare_whenFormControllerIsNull_hidesAddRepeat() {
        formEntryMenuDelegate = new FormEntryMenuDelegate(activity, () -> null, answersProvider, mock(FormIndexAnimationHandler.class));

        RoboMenu menu = new RoboMenu();
        formEntryMenuDelegate.onCreate(Robolectric.setupActivity(FragmentActivity.class).getMenuInflater(), menu);
        formEntryMenuDelegate.onPrepare(menu);

        assertThat(menu.findItem(R.id.menu_add_repeat).isVisible(), equalTo(false));
    }

    @Test
    public void onItemSelected_whenAddRepeat_callsPromptForNewRepeat() {
        RoboMenu menu = new RoboMenu();
        formEntryMenuDelegate.onCreate(Robolectric.setupActivity(FragmentActivity.class).getMenuInflater(), menu);
        formEntryMenuDelegate.onPrepare(menu);

        formEntryMenuDelegate.onItemSelected(new RoboMenuItem(R.id.menu_add_repeat));
        verify(formEntryViewModel).promptForNewRepeat();
    }

    @Test
    public void onItemSelected_whenAddRepeat_savesScreenAnswers() {
        RoboMenu menu = new RoboMenu();
        formEntryMenuDelegate.onCreate(Robolectric.setupActivity(FragmentActivity.class).getMenuInflater(), menu);
        formEntryMenuDelegate.onPrepare(menu);

        HashMap answers = new HashMap();
        when(answersProvider.getAnswers()).thenReturn(answers);
        formEntryMenuDelegate.onItemSelected(new RoboMenuItem(R.id.menu_add_repeat));
        verify(formSaveViewModel).saveAnswersForScreen(answers);
    }
}