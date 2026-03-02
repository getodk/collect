package org.odk.collect.android.widgets

import android.annotation.SuppressLint
import android.content.Context
import org.javarosa.core.model.FormIndex
import org.javarosa.core.model.IFormElement
import org.javarosa.core.model.SelectChoice
import org.javarosa.core.model.data.IAnswerData
import org.javarosa.form.api.FormEntryPrompt
import org.odk.collect.android.activities.FormFillingActivity
import org.odk.collect.android.formentry.FormEntryViewModel
import org.odk.collect.android.formentry.questions.QuestionDetails
import org.odk.collect.android.widgets.items.ItemsWidgetUtils
import org.odk.collect.timedgrid.FormAnswerRefresher
import org.odk.collect.timedgrid.FormControllerFacade
import org.odk.collect.timedgrid.NavigationAwareWidget
import org.odk.collect.timedgrid.NavigationWarning
import org.odk.collect.timedgrid.TimedGridWidgetDelegate

@SuppressLint("ViewConstructor")
class TimedGridWidget(
    context: Context,
    questionDetails: QuestionDetails,
    dependencies: Dependencies,
    formEntryViewModel: FormEntryViewModel
) : QuestionWidget(context, dependencies, questionDetails), NavigationAwareWidget {
    private val widgetDelegate = TimedGridWidgetDelegate(
        context,
        questionDetails.prompt,
        object : FormControllerFacade {
            override fun getFormElements(): List<IFormElement>? {
                return formEntryViewModel.formController.getFormDef()?.children
            }

            override fun getItems(): List<SelectChoice> {
                return ItemsWidgetUtils.loadItemsAndHandleErrors(
                    this@TimedGridWidget, questionDetails.prompt, formEntryViewModel
                )
            }

            override fun saveAnswer(index: FormIndex, answer: IAnswerData) {
                formEntryViewModel.formController.saveOneScreenAnswer(index, answer, false)
            }
        },
        object : FormAnswerRefresher {
            override fun refreshAnswer(index: FormIndex) {
                val activity = context as? FormFillingActivity ?: return
                val odkView = activity.currentViewIfODKView ?: return

                val widget = odkView.widgets
                    .filterIsInstance<StringWidget>()
                    .firstOrNull { it.formEntryPrompt.index == index }
                    ?: return

                widget.apply {
                    setDisplayValueFromModel()
                    widgetValueChanged()
                    showAnswerContainer()
                }
            }
        }
    ) {
        widgetValueChanged()
    }

    init {
        render()
    }

    override fun onCreateWidgetView(context: Context, prompt: FormEntryPrompt, answerFontSize: Int) = widgetDelegate.onCreateWidgetView(this)

    override fun getAnswer() = widgetDelegate.getAnswer()

    override fun clearAnswer() {}

    override fun setOnLongClickListener(l: OnLongClickListener?) {}

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        widgetDelegate.onDetachedFromWindow()
    }

    override fun shouldBlockNavigation(): NavigationWarning? = widgetDelegate.shouldBlockNavigation()
}
