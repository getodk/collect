package org.odk.collect.timedgrid

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.odk.collect.timedgrid.databinding.TimedGridBinding

/**
 * Common renderer for timed grid modes that share the same UI and interaction pattern:
 * LETTERS, WORDS, NUMBERS, ARITHMETIC, READING, X.
 */
class CommonTimedGridRenderer(
    private val showRowNumbers: Boolean = true
) : TimedGridRenderer {

    private lateinit var binding: TimedGridBinding
    private lateinit var grid: TimedGridWidgetLayout
    private var derivedState: TimedGridState = TimedGridState.NEW

    override fun inflateView(inflater: LayoutInflater, parent: ViewGroup): View {
        binding = TimedGridBinding.inflate(inflater, parent, false)
        return binding.root
    }

    override fun bind(
        root: View,
        configuration: TimedGridWidgetConfiguration,
        items: List<GridItem>,
        onStart: () -> Unit,
        onComplete: () -> Unit,
        onFinish: () -> Unit,
        onTogglePause: () -> Unit,
    ) {
        binding.buttonStart.setOnClickListener { onStart() }
        binding.buttonComplete.setOnClickListener { onComplete() }
        binding.buttonTimer.setOnClickListener { onTogglePause() }
        binding.buttonFinish.setOnClickListener { onFinish() }

        grid = TimedGridWidgetLayout(
            layoutInflater = LayoutInflater.from(binding.root.context),
            containerView = binding.containerRows,
            columns = configuration.columns,
            rowsPerPage = configuration.rowsPerPage,
            type = configuration.type,
            showRowNumbers = showRowNumbers,
            strictMode = configuration.strict,
            endAfterConsecutive = configuration.endAfterConsecutive,
            onComplete = onComplete,
            allItems = items
        )

        // Pagination buttons
        binding.buttonPrev.setOnClickListener {
            grid.prevPage()
            updatePaginationButtons()
        }
        binding.buttonNext.setOnClickListener {
            grid.nextPage()
            updatePaginationButtons()
        }

        // Enable Finish when assessor picks last item.
        grid.setLastItemSelectionListener { _ ->
            binding.buttonFinish.isEnabled = true
        }

        updatePaginationButtons()
    }

    private fun updatePaginationButtons() {
        binding.buttonPrev.visibility = if (grid.isFirstPage()) View.GONE else View.VISIBLE
        binding.buttonNext.visibility = if (grid.isLastPage()) View.GONE else View.VISIBLE
        binding.buttonComplete.visibility = if (grid.isLastPage() && derivedState == TimedGridState.IN_PROGRESS) View.VISIBLE else View.GONE
    }

    override fun updateUIForState(state: TimedGridState) {
        derivedState = state

        when (state) {
            TimedGridState.NEW -> {
                binding.buttonStart.visibility = View.VISIBLE
                binding.buttonTimer.visibility = View.GONE
                binding.buttonFinish.visibility = View.GONE
                binding.buttonComplete.visibility = View.GONE

                grid.setGridEnabled(false)
            }

            TimedGridState.IN_PROGRESS -> {
                binding.buttonStart.visibility = View.GONE
                binding.buttonTimer.visibility = View.VISIBLE
                binding.buttonFinish.visibility = View.GONE
                binding.buttonComplete.visibility = View.VISIBLE

                grid.setGridEnabled(true)
                updatePaginationButtons()
            }

            TimedGridState.PAUSED -> {
                binding.buttonStart.visibility = View.GONE
                binding.buttonTimer.visibility = View.VISIBLE
                binding.buttonFinish.visibility = View.GONE
                binding.buttonComplete.visibility = View.GONE

                grid.setGridEnabled(false)
                updatePaginationButtons()
            }

            TimedGridState.COMPLETED_NO_LAST_ITEM -> {
                binding.buttonStart.visibility = View.GONE
                binding.buttonTimer.visibility = View.GONE
                binding.buttonFinish.visibility = View.VISIBLE
                binding.buttonComplete.visibility = View.GONE

                val lastAttempted = getLastSelectedLastItemValue()

                if (lastAttempted == null) {
                    // Disable Finish until last-item selected
                    binding.buttonFinish.isEnabled = false

                    // Allow selecting last-item after the last toggled (or any if none)
                    val lastToggled = grid.getLastToggledValue()
                    grid.setEnabledFrom(lastToggled)
                }
            }

            TimedGridState.COMPLETED -> {
                binding.buttonStart.visibility = View.GONE
                binding.buttonTimer.visibility = View.GONE
                binding.buttonFinish.visibility = View.GONE
                binding.buttonComplete.visibility = View.GONE

                // Final visuals: wrong answers + highlight last item
                val wrongAnswers = grid.getToggledAnswers()
                val lastSelected = grid.getLastSelectedLastItemValue()
                grid.revealFinalSelection(wrongAnswers, lastSelected)
            }
        }
    }

    override fun updateTimer(timeString: String) {
        binding.buttonTimer.text = timeString
    }

    override fun getLastSelectedLastItemValue(): String? =
        grid.getLastSelectedLastItemValue()

    override fun getToggledAnswers(): Set<String> =
        grid.getToggledAnswers()

    override fun firstLineAllIncorrect(): Boolean = grid.firstLineSelected()

    override fun restoreAnswers(toggled: Set<String>, last: String?) {
        grid.setToggledAnswers(toggled)
        grid.setLastSelectedLastItemValue(last)
    }
}
