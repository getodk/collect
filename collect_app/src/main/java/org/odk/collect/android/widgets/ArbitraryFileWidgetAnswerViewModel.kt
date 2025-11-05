package org.odk.collect.android.widgets

import android.content.Context
import androidx.lifecycle.ViewModel
import org.odk.collect.android.utilities.MediaUtils
import org.odk.collect.android.utilities.QuestionMediaManager

class ArbitraryFileWidgetAnswerViewModel(
    private val questionMediaManager: QuestionMediaManager,
    private val mediaUtils: MediaUtils
) : ViewModel() {

    fun openFile(context: Context, answer: String?) {
        val file = questionMediaManager.getAnswerFile(answer)
        if (file != null) {
            mediaUtils.openFile(context, file, null)
        }
    }
}
