package org.odk.collect.android.formentry.audit;

import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.javarosawrapper.FormController;
import org.odk.collect.android.javarosawrapper.RepeatsInFieldListException;

import static org.javarosa.form.api.FormEntryController.EVENT_GROUP;
import static org.javarosa.form.api.FormEntryController.EVENT_QUESTION;
import static org.javarosa.form.api.FormEntryController.EVENT_REPEAT;

public final class AuditUtils {

    private AuditUtils() {

    }

    public static void logCurrentScreen(FormController formController, AuditEventLogger auditEventLogger, long currentTime) {
        if (formController.getEvent() == EVENT_QUESTION
                || formController.getEvent() == EVENT_GROUP
                || formController.getEvent() == EVENT_REPEAT) {
            try {
                FormEntryPrompt[] prompts = formController.getQuestionPrompts();
                for (FormEntryPrompt question : prompts) {
                    String answer = question.getAnswerValue() != null ? question.getAnswerValue().getDisplayText() : null;
                    auditEventLogger.logEvent(AuditEvent.AuditEventType.QUESTION, question.getIndex(), true, answer, currentTime, null);
                }
            } catch (RepeatsInFieldListException e) {
                throw new RuntimeException(e.getMessage(), e);
            }
        }
    }
}
