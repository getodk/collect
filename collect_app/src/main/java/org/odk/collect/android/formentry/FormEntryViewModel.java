package org.odk.collect.android.formentry;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import org.javarosa.core.model.FormIndex;
import org.odk.collect.android.analytics.Analytics;
import org.odk.collect.android.exception.JavaRosaException;
import org.odk.collect.android.formentry.javarosawrapper.FormController;

public class FormEntryViewModel extends ViewModel {

    private final FormControllerProvider formControllerProvider;
    private final Analytics analytics;
    private final MutableLiveData<FormIndex> updates = new MutableLiveData<>(null);

    @Nullable
    private FormIndex jumpBackIndex;

    public FormEntryViewModel(FormControllerProvider formControllerProvider, Analytics analytics) {
        this.formControllerProvider = formControllerProvider;
        this.analytics = analytics;
    }

    public LiveData<FormIndex> getUpdates() {
        return updates;
    }

    public void promptForNewRepeat() {
        FormIndex index = getFormController().getFormIndex();
        jumpBackIndex = index;

        getFormController().jumpToNewRepeatPrompt();
        updates.setValue(getFormController().getFormIndex());
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

        if (getFormController().indexIsInFieldList()) {
            updates.setValue(getFormController().getFormIndex());
        } else {
            try {
                getFormController().stepToNextScreenEvent();
            } catch (JavaRosaException ignored) {
                // ignored
            }
            
            updates.setValue(getFormController().getFormIndex());
        }
    }

    public void cancelRepeatPrompt() {
        analytics.logEvent("AddRepeat", "InlineDecline");

        FormController formController = formControllerProvider.getFormController();

        if (jumpBackIndex != null) {
            formController.jumpToIndex(jumpBackIndex);
            jumpBackIndex = null;
            updates.setValue(getFormController().getFormIndex());
        } else {
            try {
                getFormController().stepToNextScreenEvent();
            } catch (JavaRosaException ignored) {
                // ignored
            }

            updates.setValue(getFormController().getFormIndex());
        }
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
