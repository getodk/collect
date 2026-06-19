package org.odk.collect.android.widgets.image

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import org.javarosa.core.model.data.IAnswerData
import org.javarosa.core.model.data.StringData
import org.javarosa.form.api.FormEntryPrompt
import org.odk.collect.android.utilities.QuestionMediaManager
import org.odk.collect.android.widgets.QuestionWidget
import timber.log.Timber
import java.io.File

class FileAnswerDelegate(
    private val widget: QuestionWidget,
    private val questionMediaManager: QuestionMediaManager,
    private val prompt: FormEntryPrompt
) {
    var binaryName by mutableStateOf<String?>(prompt.answerText)
        private set

    fun getAnswer(): IAnswerData? {
        return binaryName?.let { StringData(it) }
    }

    fun deleteFile() {
        clearFile()
        widget.widgetValueChanged()
    }

    fun setData(objectData: Any) {
        if (binaryName != null) {
            clearFile()
        }

        if (objectData is File) {
            if (objectData.exists()) {
                questionMediaManager.replaceAnswerFile(prompt.index.toString(), objectData.absolutePath)
                binaryName = objectData.name
                widget.widgetValueChanged()
            } else {
                Timber.e(Error("File does not exist: ${objectData.absolutePath}"))
            }
        } else {
            Timber.e(Error("FileAnswerDelegate.setData must receive a File object, but received ${objectData.javaClass.name}"))
        }
    }

    private fun clearFile() {
        questionMediaManager.deleteAnswerFile(prompt.index.toString(), binaryName)
        binaryName = null
    }
}
