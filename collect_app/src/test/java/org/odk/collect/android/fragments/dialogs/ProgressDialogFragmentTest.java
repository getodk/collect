package org.odk.collect.android.fragments.dialogs;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
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

    private TestActivityScenario<DialogFragmentTestActivity> activityScenario;

    @Before
    public void setup() {
        activityScenario = TestActivityScenario.launch(DialogFragmentTestActivity.class);
    }

    @Test
    public void setTitle_updatesTitle() {
        ProgressDialogFragment fragment = new ProgressDialogFragment();
        AlertDialog dialog = createAndShow(fragment);

        fragment.setTitle("blah");
        CharSequence message = shadowOf(dialog).getTitle();
        assertThat(message, equalTo("blah"));
    }

    @Test
    public void setTitle_beforeDialogExists_setsTitleWhenDialogShown() {
        ProgressDialogFragment fragment = new ProgressDialogFragment();
        fragment.setTitle("blah");

        AlertDialog dialog = createAndShow(fragment);
        CharSequence message = shadowOf(dialog).getTitle();
        assertThat(message, equalTo("blah"));
    }

    @Test
    public void restoringFragment_retainsTitle() {
        ProgressDialogFragment fragment = new ProgressDialogFragment();
        createAndShow(fragment);
        fragment.setTitle("blah");

        ProgressDialogFragment restoredFragment = new ProgressDialogFragment();
        restoredFragment.setArguments(fragment.getArguments());
        AlertDialog restoredDialog = createAndShow(restoredFragment);
        CharSequence message = shadowOf(restoredDialog).getTitle();
        assertThat(message, equalTo("blah"));
    }

    @Test
    public void whenMessageNotSet_showsProgressBar() {
        ProgressDialogFragment fragment = new ProgressDialogFragment();
        createAndShow(fragment);

        View dialogView = fragment.getDialogView();
        assertThat(dialogView.findViewById(R.id.progress_bar).getVisibility(), is(View.VISIBLE));
    }

    @Test
    public void setMessage_updatesMessage() {
        ProgressDialogFragment fragment = new ProgressDialogFragment();
        createAndShow(fragment);
        View dialogView = fragment.getDialogView();

        fragment.setMessage("blah");
        assertThat(innerText(dialogView), equalTo("blah"));
    }

    @Test
    public void setMessage_beforeDialogExists_setsMessageWhenDialogShown() {
        ProgressDialogFragment fragment = new ProgressDialogFragment();
        fragment.setMessage("blah");
        createAndShow(fragment);

        assertThat(innerText(fragment.getDialogView()), equalTo("blah"));
    }

    @Test
    public void restoringFragment_retainsMessage() {
        ProgressDialogFragment fragment = new ProgressDialogFragment();
        createAndShow(fragment);
        fragment.setMessage("blah");

        ProgressDialogFragment restoredFragment = new ProgressDialogFragment();
        restoredFragment.setArguments(fragment.getArguments());
        createAndShow(restoredFragment);
        assertThat(innerText(fragment.getDialogView()), equalTo("blah"));
    }

    @Test
    public void cancelling_callsCancelOnCancellable() {
        ProgressDialogFragment.Cancellable cancellable = mock(ProgressDialogFragment.Cancellable.class);
        ProgressDialogFragment fragment = new TestProgressDialogFragment(cancellable);

        AlertDialog dialog = createAndShow(fragment);

        fragment.onCancel(dialog);
        verify(cancellable).cancel();
    }

    @Test
    public void whenThereIsCancelButtonText_clickingCancel_dismissesAndCallsCancelOnCancellable() {
        ProgressDialogFragment.Cancellable cancellable = mock(ProgressDialogFragment.Cancellable.class);
        ProgressDialogFragment fragment = new TestProgressDialogFragment(cancellable);

        AlertDialog dialog = createAndShow(fragment);

        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).performClick();
        shadowOf(getMainLooper()).idle();

        verify(cancellable).cancel();
        assertThat(dialog.isShowing(), equalTo(false));
    }

    private AlertDialog createAndShow(ProgressDialogFragment fragment) {
        activityScenario.onActivity(activity -> {
            fragment.show(activity.getSupportFragmentManager(), "TAG");
            shadowOf(getMainLooper()).idle();
        });

        return (AlertDialog) fragment.getDialog();
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

    private static class DialogFragmentTestActivity extends FragmentActivity {

        @Override
        protected void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setTheme(R.style.Theme_AppCompat); // Needed for androidx.appcompat.app.AlertDialog
        }
    }
}