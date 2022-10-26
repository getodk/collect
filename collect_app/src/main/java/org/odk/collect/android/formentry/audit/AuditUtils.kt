package org.odk.collect.android.formentry.audit

import org.javarosa.form.api.FormEntryController
import org.odk.collect.android.javarosawrapper.FormController
import org.odk.collect.android.javarosawrapper.RepeatsInFieldListException

object AuditUtils {
    @JvmStatic
    fun logCurrentScreen(
        formController: FormController,
        auditEventLogger: AuditEventLogger,
        currentTime: Long
    ) {
        if (formController.getEvent() == FormEntryController.EVENT_QUESTION ||
            formController.getEvent() == FormEntryController.EVENT_GROUP ||
            formController.getEvent() == FormEntryController.EVENT_REPEAT
        ) {
            try {
                for (question in formController.getQuestionPrompts()) {
                    val answer =
                        if (question.answerValue != null)
                            question.answerValue!!.displayText
                        else null

                    auditEventLogger.logEvent(
                        AuditEvent.AuditEventType.QUESTION,
                        question.index,
                        true,
                        answer,
                        currentTime,
                        null
                    )
                }
            } catch (e: RepeatsInFieldListException) {
                // ignore
            }
        }
    }
}
