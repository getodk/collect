package org.odk.collect.android.widgets.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.javarosa.core.model.FormIndex
import org.javarosa.core.model.data.IAnswerData
import org.odk.collect.android.formentry.FormSession
import org.odk.collect.android.formentry.FormSessionRepository
import org.odk.collect.android.javarosawrapper.FormController
import org.odk.collect.android.javarosawrapper.ValidationResult
import org.odk.collect.androidshared.livedata.LiveDataUtils
import org.odk.collect.async.Scheduler

class QuestionViewModel(
    private val scheduler: Scheduler,
    formSessionRepository: FormSessionRepository,
    sessionId: String
) : ViewModel() {
    private val _constraintValidationResult: MutableLiveData<ValidationResult> = MutableLiveData()
    val constraintValidationResult: LiveData<ValidationResult> = _constraintValidationResult
    private var formController: FormController? = null
    private var formSessionObserver = LiveDataUtils.observe(
        formSessionRepository.get(sessionId)
    ) { formSession: FormSession ->
        formController = formSession.formController
    }

    fun validate(index: FormIndex, answer: IAnswerData?) {
        scheduler.immediate {
            formController?.validateAnswerConstraint(index, answer)?.let {
                _constraintValidationResult.postValue(it)
            }
        }
    }

    override fun onCleared() {
        formSessionObserver.cancel()
    }
}
