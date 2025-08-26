package org.odk.collect.android.feature.formentry

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.odk.collect.android.support.TestDependencies
import org.odk.collect.android.support.rules.CollectTestRule
import org.odk.collect.android.support.rules.TestRuleChain.chain

@RunWith(AndroidJUnit4::class)
class AudioAutoplayTest {
    private val testDependencies = TestDependencies()
    private val rule = CollectTestRule()

    @get:Rule
    val copyFormChain: RuleChain = chain(testDependencies).around(rule)

    @Test
    fun whenAudioWithAutoplayInFirstQuestion_playAudioAutomaticallyAfterFormOpen() {
        rule.startAtMainMenu()
            .copyForm("one-question-autoplay.xml", listOf("sampleAudio.wav"))
            .startBlankForm("One Question Autoplay")

        assertThat(testDependencies.audioPlayerFactory.audioPlayer.playedClips, equalTo(1))
    }
}
