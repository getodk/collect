package org.odk.collect.android.preferences.qr;

import android.Manifest;
import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;
import dagger.Provides;
import io.reactivex.Observable;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.MainMenuActivity;
import org.odk.collect.android.injection.config.AppDependencyModule;
import org.odk.collect.android.support.ResetStateRule;
import org.odk.collect.android.support.pages.MainMenuPage;

import java.util.Collection;

import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasType;
import static androidx.test.espresso.intent.matcher.IntentMatchers.isInternal;
import static org.hamcrest.Matchers.not;


@RunWith(AndroidJUnit4.class)
public class QrCodeActivitiesTest {
    // drawable resource that will act as "qr code" in this test
    private final int checkerBackgroundDrawableId = R.drawable.checker_background;

    @Rule
    public IntentsTestRule<MainMenuActivity> rule = new IntentsTestRule<>(MainMenuActivity.class);

    @Rule
    public RuleChain copyFormChain = RuleChain
            .outerRule(GrantPermissionRule.grant(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_PHONE_STATE
            ))
            .around(new ResetStateRule(new AppDependencyModule() {
                @Override
                @Provides
                public QRCodeGenerator providesQRCodeGenerator() {
                    return new QRCodeGenerator() {
                        @Override
                        public Observable<Bitmap> generateQRCode(Collection<String> selectedPasswordKeys) {
                            return Observable.create(emitter -> {
                                Bitmap bitmap =
                                        BitmapFactory.decodeResource(
                                                ApplicationProvider.getApplicationContext().getResources(),
                                                checkerBackgroundDrawableId);
                                emitter.onNext(bitmap);
                                emitter.onComplete();
                            });
                        }
                    };
                }
            }));

    @Before
    public void stubAllExternalIntents() {
        // By default Espresso Intents does not stub any Intents. Stubbing needs to be setup before
        // every test run. In this case all external Intents will be blocked.
        intending(not(isInternal())).respondWith(new Instrumentation.ActivityResult(Activity.RESULT_OK, null));
    }

    @Test
    public void checkQRScannerIsInitiated() {
        new MainMenuPage(rule)
                .assertOnPage()
                .clickOnMenu()
                .clickConfigureQR()
                .clickScanFragment()
                .checkIsIdDisplayed(R.id.zxing_barcode_surface);
    }

    @Test
    public void checkQRCodeImageViewDisplaysImage() {
        new MainMenuPage(rule)
                .assertOnPage()
                .clickOnMenu()
                .clickConfigureQR()
                .clickViewQRFragment()
                .assertImageViewShowsImage(R.id.ivQRcode, BitmapFactory.decodeResource(
                        ApplicationProvider.getApplicationContext().getResources(),
                        checkerBackgroundDrawableId
                ));
    }

    @Test
    public void pressImportQRCode() {
        new MainMenuPage(rule)
                .assertOnPage()
                .clickOnMenu()
                .clickConfigureQR()
                .clickOnMenu()
                .clickOnString(R.string.import_qrcode_sd);
        
        intended(hasAction(Intent.ACTION_PICK));
        intended(hasType("image/*"));
    }

    @Test
    public void pressShareQRCode() {
        new MainMenuPage(rule)
                .assertOnPage()
                .clickOnMenu()
                .clickConfigureQR()
                .clickOnId(R.id.menu_item_share);

        intended(hasAction(Intent.ACTION_CHOOSER));
    }
}
