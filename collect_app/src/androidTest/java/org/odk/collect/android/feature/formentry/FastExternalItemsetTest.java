package org.odk.collect.android.feature.formentry;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.odk.collect.android.support.CollectTestRule;
import org.odk.collect.android.support.CopyFormRule;
import org.odk.collect.android.support.ResetStateRule;
import org.odk.collect.android.support.pages.MainMenuPage;

import java.util.Collections;

public class FastExternalItemsetTest {
    public CollectTestRule rule = new CollectTestRule();

    @Rule
    public RuleChain copyFormChain = RuleChain
            .outerRule(new ResetStateRule())
            .around(new CopyFormRule("selectOneExternal.xml", Collections.singletonList("selectOneExternal-media/itemsets.csv")))
            .around(rule);

    @Test
    public void testFastExternalItemset() {
        new MainMenuPage()
                .startBlankForm("selectOneExternal")
                .clickOnText("Texas")
                .swipeToNextQuestion("county")
                .clickOnText("King")
                .swipeToNextQuestion("city")
                .clickOnText("Dumont")
                .swipeToNextQuestion("state")
                .openSelectMinimalDialog()
                .clickOnText("Washington")
                .swipeToNextQuestion("county")
                .openSelectMinimalDialog()
                .clickOnText("Pierce")
                .swipeToNextQuestion("city")
                .openSelectMinimalDialog()
                .clickOnText("Puyallup")
                .swipeToEndScreen()
                .clickSaveAndExit();

        new MainMenuPage()
                .clickEditSavedForm()
                .clickOnForm("selectOneExternal")
                .assertText("Texas", "King", "Dumont", "Washington", "Pierce", "Puyallup")
                .clickOnQuestion("Texas")
                .swipeToNextQuestion("county")
                .swipeToNextQuestion("city")
                .swipeToNextQuestion("state")
                .swipeToNextQuestion("county")
                .swipeToNextQuestion("city")
                .swipeToEndScreen()
                .clickSaveAndExit();
    }
}
