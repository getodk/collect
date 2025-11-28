package org.odk.collect.shared

/**
 * Simple helper to allow writing neater [QuickCheck](https://en.wikipedia.org/wiki/QuickCheck)
 * style tests.
 */
fun <Input, Output> ((Input) -> Output).quickCheck(
    iterations: Int,
    generator: Sequence<Input>,
    checks: (Input, Output) -> Unit
) {
    generator.take(iterations).forEach { input ->
        val output = this(input)
        checks(input, output)
    }
}
