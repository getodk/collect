package org.odk.collect.android.widgets.utilities

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.javarosa.core.model.data.IAnswerData
import org.javarosa.core.model.data.StringData
import org.javarosa.form.api.FormEntryPrompt
import org.odk.collect.android.utilities.QuestionMediaManager
import timber.log.Timber
import java.io.File

class FileAnswerDelegate(
    private val questionMediaManager: QuestionMediaManager,
    private val prompt: FormEntryPrompt
) {
    var binaryName by mutableStateOf<String?>(prompt.answerText)
        private set

    fun getAnswer(): IAnswerData? {
        return binaryName?.let { StringData(it) }
    }

    fun deleteFile() {
        questionMediaManager.deleteAnswerFile(prompt.index.toString(), binaryName)
        binaryName = null
    }

    fun setData(objectData: Any): Boolean {
        if (binaryName != null) {
            deleteFile()
        }

        if (objectData is File) {
            if (objectData.exists()) {
                questionMediaManager.replaceAnswerFile(prompt.index.toString(), objectData.absolutePath)
                binaryName = objectData.name
                return true
            } else {
                Timber.e(Error("File does not exist: ${objectData.absolutePath}"))
            }
        } else {
            Timber.e(Error("FileAnswerDelegate.setData must receive a File object, but received ${objectData.javaClass.name}"))
        }

        return false
    }
}
