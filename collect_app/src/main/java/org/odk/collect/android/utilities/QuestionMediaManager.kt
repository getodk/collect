package org.odk.collect.android.utilities

import androidx.lifecycle.LiveData
import org.odk.collect.utilities.Result
import java.io.File

/**
 * Provides an interface for widgets to manage answer files. This lets them delete answer files
 * when an answer is cleared, replace answer files when an answer is changed and also access answer
 * files.
 */
interface QuestionMediaManager {

    fun createAnswerFile(file: File): LiveData<Result<File>>

    fun getAnswerFile(fileName: String?): File?

    fun deleteAnswerFile(questionIndex: String, fileName: String?)

    fun replaceAnswerFile(questionIndex: String, fileName: String)
}

fun QuestionMediaManager.getExistingAnswerFile(fileName: String?): File? {
    val file = getAnswerFile(fileName)
    return if (file != null && file.exists()) file else null
}
