package org.odk.collect.android.formentry;

import static org.odk.collect.android.javarosawrapper.FormIndexUtils.getRepeatGroupIndex;
import static org.odk.collect.androidshared.livedata.LiveDataUtils.observe;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.GroupDef;
import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.model.actions.recordaudio.RecordAudioActionHandler;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.form.api.FormEntryPrompt;
import org.javarosa.xpath.parser.XPathSyntaxException;
import org.odk.collect.android.exception.ExternalDataException;
import org.odk.collect.android.exception.JavaRosaException;
import org.odk.collect.android.formentry.audit.AuditEvent;
import org.odk.collect.android.formentry.questions.SelectChoiceUtils;
import org.odk.collect.android.javarosawrapper.FailedConstraint;
import org.odk.collect.android.javarosawrapper.FormController;
import org.odk.collect.android.widgets.interfaces.SelectChoiceLoader;
import org.odk.collect.androidshared.livedata.MutableNonNullLiveData;
import org.odk.collect.androidshared.livedata.NonNullLiveData;
import org.odk.collect.async.Cancellable;
import org.odk.collect.async.Scheduler;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class FormEntryViewModel extends ViewModel implements SelectChoiceLoader {

    private final Supplier<Long> clock;
    private final Scheduler scheduler;

    private final MutableLiveData<FormError> error = new MutableLiveData<>(null);
    private final MutableNonNullLiveData<Boolean> hasBackgroundRecording = new MutableNonNullLiveData<>(false);
    private final MutableLiveData<FormIndex> currentIndex = new MutableLiveData<>(null);
    private final MutableNonNullLiveData<Boolean> isLoading = new MutableNonNullLiveData<>(false);
    private final MutableLiveData<FailedConstraint> failedConstraint = new MutableLiveData<>(null);
    @NonNull
    private final FormSessionRepository formSessionRepository;
    private final String sessionId;

    @Nullable
    private FormController formController;

    @Nullable
    private FormIndex jumpBackIndex;

    @Nullable
    private AnswerListener answerListener;

    private final Cancellable formSessionObserver;

    @SuppressWarnings("WeakerAccess")
    public FormEntryViewModel(Supplier<Long> clock, Scheduler scheduler, FormSessionRepository formSessionRepository, String sessionId) {
        this.clock = clock;
        this.scheduler = scheduler;
        this.formSessionRepository = formSessionRepository;

        this.sessionId = sessionId;
        formSessionObserver = observe(formSessionRepository.get(this.sessionId), formController -> {
            this.formController = formController;

            boolean hasBackgroundRecording = formController.getFormDef().hasAction(RecordAudioActionHandler.ELEMENT_NAME);
            this.hasBackgroundRecording.setValue(hasBackgroundRecording);
        });
    }

    public String getSessionId() {
        return sessionId;
    }

    /**
     * @deprecated this should not be exposed
     */
    @Deprecated
    public FormController getFormController() {
        return formController;
    }

    public LiveData<FormIndex> getCurrentIndex() {
        return currentIndex;
    }

    public LiveData<FormError> getError() {
        return error;
    }

    public LiveData<FailedConstraint> getFailedConstraint() {
        return failedConstraint;
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
        refresh();
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

        refresh();
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

        refresh();
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

    public void moveForward(HashMap<FormIndex, IAnswerData> answers) {
        moveForward(answers, false);
    }

    public void moveForward(HashMap<FormIndex, IAnswerData> answers, Boolean evaluateConstraints) {
        isLoading.setValue(true);

        scheduler.immediate((Supplier<Boolean>) () -> {
            return saveScreenAnswersToFormController(answers, evaluateConstraints);
        }, updateSuccess -> {
            isLoading.setValue(false);

            formController.getAuditEventLogger().flush();

            if (updateSuccess) {
                try {
                    formController.stepToNextScreenEvent();
                } catch (JavaRosaException e) {
                    error.setValue(new NonFatal(e.getCause().getMessage()));
                }

                formController.getAuditEventLogger().flush(); // Close events waiting for an end time
                refresh();
            }
        });
    }

    public void moveBackward(HashMap<FormIndex, IAnswerData> answers) {
        isLoading.setValue(true);

        scheduler.immediate((Supplier<Boolean>) () -> {
            return saveScreenAnswersToFormController(answers, false);
        }, updateSuccess -> {
            isLoading.setValue(false);

            formController.getAuditEventLogger().flush();

            if (updateSuccess) {
                try {
                    formController.stepToPreviousScreenEvent();
                } catch (JavaRosaException e) {
                    error.setValue(new NonFatal(e.getCause().getMessage()));
                    return;
                }

                formController.getAuditEventLogger().flush(); // Close events waiting for an end time
                refresh();
            }
        });
    }

    public boolean updateAnswersForScreen(HashMap<FormIndex, IAnswerData> answers, Boolean evaluateConstraints) {
        boolean success = saveScreenAnswersToFormController(answers, evaluateConstraints);
        formController.getAuditEventLogger().flush();

        return success;
    }

    public boolean saveScreenAnswersToFormController(HashMap<FormIndex, IAnswerData> answers, Boolean evaluateConstraints) {
        if (formController == null) {
            return false;
        }

        try {
            FailedConstraint result = formController.saveAllScreenAnswers(answers, evaluateConstraints);
            if (result != null) {
                failedConstraint.postValue(result);
                return false;
            }
        } catch (JavaRosaException e) {
            error.postValue(new NonFatal(e.getMessage()));
            return false;
        }

        return true;
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

    @NonNull
    @Override
    public List<SelectChoice> loadSelectChoices(@NonNull FormEntryPrompt prompt) throws FileNotFoundException, XPathSyntaxException, ExternalDataException {
        return SelectChoiceUtils.loadSelectChoices(prompt, formController);
    }

    @Override
    protected void onCleared() {
        this.answerListener = null;
        formSessionObserver.cancel();
    }

    public void refresh() {
        currentIndex.setValue(formController.getFormIndex());
    }

    public void exit() {
        formSessionRepository.clear(sessionId);
    }

    public static class Factory implements ViewModelProvider.Factory {

        private final Supplier<Long> clock;
        private final Scheduler scheduler;
        private final FormSessionRepository formSessionRepository;
        private String sessionId;

        public Factory(Supplier<Long> clock, Scheduler scheduler, FormSessionRepository formSessionRepository) {
            this.clock = clock;
            this.scheduler = scheduler;
            this.formSessionRepository = formSessionRepository;
        }

        public void setSessionId(String sessionId) {
            this.sessionId = sessionId;
        }

        @SuppressWarnings("unchecked")
        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new FormEntryViewModel(clock, scheduler, formSessionRepository, sessionId);
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
