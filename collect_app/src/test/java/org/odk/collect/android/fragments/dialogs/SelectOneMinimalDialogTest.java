package org.odk.collect.android.fragments.dialogs;

import androidx.test.core.app.ApplicationProvider;

import org.javarosa.core.model.SelectChoice;
import org.junit.Test;
import org.odk.collect.testshared.RobolectricHelpers;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class SelectOneMinimalDialogTest extends SelectMinimalDialogTest {
    @Test
    public void whenClickBackButton_shouldAnswerBeSavedOnlyIfChanged() {
        List<SelectChoice> items = getTestChoices();
        setUpFormEntryPrompt(items, "autocomplete");
        dialogFragment = new SelectOneMinimalDialog("AAA", false, true, ApplicationProvider.getApplicationContext(), items, formEntryPrompt, null, 0, 1, false);
        SelectMinimalDialog.SelectMinimalDialogListener listener = mock(SelectMinimalDialog.SelectMinimalDialogListener.class);
        dialogFragment.setListener(listener);

        dialogFragment.show(fragmentManager, "TAG");
        RobolectricHelpers.runLooper();
        dialogFragment.onBackPressed();
        verify(listener, times(0)).updateSelectedItems(anyList());
        dialogFragment.show(fragmentManager, "TAG");
        dialogFragment.adapter.clearAnswer();
        dialogFragment.onBackPressed();
        verify(listener, times(1)).updateSelectedItems(anyList());
    }

    @Test
    public void whenClickBackArrowButton_shouldAnswerBeSavedOnlyIfChanged() {
        List<SelectChoice> items = getTestChoices();
        setUpFormEntryPrompt(items, "autocomplete");
        dialogFragment = new SelectOneMinimalDialog("AAA", false, true, ApplicationProvider.getApplicationContext(), items, formEntryPrompt, null, 0, 1, false);
        SelectMinimalDialog.SelectMinimalDialogListener listener = mock(SelectMinimalDialog.SelectMinimalDialogListener.class);
        dialogFragment.setListener(listener);

        dialogFragment.show(fragmentManager, "TAG");
        RobolectricHelpers.runLooper();

        dialogFragment.getToolbar().getChildAt(0).performClick();
        verify(listener, times(0)).updateSelectedItems(anyList());
        dialogFragment.show(fragmentManager, "TAG");
        dialogFragment.adapter.clearAnswer();
        dialogFragment.getToolbar().getChildAt(0).performClick();
        verify(listener, times(1)).updateSelectedItems(anyList());
    }
}
