package org.odk.collect.android.feature.formentry;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.odk.collect.android.R;
import org.odk.collect.android.storage.StoragePathProvider;
import org.odk.collect.android.storage.StorageSubdirectory;
import org.odk.collect.android.support.rules.FormActivityTestRule;
import org.odk.collect.android.support.rules.TestRuleChain;

public class ExternalDataFileNotFoundTest {
    private static final String EXTERNAL_DATA_QUESTIONS = "external_data_questions.xml";

    public FormActivityTestRule activityTestRule = new FormActivityTestRule(EXTERNAL_DATA_QUESTIONS, "externalDataQuestions");

    @Rule
    public RuleChain copyFormChain = TestRuleChain.chain()
            .around(activityTestRule);

    @Test
    public void questionsThatUseExternalFiles_ShouldDisplayFriendlyMessageWhenFilesAreMissing() {
        String formsDirPath = new StoragePathProvider().getOdkDirPath(StorageSubdirectory.FORMS);

        activityTestRule.startInFormEntry()
                .assertText(R.string.file_missing, formsDirPath + "/external_data_questions-media/fruits.csv")
                .swipeToNextQuestion("External csv")
                .assertText(R.string.file_missing, formsDirPath + "/external_data_questions-media/itemsets.csv");
    }
}
