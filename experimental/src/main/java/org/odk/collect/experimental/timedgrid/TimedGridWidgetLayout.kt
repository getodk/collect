package org.odk.collect.experimental.timedgrid

import android.content.res.ColorStateList
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import org.odk.collect.androidshared.system.ContextUtils.getThemeAttributeValue
import org.odk.collect.experimental.R
import org.odk.collect.experimental.databinding.TimedGridItemButtonBinding
import org.odk.collect.experimental.databinding.TimedGridItemRowBinding

data class GridItem(val value: String, val text: String)

private enum class ButtonFinalState { DEFAULT, WRONG, LAST }

/**
 * Compact grid used by the timed-grid widget.
 *
 * Responsibilities:
 *  - render items as MaterialButtons
 *  - track toggled (attempted/wrong) answers
 *  - support enabling a suffix of buttons for "last-item" selection
 *  - notify on last-item selection
 *  - reveal final visuals (wrong + last)
 */
class TimedGridWidgetLayout(
    private val layoutInflater: LayoutInflater,
    private val containerView: ViewGroup,
    private val columns: Int,
    private val rowsPerPage: Int,
    private val type: AssessmentType,
    private val showRowNumbers: Boolean,
    private val strictMode: Boolean,
    private val endAfterConsecutive: Int,
    private val onComplete: () -> Unit,
    private val allItems: List<GridItem>
) {
    private val toggledAnswers = mutableSetOf<String>()
    private val buttons = mutableListOf<MaterialButton>()
    private val valueForButton = mutableMapOf<MaterialButton, String>()

    // original visuals so we can restore them
    private val originalTextColor = mutableMapOf<MaterialButton, Int>()
    private val originalBackgroundTint = mutableMapOf<MaterialButton, ColorStateList?>()

    private var lastItemSelectionListener: ((String) -> Unit)? = null

    // marker for last-item mode (start index from setEnabledFrom). null = not in mode
    private var lastItemModeIndex: Int? = null

    private var lastSelectedLastItemValue: String? = null

    // Render all items in a single row container instead of chunking into multiple numbered rows
    private val shouldRenderAsSingleRow = columns == 1 && type == AssessmentType.READING

    private val orderedValues = mutableListOf<String>()
    private val valueToIndexMap = mutableMapOf<String, Int>()

    /**
     * Determines whether the user has chosen to continue the assessment
     * after exceeding the `end-after` consecutive mistakes limit.
     */
    private var userChoseToContinueAfterLimit = false

    // Pagination
    private var currentPage = 0
    private var totalRows = 0

    private companion object {
        const val MARGIN_PERCENT_PER_BUTTON = 0.0025f
        const val SMALL_SCREEN_DP_THRESHOLD = 600
        const val MULTIPLE_ROWS_COLUMN_THRESHOLD = 5
        val ONLY_PUNCTUATION_REGEX = Regex("^\\p{Punct}+$")
    }

    init {
        allItems.forEachIndexed { index, item ->
            orderedValues.add(item.value)
            valueToIndexMap[item.value] = index
        }

        buildAllRows()

        showPage(0)
        setGridEnabled(false)
    }

    private fun buildAllRows() {
        val rows = if (shouldRenderAsSingleRow) {
            listOf(allItems)
        } else {
            allItems.chunked(columns)
        }
        totalRows = rows.size

        rows.forEachIndexed { rowIndex, rowItems ->
            val rowBinding = TimedGridItemRowBinding.inflate(layoutInflater, containerView, false)

            if (showRowNumbers) {
                rowBinding.textviewRowNumber.text = containerView.context.getString(
                    R.string.timed_grid_row_number,
                    rowIndex + 1
                )
            } else {
                rowBinding.textviewRowNumber.visibility = View.GONE
            }

            populateRow(layoutInflater, rowBinding, rowItems)
            containerView.addView(rowBinding.root)
        }
    }

    private fun populateRow(
        layoutInflater: LayoutInflater,
        rowBinding: TimedGridItemRowBinding,
        rowItems: List<GridItem>
    ) {
        val flexBasisPercent = calculateFlexBasisPercent()

        for (item in rowItems) {
            val button = TimedGridItemButtonBinding.inflate(
                layoutInflater, rowBinding.containerWordButtons, false
            ).root

            button.text = item.text
            buttons.add(button)
            valueForButton[button] = item.value

            originalTextColor[button] = button.currentTextColor
            originalBackgroundTint[button] = button.backgroundTintList

            button.setOnClickListener { handleButtonClick(button, item) }

            val lp = button.layoutParams as com.google.android.flexbox.FlexboxLayout.LayoutParams
            if (shouldRenderAsSingleRow) {
                lp.flexGrow = 1f
                button.setPadding(24, 0, 24, 0)
            } else {
                lp.flexBasisPercent = flexBasisPercent
                button.layoutParams = lp
            }

            rowBinding.containerWordButtons.addView(button)
        }
    }

    private fun showPage(pageIndex: Int) {
        currentPage = pageIndex
        val startRow = pageIndex * rowsPerPage
        val endRow = startRow + rowsPerPage

        for (i in 0 until containerView.childCount) {
            val rowView = containerView.getChildAt(i)
            rowView.visibility = if (i in startRow until endRow) View.VISIBLE else View.GONE
        }
    }

    fun nextPage() {
        if (currentPage < getTotalPages() - 1) {
            showPage(currentPage + 1)
        }
    }

    fun prevPage() {
        if (currentPage > 0) {
            showPage(currentPage - 1)
        }
    }

    fun isFirstPage() = currentPage == 0
    fun isLastPage() = currentPage == getTotalPages() - 1
    private fun getTotalPages() = (totalRows + rowsPerPage - 1) / rowsPerPage

    fun setGridEnabled(isEnabled: Boolean) {
        lastItemModeIndex = null
        buttons.forEach { button ->
            val isPunctuationInReading = type == AssessmentType.READING && ONLY_PUNCTUATION_REGEX.matches(button.text)
            button.isEnabled = isEnabled && !isPunctuationInReading
        }
    }

    fun setEnabledFrom(valueFrom: String?) {
        val idx = buttons.indexOfFirst { valueForButton[it] == valueFrom }
        val startIndex = idx + 1
        lastItemModeIndex = startIndex.coerceAtMost(buttons.size)
        buttons.forEachIndexed { i, btn -> btn.isEnabled = i >= startIndex }
    }

    fun setLastItemSelectionListener(listener: ((String) -> Unit)?) {
        lastItemSelectionListener = listener
    }

    fun getLastToggledValue(): String? {
        for (i in buttons.size - 1 downTo 0) {
            val value = valueForButton[buttons[i]]
            if (value != null && value in toggledAnswers) return value
        }
        return null
    }

    fun getLastSelectedLastItemValue(): String? = lastSelectedLastItemValue
    fun getToggledAnswers(): Set<String> = toggledAnswers.toSet()

    fun revealFinalSelection(wrongAnswers: Set<String>, lastItemValue: String?) {
        buttons.forEach { btn ->
            val value = valueForButton[btn]
            val state = when {
                value != null && value == lastItemValue -> ButtonFinalState.LAST
                value != null && value in wrongAnswers -> ButtonFinalState.WRONG
                else -> ButtonFinalState.DEFAULT
            }
            applyFinalVisual(btn, state)
            btn.isEnabled = false
        }
        lastSelectedLastItemValue = lastItemValue
    }

    private fun calculateFlexBasisPercent(): Float {
        var desiredCols = columns
        if (isSmallScreen() && type == AssessmentType.LETTERS && columns > MULTIPLE_ROWS_COLUMN_THRESHOLD) {
            desiredCols /= 2
        }

        if (showRowNumbers) {
            desiredCols += 1
        }

        return (1f - desiredCols * MARGIN_PERCENT_PER_BUTTON) / desiredCols
    }

    private fun isSmallScreen(): Boolean {
        val displayMetrics = containerView.context.resources.displayMetrics
        val dpWidth = displayMetrics.widthPixels / displayMetrics.density
        return dpWidth < SMALL_SCREEN_DP_THRESHOLD
    }

    private fun handleButtonClick(button: MaterialButton, item: GridItem) {
        val inLastItemMode = lastItemModeIndex != null && button.isEnabled

        if (inLastItemMode) {
            MaterialAlertDialogBuilder(button.context)
                .setTitle(R.string.last_attempted_item)
                .setMessage(R.string.confirm_last_attempted_item)
                .setPositiveButton(org.odk.collect.strings.R.string.yes) { _, _ ->
                    // User confirmed -> pick last item: keep other visuals intact
                    lastSelectedLastItemValue = item.value
                    applyFinalVisual(button, ButtonFinalState.LAST)
                    lastItemSelectionListener?.invoke(item.value)
                    lastItemModeIndex = null
                    buttons.forEach { it.isEnabled = false }
                }
                .setNegativeButton(org.odk.collect.strings.R.string.no) { _, _ ->
                    // User declined -> remain in last-item mode so they can pick a different one.
                }
                .show()

            return
        }

        // Normal toggle
        val isSelected = item.value in toggledAnswers

        if (isSelected) {
            toggledAnswers.remove(item.value)
        } else {
            toggledAnswers.add(item.value)

            val currentIndex = valueToIndexMap[item.value] ?: return

            val backwardCount = countConsecutiveWrongFrom(currentIndex - 1, -1)
            val forwardCount = countConsecutiveWrongFrom(currentIndex + 1, 1)

            val totalStreak = backwardCount + 1 + forwardCount

            if (endAfterConsecutive in 1..totalStreak) {
                updateButtonVisuals(button, !isSelected)

                if (strictMode) {
                    showLimitExceededDialog(button)
                } else {
                    if (userChoseToContinueAfterLimit) {
                        return
                    }
                    showConsecutiveMistakesChoiceDialog(button)
                }
                return
            }
        }

        updateButtonVisuals(button, !isSelected)
    }

    private fun showLimitExceededDialog(button: MaterialButton) {
        MaterialAlertDialogBuilder(button.context)
            .setTitle(R.string.test_ended_early_title)
            .setMessage(
                button.context.getString(
                    R.string.test_ended_early_message,
                    endAfterConsecutive
                )
            )
            .setPositiveButton(org.odk.collect.strings.R.string.ok) { _, _ ->
                onComplete.invoke()
            }
            .setCancelable(false)
            .show()
    }

    private fun showConsecutiveMistakesChoiceDialog(button: MaterialButton) {
        MaterialAlertDialogBuilder(button.context)
            .setTitle(R.string.consecutive_mistakes_title)
            .setMessage(
                button.context.getString(
                    R.string.consecutive_mistakes_message,
                    endAfterConsecutive
                )
            )
            .setPositiveButton(R.string.end_test) { _, _ ->
                onComplete.invoke()
            }
            .setNegativeButton(R.string.continue_test) { _, _ ->
                userChoseToContinueAfterLimit = true
            }
            .show()
    }

    private fun countConsecutiveWrongFrom(startIndex: Int, step: Int): Int {
        var count = 0
        var index = startIndex

        while (true) {
            val value = orderedValues.getOrNull(index) ?: break
            val item = allItems.getOrNull(index) ?: break

            // Skip punctuation items (don’t count them, don’t break streak) in READING assessments
            if (type == AssessmentType.READING && ONLY_PUNCTUATION_REGEX.matches(item.text)) {
                index += step
                continue
            }

            if (value in toggledAnswers) {
                count++
                index += step
            } else {
                break
            }
        }

        return count
    }

    private fun updateButtonVisuals(button: MaterialButton, isSelected: Boolean) {
        button.isSelected = isSelected
        if (isSelected) {
            button.paintFlags = button.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        } else {
            button.paintFlags = button.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        }
    }

    fun setToggledAnswers(values: Set<String>) {
        toggledAnswers.clear()
        toggledAnswers.addAll(values)

        // Refresh visuals for currently rendered buttons
        for (btn in buttons) {
            val v = valueForButton[btn]
            val selected = v != null && v in toggledAnswers
            btn.isSelected = selected
            btn.paintFlags = if (selected) {
                btn.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                btn.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }
        }
    }

    /**
     * Set last-selected last-item value (visual only). Pass null to clear.
     */
    fun setLastSelectedLastItemValue(value: String?) {
        // Clear previous
        lastSelectedLastItemValue?.let { prev ->
            val prevBtn = buttons.find { valueForButton[it] == prev }
            prevBtn?.let { applyFinalVisual(it, ButtonFinalState.DEFAULT) }
        }

        lastSelectedLastItemValue = value
        value?.let { v ->
            val btn = buttons.find { valueForButton[it] == v }
            btn?.let { applyFinalVisual(it, ButtonFinalState.LAST) }
        }
    }

    fun firstLineSelected(): Boolean {
        if (buttons.size < columns || type == AssessmentType.READING) {
            return false
        }
        val firstLineButtons = buttons.take(columns)
        return firstLineButtons.all { it.isSelected }
    }

    /**
     * Apply final look for a button:
     *  - DEFAULT: restore originals
     *  - WRONG: selected + strike-through + error text color
     *  - LAST: selected + no strike-through + green background tint
     */
    private fun applyFinalVisual(button: MaterialButton, state: ButtonFinalState) {
        when (state) {
            ButtonFinalState.DEFAULT -> {
                originalBackgroundTint[button]?.let { button.backgroundTintList = it }
                originalTextColor[button]?.let { button.setTextColor(it) }
                button.paintFlags = button.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                button.isSelected = false
            }
            ButtonFinalState.WRONG -> {
                button.isSelected = true
                button.paintFlags = button.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                button.setTextColor(getThemeAttributeValue(button.context, androidx.appcompat.R.attr.colorError))
            }
            ButtonFinalState.LAST -> {
                button.isSelected = true
                button.paintFlags = button.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                button.backgroundTintList = ContextCompat.getColorStateList(
                    button.context,
                    R.color.timedGridButtonGreenLastAnswer
                )
            }
        }
    }
}
