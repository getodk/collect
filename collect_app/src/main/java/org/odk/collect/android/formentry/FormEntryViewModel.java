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
import org.odk.collect.android.javarosawrapper.FormController;

import static org.odk.collect.android.analytics.AnalyticsEvents.ADD_REPEAT;

public class FormEntryViewModel extends ViewModel {

    private FormController formController;

    private final Analytics analytics;
    private final MutableLiveData<String> error = new MutableLiveData<>(null);

    @Nullable
    private FormIndex jumpBackIndex;

    public FormEntryViewModel(Analytics analytics) {
        this.analytics = analytics;
    }

    public void formLoaded(FormController formController) {
        this.formController = formController;
    }

    public FormIndex getCurrentIndex() {
        return formController.getFormIndex();
    }

    public LiveData<String> getError() {
        return error;
    }

    public void promptForNewRepeat() {
        FormIndex index = formController.getFormIndex();
        jumpBackIndex = index;

        formController.jumpToNewRepeatPrompt();
    }

    public void addRepeat(boolean fromPrompt) {
        if (jumpBackIndex != null) {
            jumpBackIndex = null;
            analytics.logEvent(ADD_REPEAT, "Inline", formController.getCurrentFormIdentifierHash());
        } else if (fromPrompt) {
            analytics.logEvent(ADD_REPEAT, "Prompt", formController.getCurrentFormIdentifierHash());
        } else {
            analytics.logEvent(ADD_REPEAT, "Hierarchy", formController.getCurrentFormIdentifierHash());
        }

        formController.newRepeat();

        if (!formController.indexIsInFieldList()) {
            try {
                formController.stepToNextScreenEvent();
            } catch (JavaRosaException exception) {
                error.setValue(exception.getCause().getMessage());
            }
        }
    }

    public void cancelRepeatPrompt() {
        analytics.logEvent(ADD_REPEAT, "InlineDecline");

        FormController formController = this.formController;

        if (jumpBackIndex != null) {
            formController.jumpToIndex(jumpBackIndex);
            jumpBackIndex = null;
        } else {
            try {
                this.formController.stepToNextScreenEvent();
            } catch (JavaRosaException exception) {
                error.setValue(exception.getCause().getMessage());
            }
        }
    }

    public void errorDisplayed() {
        error.setValue(null);
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
