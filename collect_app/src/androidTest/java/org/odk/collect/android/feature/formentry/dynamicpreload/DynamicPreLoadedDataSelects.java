package org.odk.collect.android.feature.formentry.dynamicpreload;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.odk.collect.android.storage.StoragePathProvider;
import org.odk.collect.android.storage.StorageSubdirectory;
import org.odk.collect.android.support.rules.FormEntryActivityTestRule;
import org.odk.collect.android.support.rules.TestRuleChain;

import java.util.Collections;

/**
 * This tests the "Dynamic selects from pre-loaded data" feature of XLSForms.
 * <p>
 * * @see <a href="https://xlsform.org/en/#dynamic-selects-from-pre-loaded-data">Dynamic selects from pre-loaded data</a>
 */
public class DynamicPreLoadedDataSelects {

    private final FormEntryActivityTestRule rule = new FormEntryActivityTestRule();

    @Rule
    public RuleChain copyFormChain = TestRuleChain.chain()
            .around(rule);

    @Test
    public void withoutFilterAndWithoutFilter_displaysMatchingChoices() {
        rule.setUpProjectAndCopyForm("external-csv-search.xml", Collections.singletonList("external-csv-search-produce.csv"))
                .fillNewForm("external-csv-search.xml", "external-csv-search")
                .assertQuestion("Multiple produce")
                .assertText("Artichoke")
                .assertText("Apple")
                .assertText("Banana")
                .assertText("Blueberry")
                .assertText("Cherimoya")
                .assertText("Carrot")

                .swipeToNextQuestion("Produce search")
                .inputText("A")
                .swipeToNextQuestion("Produce")
                .assertText("Artichoke")
                .assertText("Apple")
                .assertText("Banana")
                .assertText("Cherimoya")
                .assertText("Carrot")
                .assertTextDoesNotExist("Blueberry");
    }

    @Test
    public void shouldDisplayFriendlyMessageWhenFilesAreMissing() {
        rule.setUpProjectAndCopyForm("external_data_questions.xml")
                .fillNewForm("external_data_questions.xml", "externalDataQuestions")
                .assertText(org.odk.collect.strings.R.string.file_missing, new StoragePathProvider().getOdkDirPath(StorageSubdirectory.FORMS) + "/external_data_questions-media/fruits.csv")
                .swipeToNextQuestion("External csv")
                .assertText(org.odk.collect.strings.R.string.file_missing, new StoragePathProvider().getOdkDirPath(StorageSubdirectory.FORMS) + "/external_data_questions-media/itemsets.csv");
    }
}
