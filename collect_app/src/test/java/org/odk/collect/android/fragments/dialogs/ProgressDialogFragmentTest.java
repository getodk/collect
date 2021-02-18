package org.odk.collect.android.fragments.dialogs;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.fragments.support.DialogFragmentHelpers;
import org.odk.collect.android.support.TestActivityScenario;
import org.robolectric.annotation.LooperMode;

import static android.os.Looper.getMainLooper;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.annotation.LooperMode.Mode.PAUSED;
import static org.robolectric.shadows.ShadowView.innerText;

@RunWith(AndroidJUnit4.class)
@LooperMode(PAUSED)
public class ProgressDialogFragmentTest {

    private TestActivityScenario<DialogFragmentHelpers.DialogFragmentTestActivity> activityScenario;

    @Before
    public void setup() {
        activityScenario = TestActivityScenario.launch(DialogFragmentHelpers.DialogFragmentTestActivity.class);
    }

    @Test
    public void setTitle_updatesTitle() {
        activityScenario.onActivity(activity -> {
            ProgressDialogFragment fragment = new ProgressDialogFragment();

            fragment.show(activity.getSupportFragmentManager(), "TAG");
            shadowOf(getMainLooper()).idle();

            fragment.setTitle("blah");
            CharSequence message = shadowOf(fragment.getDialog()).getTitle();
            assertThat(message, equalTo("blah"));
        });
    }

    @Test
    public void setTitle_beforeDialogExists_setsTitleWhenDialogShown() {
        activityScenario.onActivity(activity -> {
            ProgressDialogFragment fragment = new ProgressDialogFragment();
            fragment.setTitle("blah");

            fragment.show(activity.getSupportFragmentManager(), "TAG");
            shadowOf(getMainLooper()).idle();

            CharSequence message = shadowOf(fragment.getDialog()).getTitle();
            assertThat(message, equalTo("blah"));
        });
    }

    @Test
    public void whenMessageNotSet_showsProgressBar() {
        activityScenario.onActivity(activity -> {
            ProgressDialogFragment fragment = new ProgressDialogFragment();

            fragment.show(activity.getSupportFragmentManager(), "TAG");
            shadowOf(getMainLooper()).idle();

            View dialogView = fragment.getDialogView();
            assertThat(dialogView.findViewById(R.id.progress_bar).getVisibility(), is(View.VISIBLE));
        });
    }

    @Test
    public void setMessage_updatesMessage() {
        activityScenario.onActivity(activity -> {
            ProgressDialogFragment fragment = new ProgressDialogFragment();

            fragment.show(activity.getSupportFragmentManager(), "TAG");
            shadowOf(getMainLooper()).idle();

            View dialogView = fragment.getDialogView();

            fragment.setMessage("blah");
            assertThat(innerText(dialogView), equalTo("blah"));
        });
    }

    @Test
    public void setMessage_beforeDialogExists_setsMessageWhenDialogShown() {
        activityScenario.onActivity(activity -> {
            ProgressDialogFragment fragment = new ProgressDialogFragment();
            fragment.setMessage("blah");

            fragment.show(activity.getSupportFragmentManager(), "TAG");
            shadowOf(getMainLooper()).idle();

            assertThat(innerText(fragment.getDialogView()), equalTo("blah"));
        });
    }

    @Test
    public void setCancellable_makesTheDialogNotCancellable() {
        activityScenario.onActivity(activity -> {
            ProgressDialogFragment fragment = new ProgressDialogFragment();

            Bundle args = new Bundle();
            args.putBoolean(ProgressDialogFragment.CANCELABLE, false);

            fragment.setArguments(args);
            fragment.show(activity.getSupportFragmentManager(), "TAG");
            shadowOf(getMainLooper()).idle();

            assertThat(shadowOf(fragment.getDialog()).isCancelable(), equalTo(false));
        });
    }

    @Test
    public void cancelling_callsCancelOnCancellable() {
        activityScenario.onActivity(activity -> {
            ProgressDialogFragment.Cancellable cancellable = mock(ProgressDialogFragment.Cancellable.class);
            ProgressDialogFragment fragment = new TestProgressDialogFragment(cancellable);

            fragment.show(activity.getSupportFragmentManager(), "TAG");
            shadowOf(getMainLooper()).idle();

            AlertDialog dialog = (AlertDialog) fragment.getDialog();

            fragment.onCancel(dialog);
            verify(cancellable).cancel();
        });
    }

    @Test
    public void whenThereIsCancelButtonText_clickingCancel_dismissesAndCallsCancelOnCancellable() {
        activityScenario.onActivity(activity -> {
            ProgressDialogFragment.Cancellable cancellable = mock(ProgressDialogFragment.Cancellable.class);
            ProgressDialogFragment fragment = new TestProgressDialogFragment(cancellable);

            fragment.show(activity.getSupportFragmentManager(), "TAG");
            shadowOf(getMainLooper()).idle();

            AlertDialog dialog = (AlertDialog) fragment.getDialog();

            dialog.getButton(DialogInterface.BUTTON_NEGATIVE).performClick();
            shadowOf(getMainLooper()).idle();

            verify(cancellable).cancel();
            assertThat(dialog.isShowing(), equalTo(false));
        });
    }

    @Test
    public void whenActivityIsRecreated_titleAndMessageAreRetained() {
        activityScenario.onActivity(activity -> {
            ProgressDialogFragment fragment = new ProgressDialogFragment();
            fragment.show(activity.getSupportFragmentManager(), "TAG");
            fragment.setTitle("I AM TITLE");
            fragment.setMessage("I AM MESSAGE");
        });

        activityScenario.recreate();

        activityScenario.onActivity(activity -> {
            ProgressDialogFragment fragment = (ProgressDialogFragment) activity.getSupportFragmentManager().findFragmentByTag("TAG");
            assertThat(shadowOf(fragment.getDialog()).getTitle(), equalTo("I AM TITLE"));
            assertThat(innerText(fragment.getDialogView()), equalTo("I AM MESSAGE"));
        });
    }

    public static class TestProgressDialogFragment extends ProgressDialogFragment {
        private final Cancellable cancellable;

        TestProgressDialogFragment(Cancellable cancellable) {
            this.cancellable = cancellable;
        }

        @Override
        protected String getCancelButtonText() {
            return "Blah";
        }

        @Override
        protected Cancellable getCancellable() {
            return cancellable;
        }
    }
}