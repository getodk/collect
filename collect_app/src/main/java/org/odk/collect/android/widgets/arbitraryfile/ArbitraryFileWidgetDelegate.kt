package org.odk.collect.android.widgets.arbitraryfile

import org.javarosa.core.model.data.IAnswerData
import org.javarosa.core.model.data.StringData
import org.odk.collect.android.utilities.QuestionMediaManager
import timber.log.Timber
import java.io.File

class ArbitraryFileWidgetDelegate(
    private val questionMediaManager: QuestionMediaManager
) {
    fun getAnswer(answer: String?): IAnswerData? {
        return if (answer.isNullOrEmpty()) null else StringData(answer)
    }

    fun deleteFile(formEntryPromptIndex: String, answer: String?) {
        questionMediaManager.deleteAnswerFile(
            formEntryPromptIndex,
            questionMediaManager.getAnswerFile(answer)!!.absolutePath
        )
    }

    fun setData(
        formEntryPromptIndex: String,
        previousAnswer: String?,
        newAnswer: Any,
        onSuccess: (String) -> Unit
    ) {
        if (previousAnswer != null) {
            deleteFile(formEntryPromptIndex, previousAnswer)
        }

        if (newAnswer is File) {
            if (newAnswer.exists()) {
                questionMediaManager.replaceAnswerFile(
                    formEntryPromptIndex,
                    newAnswer.absolutePath
                )
                onSuccess(newAnswer.name)
            } else {
                Timber.e(Error("Inserting Arbitrary file FAILED"))
            }
        } else {
            Timber.e(Error("FileWidget's setBinaryData must receive a File but received: " + newAnswer.javaClass))
        }
    }
}
