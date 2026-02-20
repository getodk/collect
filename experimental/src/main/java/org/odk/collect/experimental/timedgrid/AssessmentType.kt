package org.odk.collect.experimental.timedgrid

enum class AssessmentType(
    private val rendererFactory: () -> TimedGridRenderer
) {
    LETTERS(::CommonTimedGridRenderer),
    WORDS(::CommonTimedGridRenderer),
    NUMBERS(::CommonTimedGridRenderer),
    ARITHMETIC(::CommonTimedGridRenderer),
    READING({ CommonTimedGridRenderer(showRowNumbers = false) });

    fun createRenderer(): TimedGridRenderer = rendererFactory()
}
