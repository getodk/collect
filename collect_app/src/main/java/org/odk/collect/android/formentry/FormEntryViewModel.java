package org.odk.collect.android.formentry;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import org.javarosa.core.model.FormIndex;
import org.odk.collect.android.analytics.Analytics;
import org.odk.collect.android.formentry.javarosawrapper.FormController;

public class FormEntryViewModel extends ViewModel {

    private final FormControllerProvider formControllerProvider;
    private final Analytics analytics;
    private final MutableLiveData<ViewUpdate> updates = new MutableLiveData<>(null);

    @Nullable
    private FormIndex jumpBackIndex;

    public FormEntryViewModel(FormControllerProvider formControllerProvider, Analytics analytics) {
        this.formControllerProvider = formControllerProvider;
        this.analytics = analytics;
    }

    public LiveData<ViewUpdate> getUpdates() {
        return updates;
    }

    public void promptForNewRepeat() {
        FormIndex index = getFormController().getFormIndex();
        jumpBackIndex = index;

        getFormController().jumpToNewRepeatPrompt();
        updates.setValue(ViewUpdate.REFRESH);
    }

    public void addRepeat(boolean fromPrompt) {
        if (jumpBackIndex != null) {
            jumpBackIndex = null;
            analytics.logEvent("AddRepeat", "Inline");
        } else if (fromPrompt) {
            analytics.logEvent("AddRepeat", "Prompt");
        } else {
            analytics.logEvent("AddRepeat", "Hierarchy");
        }

        getFormController().newRepeat();
    }

    public void cancelRepeatPrompt() {
        analytics.logEvent("AddRepeat", "InlineDecline");

        FormController formController = formControllerProvider.getFormController();

        if (jumpBackIndex != null) {
            formController.jumpToIndex(jumpBackIndex);
            jumpBackIndex = null;
            updates.setValue(ViewUpdate.REFRESH);
        } else {
            updates.setValue(ViewUpdate.SHOW_NEXT);
        }
    }

    public enum ViewUpdate {
        REFRESH,
        SHOW_NEXT
    }

    private FormController getFormController() {
        return formControllerProvider.getFormController();
    }

    public static class Factory implements ViewModelProvider.Factory {

        private final FormControllerProvider formControllerProvider;
        private final Analytics analytics;

        public Factory(FormControllerProvider formControllerProvider, Analytics analytics) {
            this.formControllerProvider = formControllerProvider;
            this.analytics = analytics;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new FormEntryViewModel(formControllerProvider, analytics);
        }
    }
}
