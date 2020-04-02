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

public class FormEntryViewModel extends ViewModel implements RequiresFormController {


    private final Analytics analytics;
    private final MutableLiveData<String> error = new MutableLiveData<>(null);

    @Nullable
    private FormController formController;

    @Nullable
    private FormIndex jumpBackIndex;

    @SuppressWarnings("WeakerAccess")
    public FormEntryViewModel(Analytics analytics) {
        this.analytics = analytics;
    }

    @Override
    public void formLoaded(FormController formController) {
        this.formController = formController;
    }

    @Nullable
    public FormIndex getCurrentIndex() {
        if (formController != null) {
            return formController.getFormIndex();
        } else {
            return null;
        }
    }

    public LiveData<String> getError() {
        return error;
    }

    @SuppressWarnings("WeakerAccess")
    public void promptForNewRepeat() {
        if (formController == null) {
            return;
        }

        jumpBackIndex = formController.getFormIndex();
        formController.jumpToNewRepeatPrompt();
    }

    public void addRepeat(boolean fromPrompt) {
        if (formController == null) {
            return;
        }

        if (jumpBackIndex != null) {
            jumpBackIndex = null;
            analytics.logEvent(ADD_REPEAT, "Inline", formController.getCurrentFormIdentifierHash());
        } else if (fromPrompt) {
            analytics.logEvent(ADD_REPEAT, "Prompt", formController.getCurrentFormIdentifierHash());
        } else {
            analytics.logEvent(ADD_REPEAT, "Hierarchy", formController.getCurrentFormIdentifierHash());
        }

        formController.newRepeat();

        try {
            formController.stepToNextScreenEvent();
        } catch (JavaRosaException exception) {
            error.setValue(exception.getCause().getMessage());
        }
    }

    public void cancelRepeatPrompt() {
        if (formController == null) {
            return;
        }

        analytics.logEvent(ADD_REPEAT, "InlineDecline", formController.getCurrentFormIdentifierHash());
        
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

        @SuppressWarnings("unchecked")
        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new FormEntryViewModel(analytics);
        }
    }
}
