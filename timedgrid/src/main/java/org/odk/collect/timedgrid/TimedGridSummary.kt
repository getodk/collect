package org.odk.collect.timedgrid

data class TimedGridSummary(
    val secondsRemaining: Int,
    val attemptedCount: Int,
    val incorrectCount: Int,
    val correctCount: Int,
    val firstLineAllIncorrect: Boolean,
    val sentencesPassed: Int,
    val correctItems: String,
    val unansweredItems: String,
    val punctuationCount: Int,
) {
    data class Builder(
        var secondsRemaining: Int = 0,
        var attemptedCount: Int = 0,
        var incorrectCount: Int = 0,
        var correctCount: Int = 0,
        var firstLineAllIncorrect: Boolean = false,
        var sentencesPassed: Int = 0,
        var correctItems: String = "",
        var unansweredItems: String = "",
        var punctuationCount: Int = 0,
    ) {
        fun secondsRemaining(secondsRemaining: Int) = apply { this.secondsRemaining = secondsRemaining }
        fun attemptedCount(attemptedCount: Int) = apply { this.attemptedCount = attemptedCount }
        fun incorrectCount(incorrectCount: Int) = apply { this.incorrectCount = incorrectCount }
        fun correctCount(correctCount: Int) = apply { this.correctCount = correctCount }
        fun firstLineAllIncorrect(firstLineAllIncorrect: Boolean) = apply { this.firstLineAllIncorrect = firstLineAllIncorrect }
        fun sentencesPassed(sentencesPassed: Int) = apply { this.sentencesPassed = sentencesPassed }
        fun correctItems(correctItems: String) = apply { this.correctItems = correctItems }
        fun unansweredItems(unansweredItems: String) = apply { this.unansweredItems = unansweredItems }
        fun punctuationCount(punctuationCount: Int) = apply { this.punctuationCount = punctuationCount }
        fun build() = TimedGridSummary(
            secondsRemaining,
            attemptedCount,
            incorrectCount,
            correctCount,
            firstLineAllIncorrect,
            sentencesPassed,
            correctItems,
            unansweredItems,
            punctuationCount
        )
    }
}
