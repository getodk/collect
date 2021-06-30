package org.odk.collect.android.feature.instancemanagement

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.odk.collect.android.R
import org.odk.collect.android.storage.StorageSubdirectory
import org.odk.collect.android.support.CollectTestRule
import org.odk.collect.android.support.TestDependencies
import org.odk.collect.android.support.TestRuleChain
import org.odk.collect.android.support.pages.MainMenuPage
import java.io.File

@RunWith(AndroidJUnit4::class)
class InstancesAdbTest {

    private val testDependencies = TestDependencies()
    private val rule = CollectTestRule()

    @get:Rule
    val chain: RuleChain = TestRuleChain.chain(testDependencies).around(rule)

    @Test
    fun canAddInstanceOnDisk() {
        rule.startAtMainMenu()
            .copyForm("one-question.xml")
            .clickFillBlankForm() // Add form via disk sync
            .copyInstance("One Question_2021-06-22_15-55-50.xml")
            .pressBack(MainMenuPage()) // Return to main menu to trigger instance disk sync
            .clickEditSavedForm(1)
            .assertText("One Question")
    }

    @Test
    fun deletingInstanceOnDisk_andThenOpeningInstance_showsWarning_andRemovesInstance() {
        val mainMenuPage = rule.startAtMainMenu()
            .copyForm("one-question.xml")
            .startBlankForm("One Question")
            .swipeToEndScreen()
            .clickSaveAndExit()

        val instancesDir = testDependencies.storagePathProvider.getOdkDirPath(StorageSubdirectory.INSTANCES)
        val instanceDeleted = File(instancesDir).listFiles()[0].deleteRecursively()
        Assert.assertTrue(instanceDeleted)

        mainMenuPage
            .clickEditSavedForm(1)
            .clickOnFormWithDialog("One Question")
            .assertText(R.string.instance_deleted_message)
            .clickOK(MainMenuPage())
            .assertNumberOfEditableForms(0)
    }
}
