package org.odk.collect.android.feature.formentry;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.odk.collect.android.storage.StoragePathProvider;
import org.odk.collect.android.storage.StorageSubdirectory;
import org.odk.collect.android.support.rules.CollectTestRule;
import org.odk.collect.android.support.rules.TestRuleChain;

import java.util.Collections;

/**
 * This tests the "External selects" feature of XLSForms. This will often be referred to as "fast
 * external itemsets".
 *
 * @see <a href="https://xlsform.org/en/#external-selects">External selects</a>
 */
public class ExternalSelectsTest {
    public CollectTestRule rule = new CollectTestRule();

    @Rule
    public RuleChain copyFormChain = TestRuleChain.chain()
            .around(rule);

    @Test
    public void displaysAllChoicesFromItemsetsCSV() {
        rule.startAtMainMenu()
                .copyForm("selectOneExternal.xml", Collections.singletonList("selectOneExternal-media/itemsets.csv"))
                .startBlankForm("selectOneExternal")
                .clickOnText("Texas")
                .swipeToNextQuestion("county")
                .assertText("King")
                .assertText("Cameron");
    }

    @Test
    public void missingFileMessage_shouldBeDisplayedIfExternalFileWithChoicesIsMissing() {
        String formsDirPath = new StoragePathProvider().getOdkDirPath(StorageSubdirectory.FORMS);

        rule.startAtMainMenu()
                .copyForm("select_one_external.xml")
                .startBlankForm("cascading select test")
                .clickOnText("Texas")
                .swipeToNextQuestion("county")
                .assertText("File: $formsDirPath/select_one_external-media/itemsets.csv is missing.")
                .swipeToNextQuestion("city")
                .assertText("File: $formsDirPath/select_one_external-media/itemsets.csv is missing.");
    }

    @Test
    public void missingFileMessage_shouldBeDisplayedIfExternalFileWithChoicesUsedBySearchFunctionIsMissing() {
        String formsDirPath = new StoragePathProvider().getOdkDirPath(StorageSubdirectory.FORMS);

        rule.startAtMainMenu()
                .copyForm("search_and_select.xml")
                .startBlankForm("search_and_select")
                .assertText("File: $formsDirPath/search_and_select-media/nombre.csv is missing.")
                .assertText("File: $formsDirPath/search_and_select-media/nombre2.csv is missing.");
    }
}
