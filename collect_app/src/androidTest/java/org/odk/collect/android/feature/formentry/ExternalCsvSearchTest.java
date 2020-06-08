package org.odk.collect.android.feature.formentry;

import android.Manifest;

import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.rule.GrantPermissionRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.support.CopyFormRule;
import org.odk.collect.android.support.FormLoadingUtils;
import org.odk.collect.android.support.ResetStateRule;
import org.odk.collect.android.support.pages.FormEntryPage;

import java.util.Collections;

public class ExternalCsvSearchTest {

    private static final String EXTERNAL_CSV_SEARCH_FORM = "external-csv-search.xml";

    public IntentsTestRule<FormEntryActivity> rule = FormLoadingUtils.getFormActivityTestRuleFor(EXTERNAL_CSV_SEARCH_FORM);

    @Rule
    public RuleChain copyFormChain = RuleChain
            .outerRule(GrantPermissionRule.grant(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
            )
            .around(new ResetStateRule())
            .around(new CopyFormRule(EXTERNAL_CSV_SEARCH_FORM, Collections.singletonList("external-csv-search-produce.csv"), true))
            .around(rule);

    @Test
    public void search_withoutFilter_displaysAllChoices() {
        new FormEntryPage("external-csv-search", rule).assertOnPage()
                .assertText("Artichoke")
                .assertText("Apple")
                .assertText("Banana")
                .assertText("Blueberry")
                .assertText("Cherimoya")
                .assertText("Carrot");
    }

    @Test
    // Regression: https://github.com/opendatakit/collect/issues/3132
    public void search_withFilter_showsMatchingChoices() {
        new FormEntryPage("external-csv-search", rule).assertOnPage()
                .swipeToNextQuestion("Produce search")
                .inputText("A")
                .swipeToNextQuestion("Produce")
                .assertText("Artichoke")
                .assertText("Apple")
                .assertText("Banana")
                .assertText("Cherimoya")
                .assertText("Carrot")
                .checkIfTextDoesNotExist("Blueberry")

                .swipeToPreviousQuestion()
                .inputText("B")
                .swipeToNextQuestion("Produce")
                .assertText("Banana")
                .assertText("Blueberry")
                .checkIfTextDoesNotExist("Artichoke")
                .checkIfTextDoesNotExist("Apple")
                .checkIfTextDoesNotExist("Cherimoya")
                .checkIfTextDoesNotExist("Carrot");
    }
}
