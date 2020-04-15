package org.odk.collect.android.preferences.qr;

import android.Manifest;
import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.FragmentFactory;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.MainMenuActivity;
import org.odk.collect.android.support.ResetStateRule;
import org.odk.collect.android.support.pages.MainMenuPage;
import org.odk.collect.android.support.pages.QRCodeTabsActivityPage;

import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.filterEquals;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasType;
import static androidx.test.espresso.intent.matcher.IntentMatchers.isInternal;
import static androidx.test.espresso.intent.matcher.IntentMatchers.toPackage;
import static org.hamcrest.Matchers.not;


@RunWith(AndroidJUnit4.class)
public class QrCodeActivitiesTest {
    @Rule
    public IntentsTestRule<QRCodeTabsActivity> rule = new IntentsTestRule<>(QRCodeTabsActivity.class);

    @Rule
    public RuleChain copyFormChain = RuleChain
            .outerRule(GrantPermissionRule.grant(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_PHONE_STATE
            ))
            .around(new ResetStateRule());

    @Before
    public void stubAllExternalIntents() {
        // By default Espresso Intents does not stub any Intents. Stubbing needs to be setup before
        // every test run. In this case all external Intents will be blocked.
        intending(not(isInternal())).respondWith(new Instrumentation.ActivityResult(Activity.RESULT_OK, null));
    }

    @Test
    public void testScanner() {
        new QRCodeTabsActivityPage(rule)
                .assertOnPage()
                .clickScanFragment()
                .checkIsIdDisplayed(R.id.zxing_barcode_surface);
    }

    @Test
    public void testView() {
        new QRCodeTabsActivityPage(rule)
                .assertOnPage()
                .clickViewQRFragment()
                .checkIsIdDisplayed(R.id.ivQRcode)
                .checkIfElementIsGone(R.id.progressBar)
                .assertImageViewShowsImage(R.id.ivQRcode);

    }

    @Test
    public void testPressImportQRCode() {
        new QRCodeTabsActivityPage(rule)
                .clickOnMenu()
                .clickOnString(R.string.import_qrcode_sd);
        
        intended(hasAction(Intent.ACTION_PICK));
        intended(hasType("image/*"));
    }

    @Test
    public void testPressShareQRCode() {
        new QRCodeTabsActivityPage(rule)
                .clickOnId(R.id.menu_item_share);

        intended(hasAction(Intent.ACTION_CHOOSER));
    }


}
