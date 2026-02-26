package org.odk.collect.timedgrid

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.javarosa.core.model.SelectChoice
import org.javarosa.core.model.data.IAnswerData
import org.javarosa.core.model.data.MultipleItemsData
import org.javarosa.core.model.data.helper.Selection
import org.javarosa.form.api.FormEntryPrompt
import kotlin.collections.ifEmpty
import kotlin.collections.toMutableList
import kotlin.time.Duration.Companion.milliseconds

class TimedGridWidgetDelegate(
    private val context: Context,
    private val formEntryPrompt: FormEntryPrompt,
    private val items: List<SelectChoice>,
    formControllerFacade: FormControllerFacade,
    formAnswerRefresher: FormAnswerRefresher,
    private val widgetValueChanged: () -> Unit
) {
    private val viewModel = ViewModelProvider(context as ViewModelStoreOwner)[TimedGridViewModel::class.java]
    private val timer = PausableCountDownTimer()

    // Parsed prompt configuration (type, duration, etc.).
    private val config = TimedGridWidgetConfiguration.fromPrompt(
        formEntryPrompt
    ).also {
        timer.setUpDuration(it.duration.inWholeMilliseconds)
    }

    private val summaryAnswerCreator = TimedGridSummaryAnswerCreator(formEntryPrompt, formControllerFacade, formAnswerRefresher)

    // Filtered items to display in the grid.
    private val gridItems = items
        .filter { it.value != config.allAnsweredCorrectly }
        .map { GridItem(it.value, formEntryPrompt.getSelectChoiceText(it)) }

    // Renderer chosen by the assessment type.
    private val renderer = config.type.createRenderer()

    // Current widget state and timer reference.
    private var state = TimedGridState.NEW

    private val summaryBuilder = TimedGridSummary.Builder()

    fun onCreateWidgetView(parent: ViewGroup): View {
        val inflater = LayoutInflater.from(context)
        val root = renderer.inflateView(inflater, parent)
        renderer.bind(
            root,
            config,
            gridItems,
            onStart = { startAssessment() },
            onComplete = { onEarlyFinishPress() },
            onFinish = { finishAssessment() },
            onTogglePause = { if (state == TimedGridState.PAUSED) resumeAssessment() else pauseAssessment() }
        )

        timer.setUpListeners(
            this::onTimerTick,
            this::onTimerFinish
        )

        viewModel.getTimedGridState(formEntryPrompt.index)?.let { saved ->
            state = saved.state
            timer.cancel()
            timer.setUpDuration(saved.millisRemaining)

            renderer.restoreAnswers(saved.toggledAnswers, saved.lastAttempted)

            if (state == TimedGridState.IN_PROGRESS) {
                timer.start()
            } else if (state == TimedGridState.PAUSED) {
                val secondsRemaining = saved.millisRemaining.milliseconds.inWholeSeconds
                val timeLeftText = context.getString(
                    R.string.timed_grid_time_left,
                    secondsRemaining
                )
                renderer.updateTimer(timeLeftText)
                summaryBuilder.secondsRemaining(secondsRemaining.toInt())
            }

            renderer.updateUIForState(state)
        } ?: run {
            readSavedItems()
            renderer.updateUIForState(state)
        }

        return root
    }

    /**
     * Returns all selected (wrong) answers followed by last attempted answer.
     * If no answers ware selected, a special value is used instead.
     * [selected answers, last attempted answer] where selected answers can be real selected answers or special value for "all correct".
     */
    fun getAnswer(): IAnswerData {
        val selectedValues = renderer.getToggledAnswers().ifEmpty {
            setOf(config.allAnsweredCorrectly)
        }.toMutableList()

        renderer.getLastSelectedLastItemValue()?.let {
            selectedValues.add(it)
        }

        val choicesByValue = items.associateBy { it.value }
        return MultipleItemsData(selectedValues.map { selectedValue -> Selection(choicesByValue[selectedValue]) })
    }

    fun onDetachedFromWindow() {
        // Ensure state is saved when widget is removed
        saveTimerState()
        timer.cancel()
    }

    private fun onTimerTick(millisUntilFinished: Long) {
        val secondsRemaining = millisUntilFinished.milliseconds.inWholeSeconds
        val timeLeftText = context.getString(
            R.string.timed_grid_time_left,
            secondsRemaining
        )
        renderer.updateTimer(timeLeftText)
        summaryBuilder.secondsRemaining(secondsRemaining.toInt())
    }

    private fun onTimerFinish() {
        val timeLeftText = context.getString(
            R.string.timed_grid_time_left,
            0
        )
        renderer.updateTimer(timeLeftText)
        if (config.strict) {
            completeAssessment()
        }
    }

    private fun startAssessment() {
        renderer.restoreAnswers(emptySet(), null)
        state = TimedGridState.IN_PROGRESS
        renderer.updateUIForState(state)
        timer.start()
    }

    private fun pauseAssessment() {
        if (!config.allowPause) {
            return
        }

        timer.pause()
        state = TimedGridState.PAUSED
        saveTimerState()
        renderer.updateUIForState(state)
    }

    private fun resumeAssessment() {
        if (!config.allowPause) {
            return
        }

        timer.start()
        state = TimedGridState.IN_PROGRESS
        renderer.updateUIForState(state)
    }

    private fun onEarlyFinishPress() {
        when (config.finish) {
            FinishType.CONFIRM_AND_PICK -> {
                showConfirmFinishDialog { completeAssessment() }
            }
            FinishType.CONFIRM_AND_AUTO_PICK -> {
                showConfirmFinishDialog {
                    completeAssessment(true)
                }
            }
            FinishType.AUTO_PICK_NO_CONFIRM -> {
                completeAssessment(true)
            }
        }
    }

    private fun showConfirmFinishDialog(onConfirm: () -> Unit) {
        MaterialAlertDialogBuilder(context)
            .setTitle(R.string.early_finish_title)
            .setMessage(R.string.early_finish_message)
            .setPositiveButton(R.string.end_test) { _, _ ->
                onConfirm()
            }
            .setNegativeButton(R.string.continue_test, null)
            .show()
    }

    private fun readSavedItems() {
        val selections = (formEntryPrompt.answerValue?.value as? List<Selection>)
            ?: emptyList()

        val choiceByValue = items.associateBy { it.value }
        val mapped = selections.mapNotNull { choiceByValue[it.value] }

        if (mapped.isNotEmpty()) {
            val toggledSet = mapped.take(mapped.size - 1)
                .mapNotNull { it.value }
                .toSet()

            val lastValue = mapped.last().value
            renderer.restoreAnswers(
                toggledSet,
                lastValue.takeIf { it != config.allAnsweredCorrectly }
            )
        }
    }

    private fun completeAssessment(forceAutopick: Boolean = false) {
        timer.cancel()
        state = TimedGridState.COMPLETED_NO_LAST_ITEM
        saveTimerState()
        renderer.updateUIForState(state)

        // If the last item is toggled, auto-pick it as last attempted
        val lastItemValue = gridItems.lastOrNull()?.value
        if (forceAutopick ||
            (lastItemValue != null && renderer.getToggledAnswers().contains(lastItemValue))
        ) {
            autoPickLastItem()
            finishAssessment()
        }
    }

    private fun finishAssessment() {
        state = TimedGridState.COMPLETED
        saveTimerState()
        renderer.updateUIForState(state)

        // summaryBuilder.secondsRemaining updated by timer
        summaryBuilder.attemptedCount(gridItems.indexOfFirst { gridItem -> gridItem.value == renderer.getLastSelectedLastItemValue() } + 1)
        summaryBuilder.incorrectCount(renderer.getToggledAnswers().size)
        summaryBuilder.correctCount(summaryBuilder.attemptedCount - summaryBuilder.incorrectCount)
        summaryBuilder.firstLineAllIncorrect(renderer.firstLineAllIncorrect())
        summaryBuilder.sentencesPassed(gridItems.subList(0, summaryBuilder.attemptedCount)
            .count { gridItem -> Regex("^\\p{Punct}+$").matches(gridItem.text) })
        summaryBuilder.correctItems(gridItems.subList(0, summaryBuilder.attemptedCount)
            .filter { !renderer.getToggledAnswers().contains(it.value) }
            .joinToString { it.text })
        summaryBuilder.unansweredItems(gridItems.subList(
            summaryBuilder.attemptedCount, gridItems.size
        ).joinToString { it.text })
        summaryBuilder.punctuationCount(gridItems.count { Regex("^\\p{Punct}+$").matches(it.text) })
        summaryAnswerCreator.answerSummaryQuestions(summaryBuilder.build())

        widgetValueChanged()
    }

    private fun autoPickLastItem() {
        val lastItemValue = gridItems.lastOrNull()?.value
        if (lastItemValue != null) {
            renderer.restoreAnswers(renderer.getToggledAnswers(), lastItemValue)
        }
    }

    private fun saveTimerState() {
        viewModel.saveTimedGridState(
            formEntryPrompt.index,
            TimedGridViewModel.TimedGridTimerState(
                millisRemaining = timer.getMillisRemaining(),
                state = state,
                toggledAnswers = renderer.getToggledAnswers(),
                lastAttempted = renderer.getLastSelectedLastItemValue()
            )
        )
    }

    fun shouldBlockNavigation(): Boolean =
        state == TimedGridState.IN_PROGRESS ||
            state == TimedGridState.PAUSED ||
            state == TimedGridState.COMPLETED_NO_LAST_ITEM
}
