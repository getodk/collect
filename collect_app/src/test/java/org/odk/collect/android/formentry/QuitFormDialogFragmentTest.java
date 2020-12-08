package org.odk.collect.android.formentry;

import android.content.DialogInterface;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.analytics.Analytics;
import org.odk.collect.android.formentry.saving.FormSaveViewModel;
import org.odk.collect.android.injection.config.AppDependencyModule;
import org.odk.collect.android.support.RobolectricHelpers;
import org.odk.collect.async.Scheduler;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowDialog;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
public class QuitFormDialogFragmentTest {

    private final FormSaveViewModel formSaveViewModel = mock(FormSaveViewModel.class);

    private FragmentActivity activity;
    private FragmentManager fragmentManager;
    private QuitFormDialogFragment dialogFragment;

    @Before
    public void setup() {
        activity = RobolectricHelpers.createThemedActivity(FragmentActivity.class);
        fragmentManager = activity.getSupportFragmentManager();
        dialogFragment = new QuitFormDialogFragment();

        RobolectricHelpers.overrideAppDependencyModule(new AppDependencyModule() {
            @Override
            public FormSaveViewModel.FactoryFactory providesFormSaveViewModelFactoryFactory(Analytics analytics, Scheduler scheduler) {
                return (owner, defaultArgs) -> new ViewModelProvider.Factory() {

                    @NonNull
                    @Override
                    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
                        return (T) formSaveViewModel;
                    }
                };
            }
        });
    }

    @Test
    public void shouldShowCorrectButtons() {
        dialogFragment.show(fragmentManager, "TAG");
        AlertDialog dialog = (AlertDialog) ShadowDialog.getLatestDialog();

        assertNotNull(dialog);
        assertThat(dialog.getButton(DialogInterface.BUTTON_POSITIVE).getVisibility(), equalTo(GONE));
        assertThat(dialog.getButton(DialogInterface.BUTTON_NEGATIVE).getVisibility(), equalTo(VISIBLE));
        assertThat(dialog.getButton(DialogInterface.BUTTON_NEGATIVE).getText(),
                equalTo(activity.getString(R.string.do_not_exit)));
    }

    @Test
    public void shouldShowCorrectTitle_whenNoFormIsLoaded() {
        dialogFragment.show(fragmentManager, "TAG");
        AlertDialog dialog = (AlertDialog) ShadowDialog.getLatestDialog();
        TextView dialogTitle = dialog.findViewById(R.id.alertTitle);

        assertThat(dialogTitle.getText().toString(), equalTo(activity.getString(R.string.quit_application, activity.getString(R.string.no_form_loaded))));
    }


    @Test
    public void shouldShowCorrectTitle_whenFormIsLoaded() {
        when(formSaveViewModel.getFormName()).thenReturn("blah");

        dialogFragment.show(fragmentManager, "TAG");
        AlertDialog dialog = (AlertDialog) ShadowDialog.getLatestDialog();
        TextView dialogTitle = dialog.findViewById(R.id.alertTitle);

        assertThat(dialogTitle.getText().toString(), equalTo(activity.getString(R.string.quit_application, "blah")));
    }

    @Test
    public void dialogIsCancellable() {
        dialogFragment.show(fragmentManager, "tag");
        assertThat(shadowOf(dialogFragment.getDialog()).isCancelable(), equalTo(true));
    }

    @Test
    public void clickingCancel_shouldDismissTheDialog() {
        dialogFragment.show(fragmentManager, "tag");
        AlertDialog dialog = (AlertDialog) ShadowDialog.getLatestDialog();
        assertTrue(dialog.isShowing());

        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).performClick();
        assertFalse(dialog.isShowing());
        assertTrue(shadowOf(dialog).hasBeenDismissed());
    }
}
