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

    private FormController formController;
    private final Analytics analytics;
    private final MutableLiveData<FormIndex> updates = new MutableLiveData<>(null);

    @Nullable
    private FormIndex jumpBackIndex;

    public FormEntryViewModel(Analytics analytics) {
        this.analytics = analytics;
    }

    public void formLoaded(FormController formController) {
        this.formController = formController;
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

        FormController formController = getFormController();

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
        return formController;
    }

    public static class Factory implements ViewModelProvider.Factory {

        private final Analytics analytics;

        public Factory(Analytics analytics) {
            this.analytics = analytics;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new FormEntryViewModel(analytics);
        }
    }
}
