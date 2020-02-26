package org.odk.collect.android.formentry;

import android.app.Application;

import androidx.fragment.app.FragmentActivity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.formentry.javarosawrapper.FormController;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.fakes.RoboMenu;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class FormEntryMenuDelegateTest {

    private FormEntryMenuDelegate formEntryMenuDelegate;
    private FormController formController;
    private Application context;

    @Before
    public void setup() {
        context = RuntimeEnvironment.application;
        formController = mock(FormController.class);
        formEntryMenuDelegate = new FormEntryMenuDelegate(context, () -> formController);
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
        formEntryMenuDelegate = new FormEntryMenuDelegate(context, () -> null);

        RoboMenu menu = new RoboMenu();
        formEntryMenuDelegate.onCreate(Robolectric.setupActivity(FragmentActivity.class).getMenuInflater(), menu);
        formEntryMenuDelegate.onPrepare(menu);

        assertThat(menu.findItem(R.id.menu_add_repeat).isVisible(), equalTo(false));
    }
}