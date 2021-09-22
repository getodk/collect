package org.odk.collect.android.feature.maps;

import android.Manifest;
import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.location.Location;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.rule.GrantPermissionRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.odk.collect.android.R;
import org.odk.collect.android.RecordedIntentsRule;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.support.CollectTestRule;
import org.odk.collect.android.support.CopyFormRule;
import org.odk.collect.android.support.ResetStateRule;
import org.odk.collect.android.support.pages.MainMenuPage;
import org.odk.collect.geo.GeoUtils;

import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;

public class FormMapTest {

    private static final String SINGLE_GEOPOINT_FORM = "single-geopoint.xml";
    private static final String NO_GEOPOINT_FORM = "basic.xml";

    public final CollectTestRule rule = new CollectTestRule();

    @Rule
    public RuleChain copyFormChain = RuleChain
            .outerRule(GrantPermissionRule.grant(Manifest.permission.ACCESS_COARSE_LOCATION))
            .around(new RecordedIntentsRule())
            .around(new ResetStateRule())
            .around(new CopyFormRule(SINGLE_GEOPOINT_FORM))
            .around(new CopyFormRule(NO_GEOPOINT_FORM))
            .around(rule);

    @Before
    public void stubGeopointIntent() {
        Location location = new Location("gps");
        location.setLatitude(125.1);
        location.setLongitude(10.1);
        location.setAltitude(5);

        Intent intent = new Intent();
        intent.putExtra(FormEntryActivity.ANSWER_KEY, GeoUtils.formatLocationResultString(location));
        Instrumentation.ActivityResult result = new Instrumentation.ActivityResult(Activity.RESULT_OK, intent);

        intending(hasComponent("org.odk.collect.geo.GeoPointActivity"))
                .respondWith(result);
    }

    @Test
    public void gettingBlankFormList_showsMapIcon_onlyForFormsWithGeometry() {
        new MainMenuPage()
                .clickFillBlankForm()
                .checkMapIconDisplayedForForm("Single geopoint")
                .checkMapIconNotDisplayedForForm("basic");
    }

    @Test
    public void clickingOnMapIcon_opensMapForForm() {
        new MainMenuPage()
                .clickFillBlankForm()
                .clickOnMapIconForForm("Single geopoint")
                .assertText("Single geopoint");
    }

    @Test
    public void fillingBlankForm_addsInstanceToMap() {
        String oneInstanceString = ApplicationProvider.getApplicationContext().getResources().getString(R.string.geometry_status, 1, 1);

        new MainMenuPage()
                .clickFillBlankForm()
                .clickOnMapIconForForm("Single geopoint")
                .clickFillBlankFormButton("Single geopoint")
                .inputText("Foo")
                .swipeToNextQuestion()
                .clickWidgetButton()
                .swipeToEndScreen()
                .clickSaveAndExitBackToMap()
                .assertText(oneInstanceString);
    }
}
