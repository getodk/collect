package org.odk.collect.android.formentry;

import static org.odk.collect.android.javarosawrapper.FormIndexUtils.getRepeatGroupIndex;
import static org.odk.collect.androidshared.livedata.LiveDataUtils.observe;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.javarosa.core.model.Constants;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.GroupDef;
import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.model.actions.recordaudio.RecordAudioActionHandler;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.form.api.FormEntryController;
import org.javarosa.form.api.FormEntryPrompt;
import org.javarosa.xpath.parser.XPathSyntaxException;
import org.odk.collect.android.exception.ExternalDataException;
import org.odk.collect.android.exception.JavaRosaException;
import org.odk.collect.android.formentry.audit.AuditEvent;
import org.odk.collect.android.formentry.questions.SelectChoiceUtils;
import org.odk.collect.android.javarosawrapper.FailedValidationResult;
import org.odk.collect.android.javarosawrapper.FormController;
import org.odk.collect.android.javarosawrapper.RepeatsInFieldListException;
import org.odk.collect.android.javarosawrapper.ValidationResult;
import org.odk.collect.android.widgets.interfaces.SelectChoiceLoader;
import org.odk.collect.androidshared.data.Consumable;
import org.odk.collect.androidshared.livedata.MutableNonNullLiveData;
import org.odk.collect.androidshared.livedata.NonNullLiveData;
import org.odk.collect.async.Cancellable;
import org.odk.collect.async.Scheduler;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class FormEntryViewModel extends ViewModel implements SelectChoiceLoader {

    private final Supplier<Long> clock;
    private final Scheduler scheduler;

    private final MutableLiveData<FormError> error = new MutableLiveData<>(null);
    private final MutableNonNullLiveData<Boolean> hasBackgroundRecording = new MutableNonNullLiveData<>(false);
    private final MutableLiveData<FormIndex> currentIndex = new MutableLiveData<>(null);
    private final MutableNonNullLiveData<Boolean> isLoading = new MutableNonNullLiveData<>(false);
    private final MutableLiveData<Consumable<ValidationResult>>
            validationResult = new MutableLiveData<>(new Consumable<>(null));
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

    private final Map<FormIndex, List<SelectChoice>> choices = new HashMap<>();

    @SuppressWarnings("WeakerAccess")
    public FormEntryViewModel(Supplier<Long> clock, Scheduler scheduler, FormSessionRepository formSessionRepository, String sessionId) {
        this.clock = clock;
        this.scheduler = scheduler;
        this.formSessionRepository = formSessionRepository;

        this.sessionId = sessionId;
        formSessionObserver = observe(formSessionRepository.get(this.sessionId), formSession -> {
            this.formController = formSession.getFormController();

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

    public LiveData<Consumable<ValidationResult>> getValidationResult() {
        return validationResult;
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
            error.setValue(new FormError.NonFatal(e.getCause() != null ? e.getCause().getMessage() : e.getMessage()));
        }

        if (!formController.indexIsInFieldList()) {
            try {
                formController.stepToNextScreenEvent();
            } catch (JavaRosaException e) {
                error.setValue(new FormError.NonFatal(e.getCause() != null ? e.getCause().getMessage() : e.getMessage()));
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
                error.setValue(new FormError.NonFatal(exception.getCause().getMessage()));
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

            if (updateSuccess) {
                try {
                    formController.stepToNextScreenEvent();
                } catch (JavaRosaException e) {
                    error.setValue(new FormError.NonFatal(e.getCause().getMessage()));
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

            if (updateSuccess) {
                try {
                    formController.stepToPreviousScreenEvent();
                } catch (JavaRosaException e) {
                    error.setValue(new FormError.NonFatal(e.getCause().getMessage()));
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
            ValidationResult result = formController.saveAllScreenAnswers(answers, evaluateConstraints);
            if (result instanceof FailedValidationResult) {
                validationResult.postValue(new Consumable<>(result));
                return false;
            }
        } catch (JavaRosaException e) {
            error.postValue(new FormError.NonFatal(e.getMessage()));
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
        List<SelectChoice> selectChoices = choices.get(prompt.getIndex());

        if (selectChoices != null) {
            return selectChoices;
        } else {
            // Not all select choices are loaded preemptively yet
            return SelectChoiceUtils.loadSelectChoices(prompt, formController);
        }
    }

    @Override
    protected void onCleared() {
        this.answerListener = null;
        formSessionObserver.cancel();
    }

    public void refresh() {
        choices.clear();

        if (formController != null) {
            currentIndex.setValue(formController.getFormIndex()); // Need to this first for `SavePointTest` for some reason

            isLoading.setValue(true);
            scheduler.immediate(() -> {
                try {
                    /*
                     We can't load for field lists as their choices might change on screen (before
                     refresh is called again).
                    */
                    if (!formController.indexIsInFieldList()) {
                        preloadSelectChoices();
                    }

                    return null;
                } catch (RepeatsInFieldListException | XPathSyntaxException |
                         FileNotFoundException e) {
                    return null;
                }
            }, (ignored) -> {
                isLoading.setValue(false);
                currentIndex.setValue(formController.getFormIndex());
            });
        }
    }

    public void exit() {
        formSessionRepository.clear(sessionId);
    }

    public void validate() {
        isLoading.setValue(true);
        scheduler.immediate(
                () -> {
                    ValidationResult result = null;
                    try {
                        result = formController.validateAnswers(true, true);
                    } catch (JavaRosaException e) {
                        error.postValue(new FormError.NonFatal(e.getMessage()));
                    }

                    return result;
                }, result -> {
                    isLoading.setValue(false);

                    if (result instanceof FailedValidationResult) {
                        refresh();
                    }
                    validationResult.setValue(new Consumable<>(result));
                }
        );
    }

    private void preloadSelectChoices() throws RepeatsInFieldListException, FileNotFoundException, XPathSyntaxException {
        int event = formController.getEvent();
        if (event == FormEntryController.EVENT_QUESTION || event == FormEntryController.EVENT_GROUP || event == FormEntryController.EVENT_REPEAT) {
            FormEntryPrompt[] prompts = formController.getQuestionPrompts();
            List<FormEntryPrompt> selectPrompts = Arrays.stream(prompts).filter((prompt) -> {
                boolean isSelect = prompt.getControlType() == Constants.CONTROL_SELECT_ONE || prompt.getControlType() == Constants.CONTROL_SELECT_MULTI || prompt.getControlType() == Constants.CONTROL_RANK;

                boolean isSelectOneExternal = prompt.getControlType() == Constants.CONTROL_INPUT && prompt.getDataType() == Constants.DATATYPE_TEXT && prompt.getQuestion().getAdditionalAttribute(null, "query") != null;
                return isSelect || isSelectOneExternal;
            }).collect(Collectors.toList());
            for (FormEntryPrompt prompt : selectPrompts) {
                List<SelectChoice> selectChoices = SelectChoiceUtils.loadSelectChoices(prompt, formController);
                choices.put(prompt.getIndex(), selectChoices);
            }
        }
    }

    public interface AnswerListener {
        void onAnswer(FormIndex index, IAnswerData answer);
    }
}
