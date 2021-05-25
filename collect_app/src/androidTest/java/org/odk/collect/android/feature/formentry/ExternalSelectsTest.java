package org.odk.collect.android.feature.formentry;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.odk.collect.android.support.CollectTestRule;
import org.odk.collect.android.support.CopyFormRule;
import org.odk.collect.android.support.ResetStateRule;
import org.odk.collect.android.support.pages.MainMenuPage;

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
    public RuleChain copyFormChain = RuleChain
            .outerRule(new ResetStateRule())
            .around(new CopyFormRule("selectOneExternal.xml", Collections.singletonList("selectOneExternal-media/itemsets.csv")))
            .around(rule);

    @Test
    public void displaysAllChoicesFromItemsetsCSV() {
        new MainMenuPage()
                .startBlankForm("selectOneExternal")
                .clickOnText("Texas")
                .swipeToNextQuestion("county")
                .assertText("King")
                .assertText("Cameron");
    }
}
