package org.odk.collect.android.feature.formentry;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.odk.collect.android.support.rules.FormActivityTestRule;
import org.odk.collect.android.support.rules.TestRuleChain;

import java.util.Collections;

/**
 * This tests the "Dynamic selects from pre-loaded data" feature of XLSForms.
 *
 * * @see <a href="https://xlsform.org/en/#dynamic-selects-from-pre-loaded-data">Dynamic selects from pre-loaded data</a>
 */
public class DynamicPreLoadedDataSelects {

    private static final String EXTERNAL_CSV_SEARCH_FORM = "external-csv-search.xml";

    public FormActivityTestRule rule = new FormActivityTestRule(EXTERNAL_CSV_SEARCH_FORM, "external-csv-search", Collections.singletonList("external-csv-search-produce.csv"));

    @Rule
    public RuleChain copyFormChain = TestRuleChain.chain()
            .around(rule);

    @Test
    public void withoutFilter_displaysAllChoices() {
        rule.startInFormEntry()
                .assertOnPage()
                .assertText("Artichoke")
                .assertText("Apple")
                .assertText("Banana")
                .assertText("Blueberry")
                .assertText("Cherimoya")
                .assertText("Carrot");
    }

    @Test
    // Regression: https://github.com/getodk/collect/issues/3132
    public void withFilter_showsMatchingChoices() {
        rule.startInFormEntry()
                .assertOnPage()
                .swipeToNextQuestion("Produce search")
                .inputText("A")
                .swipeToNextQuestion("Produce")
                .assertText("Artichoke")
                .assertText("Apple")
                .assertText("Banana")
                .assertText("Cherimoya")
                .assertText("Carrot")
                .assertTextDoesNotExist("Blueberry")
                .swipeToPreviousQuestion("Produce search")
                .inputText("B")
                .swipeToNextQuestion("Produce")
                .assertText("Banana")
                .assertText("Blueberry")
                .assertTextDoesNotExist("Artichoke")
                .assertTextDoesNotExist("Apple")
                .assertTextDoesNotExist("Cherimoya")
                .assertTextDoesNotExist("Carrot");
    }
}
