package org.odk.collect.android.feature.formentry

import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.odk.collect.android.storage.StoragePathProvider
import org.odk.collect.android.storage.StorageSubdirectory
import org.odk.collect.android.support.rules.CollectTestRule
import org.odk.collect.android.support.rules.TestRuleChain.chain

/**
 * This tests the "External selects" feature of XLSForms. This will often be referred to as "fast
 * external itemsets".
 *
 * @see [External selects](https://xlsform.org/en/.external-selects)
 */
class ExternalSelectsTest {
    private var rule: CollectTestRule = CollectTestRule()

    @get:Rule
    val copyFormChain: RuleChain = chain()
        .around(rule)

    @Test
    fun displaysAllChoicesFromItemsetsCSV() {
        rule.startAtMainMenu()
            .copyForm("selectOneExternal.xml", listOf("selectOneExternal-media/itemsets.csv"))
            .startBlankForm("selectOneExternal")
            .clickOnText("Texas")
            .swipeToNextQuestion("county")
            .assertText("King")
            .assertText("Cameron")
    }

    @Test
    fun missingFileMessage_shouldBeDisplayedIfExternalFileWithChoicesIsMissing() {
        val formsDirPath = StoragePathProvider().getOdkDirPath(StorageSubdirectory.FORMS)

        rule.startAtMainMenu()
            .copyForm("select_one_external.xml")
            .startBlankForm("cascading select test")
            .clickOnText("Texas")
            .swipeToNextQuestion("county")
            .assertText("File: $formsDirPath/select_one_external-media/itemsets.csv is missing.")
            .swipeToNextQuestion("city")
            .assertText("File: $formsDirPath/select_one_external-media/itemsets.csv is missing.")
    }

    @Test
    fun missingFileMessage_shouldBeDisplayedIfExternalFileWithChoicesUsedBySearchFunctionIsMissing() {
        val formsDirPath = StoragePathProvider().getOdkDirPath(StorageSubdirectory.FORMS)

        rule.startAtMainMenu()
            .copyForm("search_and_select.xml")
            .startBlankForm("search_and_select")
            .assertText("File: $formsDirPath/search_and_select-media/nombre.csv is missing.")
            .assertText("File: $formsDirPath/search_and_select-media/nombre2.csv is missing.")
    }
}
