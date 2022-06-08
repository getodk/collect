package org.odk.collect.android.formentry;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.GroupDef;
import org.javarosa.core.model.actions.recordaudio.RecordAudioActionHandler;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.form.api.FormEntryController;
import org.javarosa.form.api.FormEntryPrompt;
import org.jetbrains.annotations.NotNull;
import org.odk.collect.android.exception.JavaRosaException;
import org.odk.collect.android.formentry.audit.AuditEvent;
import org.odk.collect.android.javarosawrapper.FormController;
import org.odk.collect.androidshared.livedata.MutableNonNullLiveData;
import org.odk.collect.androidshared.livedata.NonNullLiveData;
import org.odk.collect.async.Scheduler;

import java.util.HashMap;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.odk.collect.android.javarosawrapper.FormIndexUtils.getRepeatGroupIndex;

public class FormEntryViewModel extends ViewModel implements RequiresFormController {

    private final Supplier<Long> clock;
    private final Scheduler scheduler;

    private final MutableLiveData<FormError> error = new MutableLiveData<>(null);
    private final MutableNonNullLiveData<Boolean> hasBackgroundRecording = new MutableNonNullLiveData<>(false);
    private final MutableLiveData<FormIndex> currentIndex = new MutableLiveData<>(null);
    private final MutableNonNullLiveData<Boolean> isLoading = new MutableNonNullLiveData<>(false);

    @Nullable
    private FormController formController;

    @Nullable
    private FormIndex jumpBackIndex;

    @Nullable
    private AnswerListener answerListener;

    @SuppressWarnings("WeakerAccess")
    public FormEntryViewModel(Supplier<Long> clock, Scheduler scheduler) {
        this.clock = clock;
        this.scheduler = scheduler;
    }

    @Override
    public void formLoaded(@NotNull FormController formController) {
        this.formController = formController;

        boolean hasBackgroundRecording = formController.getFormDef().hasAction(RecordAudioActionHandler.ELEMENT_NAME);
        this.hasBackgroundRecording.setValue(hasBackgroundRecording);
    }

    public boolean isFormControllerSet() {
        return formController != null;
    }

    @Nullable
    public LiveData<FormIndex> getCurrentIndex() {
        return currentIndex;
    }

    public LiveData<FormError> getError() {
        return error;
    }

    public NonNullLiveData<Boolean> isLoading() {
        return isLoading;
    }

    @SuppressWarnings("WeakerAccess")
    public void promptForNewRepeat() {
        if (formController == null) {
            return;
        }

        jumpBackIndex = formController.getFormIndex();
        jumpToNewRepeat();
        updateIndex();
    }

    public void jumpToNewRepeat() {
        formController.jumpToNewRepeatPrompt();
    }

    public void addRepeat() {
        if (formController == null) {
            return;
        }

        jumpBackIndex = null;

        try {
            formController.newRepeat();
        } catch (RuntimeException e) {
            error.setValue(new NonFatal(e.getCause() != null ? e.getCause().getMessage() : e.getMessage()));
        }

        if (!formController.indexIsInFieldList()) {
            try {
                formController.stepToNextScreenEvent();
            } catch (JavaRosaException e) {
                error.setValue(new NonFatal(e.getCause() != null ? e.getCause().getMessage() : e.getMessage()));
            }
        }

        updateIndex();
    }

    public void cancelRepeatPrompt() {
        if (formController == null) {
            return;
        }

        if (jumpBackIndex != null) {
            formController.jumpToIndex(jumpBackIndex);
            jumpBackIndex = null;
        } else {
            try {
                this.formController.stepToNextScreenEvent();
            } catch (JavaRosaException exception) {
                error.setValue(new NonFatal(exception.getCause().getMessage()));
            }
        }

        updateIndex();
    }

    public void errorDisplayed() {
        error.setValue(null);
    }

    public boolean canAddRepeat() {
        if (formController != null && formController.indexContainsRepeatableGroup()) {
            FormDef formDef = formController.getFormDef();
            FormIndex repeatGroupIndex = getRepeatGroupIndex(formController.getFormIndex(), formDef);
            return !((GroupDef) formDef.getChild(repeatGroupIndex)).noAddRemove;
        } else {
            return false;
        }
    }

    public void moveForward() {
        try {
            formController.stepToNextScreenEvent();
        } catch (JavaRosaException e) {
            error.setValue(new NonFatal(e.getCause().getMessage()));
            return;
        }

        formController.getAuditEventLogger().flush(); // Close events waiting for an end time
        updateIndex();
    }

    public void moveForward(HashMap<FormIndex, IAnswerData> answers) {
        isLoading.setValue(true);

        scheduler.immediate((Supplier<Void>) () -> {
            updateAnswersForScreen(answers);
            return null;
        }, unused -> {
            isLoading.setValue(false);
            moveForward();
        });
    }

    public void moveBackward(HashMap<FormIndex, IAnswerData> answers) {
        isLoading.setValue(true);

        scheduler.immediate((Supplier<Void>) () -> {
            if (formController.currentPromptIsQuestion()) {
                updateAnswersForScreen(answers);
            }
            return null;
        }, unused -> {
            isLoading.setValue(false);

            try {
                int event = formController.stepToPreviousScreenEvent();

                // If we are the beginning of the form we need to move back to the first actual screen
                if (event == FormEntryController.EVENT_BEGINNING_OF_FORM) {
                    formController.stepToNextScreenEvent();
                }
            } catch (JavaRosaException e) {
                error.setValue(new NonFatal(e.getCause().getMessage()));
                return;
            }

            formController.getAuditEventLogger().flush(); // Close events waiting for an end time
            updateIndex();
        });
    }

    public void updateAnswersForScreen(HashMap<FormIndex, IAnswerData> answers) {
        if (formController == null) {
            return;
        }

        try {
            formController.saveAllScreenAnswers(answers, false);
        } catch (JavaRosaException ignored) {
            // ignored
        }

        formController.getAuditEventLogger().flush();
    }

    public void openHierarchy() {
        formController.getAuditEventLogger().logEvent(AuditEvent.AuditEventType.HIERARCHY, true, clock.get());
    }

    public NonNullLiveData<Boolean> hasBackgroundRecording() {
        return hasBackgroundRecording;
    }

    public FormEntryPrompt getQuestionPrompt(FormIndex formIndex) {
        return formController.getQuestionPrompt(formIndex);
    }

    public void setAnswerListener(@Nullable AnswerListener answerListener) {
        this.answerListener = answerListener;
    }

    public void answerQuestion(FormIndex index, IAnswerData answer) {
        if (this.answerListener != null) {
            this.answerListener.onAnswer(index, answer);
        }
    }

    @Override
    protected void onCleared() {
        this.answerListener = null;
    }

    private void updateIndex() {
        currentIndex.setValue(formController.getFormIndex());
    }

    public static class Factory implements ViewModelProvider.Factory {

        private final Supplier<Long> clock;
        private final Scheduler scheduler;

        public Factory(Supplier<Long> clock, Scheduler scheduler) {
            this.clock = clock;
            this.scheduler = scheduler;
        }

        @SuppressWarnings("unchecked")
        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new FormEntryViewModel(clock, scheduler);
        }
    }

    public abstract static class FormError {

    }

    public static class NonFatal extends FormError {

        private final String message;

        public NonFatal(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }

            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            NonFatal nonFatal = (NonFatal) o;
            return Objects.equals(message, nonFatal.message);
        }

        @Override
        public int hashCode() {
            return Objects.hash(message);
        }
    }

    public interface AnswerListener {
        void onAnswer(FormIndex index, IAnswerData answer);
    }
}
