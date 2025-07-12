package org.odk.collect.android.benchmark

import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.odk.collect.android.benchmark.support.Benchmarker
import org.odk.collect.android.benchmark.support.benchmark
import org.odk.collect.android.support.StubOpenRosaServer
import org.odk.collect.android.support.TestDependencies
import org.odk.collect.android.support.pages.MainMenuPage
import org.odk.collect.android.support.rules.CollectTestRule
import org.odk.collect.android.support.rules.TestRuleChain.chain

class FormsUpdateBenchmark {
    private val testDependencies = TestDependencies()
    private val rule = CollectTestRule(useDemoProject = false)

    @get:Rule
    val chain: RuleChain = chain(testDependencies).around(rule)

    @Test
    fun run() {
        val mediaFileItems = (1..100).map { i ->
            StubOpenRosaServer.MediaFileItem("$i.png", "100-media-files/$i.png")
        }

        testDependencies.server.addForm("100-media-files.xml", mediaFileItems)

        val benchmarker = Benchmarker()

        rule.withProject(testDependencies.server.url)
            .clickGetBlankForm()
            .clickGetSelected()
            .clickOKOnDialog(MainMenuPage())
            .benchmark(
                "Fetching form list with many media files when there are no updates",
                2, benchmarker
            ) {
                it
                    .clickGetBlankForm()
                    .clickSelectAll()
            }

        benchmarker.assertResults()
    }
}
