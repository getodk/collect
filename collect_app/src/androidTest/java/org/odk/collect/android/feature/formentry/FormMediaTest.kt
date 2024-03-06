package org.odk.collect.android.feature.formentry

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith
import org.odk.collect.android.storage.StoragePathProvider
import org.odk.collect.android.storage.StorageSubdirectory
import org.odk.collect.android.support.rules.CollectTestRule
import org.odk.collect.android.support.rules.TestRuleChain.chain

@RunWith(AndroidJUnit4::class)
class FormMediaTest {

    private val rule = CollectTestRule()

    @get:Rule
    val ruleChain: RuleChain = chain().around(rule)

    @Test
    fun loadingFormWithZippedMedia_unzipsIntoMediaDirectory() {
        rule.startAtMainMenu()
            .copyForm("external_select_10.xml", listOf("external_data_10.zip"))
            .startBlankForm("external select 10")
            .clickOnText("a")
            .swipeToNextQuestion("Second")
            .assertText("aa")
            .assertText("ab")
            .assertText("ac")
    }

    @Test
    fun missingFileMessage_shouldBeDisplayedIfExternalFileIsMissing() {
        val formsDirPath = StoragePathProvider().getOdkDirPath(StorageSubdirectory.FORMS)

        rule.startAtMainMenu()
            .copyForm("search_and_select.xml")
            .startBlankForm("search_and_select")
            .assertText("File: $formsDirPath/search_and_select-media/nombre.csv is missing.")
            .assertText("File: $formsDirPath/search_and_select-media/nombre2.csv is missing.")
            .swipeToEndScreen()
            .clickFinalize()
            .copyForm("select_one_external.xml")
            .startBlankForm("cascading select test")
            .clickOnText("Texas")
            .swipeToNextQuestion("county")
            .assertText("File: $formsDirPath/select_one_external-media/itemsets.csv is missing.")
            .swipeToNextQuestion("city")
            .assertText("File: $formsDirPath/select_one_external-media/itemsets.csv is missing.")
            .swipeToEndScreen()
            .clickFinalize()
            .copyForm("fieldlist-updates_nocsv.xml")
            .startBlankForm("fieldlist-updates")
            .clickGoToArrow()
            .clickGoUpIcon()
            .clickOnElementInHierarchy(14)
            .clickOnQuestion("Source15")
            .assertText("File: $formsDirPath/fieldlist-updates_nocsv-media/fruits.csv is missing.")
            .swipeToEndScreen()
            .clickFinalize()
    }
}
