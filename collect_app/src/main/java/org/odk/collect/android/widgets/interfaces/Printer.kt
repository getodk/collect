package org.odk.collect.android.widgets.interfaces

import android.content.Context
import org.odk.collect.android.utilities.QuestionMediaManager
import org.odk.collect.androidshared.livedata.NonNullLiveData

interface Printer {
    fun parseAndPrint(
        htmlDocument: String,
        questionMediaManager: QuestionMediaManager,
        context: Context
    )

    fun isLoading(): NonNullLiveData<Boolean>
}
