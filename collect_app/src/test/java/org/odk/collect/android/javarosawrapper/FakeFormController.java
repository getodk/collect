package org.odk.collect.android.javarosawrapper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.form.api.FormEntryController;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.exception.JavaRosaException;
import org.odk.collect.android.formentry.audit.AuditEventLogger;
import org.odk.collect.android.utilities.StubFormController;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class FakeFormController extends StubFormController {
    private FormIndex index;
    private final AuditEventLogger auditEventLogger;
    private final LinkedList<Integer> nextEvents = new LinkedList<>();
    private Integer currentEvent = FormEntryController.EVENT_END_OF_FORM;
    private int step;
    private RuntimeException newRepeatError;
    private JavaRosaException nextStepError;
    private JavaRosaException saveError;
    private Map<FormIndex, FormEntryPrompt> prompts = new HashMap<>();
    private FailedValidationResult failedConstraint;
    private JavaRosaException validationError;
    private JavaRosaException previousStepError;
    private FormIndex nextRepeatPrompt;
    private List<FormEntryPrompt> currentPrompts;

    public FakeFormController(FormIndex startingIndex, AuditEventLogger auditEventLogger) {
        this.index = startingIndex;
        this.auditEventLogger = auditEventLogger;
    }

    @Nullable
    @Override
    public FormIndex getFormIndex() {
        return index;
    }

    @Nullable
    @Override
    public FormDef getFormDef() {
        return new FormDef();
    }

    @Override
    public int getEvent() {
        return currentEvent;
    }

    @Nullable
    @Override
    public AuditEventLogger getAuditEventLogger() {
        return auditEventLogger;
    }

    @Override
    public int stepToNextScreenEvent() throws JavaRosaException {
        if (nextStepError != null) {
            throw nextStepError;
        }

        step = step + 1;
        index = new FormIndex(null, index.getLocalIndex() + 1, 0, new TreeReference());

        if (!nextEvents.isEmpty()) {
            currentEvent = nextEvents.pop();
        } else {
            currentEvent = FormEntryController.EVENT_END_OF_FORM;
        }

        return currentEvent;
    }

    @Override
    public void newRepeat() {
        if (newRepeatError != null) {
            throw newRepeatError;
        }
    }

    @NonNull
    @Override
    public ValidationResult saveAllScreenAnswers(@Nullable HashMap<FormIndex, IAnswerData> answers, boolean evaluateConstraints) throws JavaRosaException {
        if (saveError != null) {
            throw saveError;
        } else if (failedConstraint != null) {
            return failedConstraint;
        } else {
            return SuccessValidationResult.INSTANCE;
        }
    }

    @Nullable
    @Override
    public FormEntryPrompt getQuestionPrompt(FormIndex index) {
        return prompts.get(index);
    }

    @Nullable
    @Override
    public FormEntryPrompt getQuestionPrompt() {
        return currentPrompts.get(0);
    }

    @NonNull
    @Override
    public ValidationResult validateAnswers(boolean markCompleted, boolean moveToInvalidIndex) throws JavaRosaException {
        if (validationError != null) {
            throw validationError;
        } else if (failedConstraint != null) {
            return failedConstraint;
        } else {
            return SuccessValidationResult.INSTANCE;
        }
    }

    @Override
    public int stepToPreviousScreenEvent() throws JavaRosaException {
        if (previousStepError != null) {
            throw previousStepError;
        }

        step = step - 1;
        index = new FormIndex(null, index.getLocalIndex() - 1, 0, new TreeReference());
        return FormEntryController.EVENT_BEGINNING_OF_FORM;
    }

    @Override
    public void jumpToNewRepeatPrompt() {
        if (nextRepeatPrompt != null) {
            index = nextRepeatPrompt;
        } else {
            throw new IllegalStateException("No repeat prompt index set!");
        }
    }

    @Override
    public int jumpToIndex(@Nullable FormIndex index) {
        this.index = index;
        return FormEntryController.EVENT_END_OF_FORM;
    }

    @NonNull
    @Override
    public FormEntryPrompt[] getQuestionPrompts() {
        return currentPrompts.toArray(new FormEntryPrompt[] {});
    }

    public void addNextEvents(List<Integer> events) {
        nextEvents.addAll(events);
    }

    public int getStepPosition() {
        return step;
    }

    public void setNewRepeatError(RuntimeException exception) {
        this.newRepeatError = exception;
    }

    public void setNextStepError(JavaRosaException exception) {
        this.nextStepError = exception;
    }

    public void setPreviousStepError(JavaRosaException exception) {
        this.previousStepError = exception;
    }

    public void setSaveError(JavaRosaException exception) {
        this.saveError = exception;
    }

    public void setPrompt(FormIndex index, FormEntryPrompt prompt) {
        prompts.put(index, prompt);
    }

    public void setFailedConstraint(FailedValidationResult result) {
        this.failedConstraint = result;
    }

    public void setValidationError(JavaRosaException exception) {
        this.validationError = exception;
    }

    public void setNextRepeatPrompt(FormIndex nextRepeatPrompt) {
        this.nextRepeatPrompt = nextRepeatPrompt;
    }

    public void setCurrentEvent(int event) {
        this.currentEvent = event;
    }

    public void setQuestionPrompts(List<FormEntryPrompt> prompts) {
        this.currentPrompts = prompts;
    }
}
