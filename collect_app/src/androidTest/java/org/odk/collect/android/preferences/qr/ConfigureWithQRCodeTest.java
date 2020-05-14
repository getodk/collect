package org.odk.collect.android.preferences.qr;

import android.Manifest;
import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import androidx.core.content.FileProvider;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import com.google.zxing.WriterException;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.odk.collect.android.BuildConfig;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.MainMenuActivity;
import org.odk.collect.android.injection.config.AppDependencyModule;
import org.odk.collect.android.support.ResetStateRule;
import org.odk.collect.android.support.RunnableRule;
import org.odk.collect.android.support.pages.MainMenuPage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;

import dagger.Provides;
import io.reactivex.Observable;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.BundleMatchers.hasEntry;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasExtraWithKey;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasExtras;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasType;
import static androidx.test.espresso.intent.matcher.IntentMatchers.isInternal;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertTrue;


@RunWith(AndroidJUnit4.class)
public class ConfigureWithQRCodeTest {

    private final StubQRCodeGenerator stubQRCodeGenerator = new StubQRCodeGenerator();

    public IntentsTestRule<MainMenuActivity> rule = new IntentsTestRule<>(MainMenuActivity.class);

    @Rule
    public RuleChain copyFormChain = RuleChain
            .outerRule(GrantPermissionRule.grant(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.CAMERA
            ))
            .around(new ResetStateRule(new AppDependencyModule() {
                @Override
                @Provides
                public QRCodeGenerator providesQRCodeGenerator() {
                    return stubQRCodeGenerator;
                }
            }))
            .around(new RunnableRule(stubQRCodeGenerator::setup))
            .around(rule);

    @Before
    public void stubAllExternalIntents() {
        // Pretend that the share/import actions are cancelled so we don't deal with actual import here
        intending(not(isInternal())).respondWith(new Instrumentation.ActivityResult(Activity.RESULT_CANCELED, null));
    }

    @After
    public void teardown() {
        // Clean up files created by stub generator
        stubQRCodeGenerator.teardown();
    }

    @Test
    public void onMainMenu_clickConfigureQRCode_andClickingOnScan_opensScanner() {
        new MainMenuPage(rule)
                .assertOnPage()
                .clickOnMenu()
                .clickConfigureQR()
                .clickScanFragment()
                .checkIsIdDisplayed(R.id.zxing_barcode_surface);
    }

    @Test
    public void onMainMenu_clickConfigureQRCode_andClickingOnView_showsQRCode() {
        new MainMenuPage(rule)
                .assertOnPage()
                .clickOnMenu()
                .clickConfigureQR()
                .clickViewQRFragment()
                .assertImageViewShowsImage(R.id.ivQRcode, BitmapFactory.decodeResource(
                        getApplicationContext().getResources(),
                        stubQRCodeGenerator.getDrawableID()
                ));
    }

    @Test
    @Ignore("Should be replaced at Robolectric level")
    public void onMainMenu_clickConfigureQRCode_andClickingOnImportQRCode_startsExternalImagePickerIntent() {
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
    @Ignore("Should be replaced at Robolectric level")
    public void onMainMenu_clickConfigureQRCode_andClickingOnShareQRCode_startsExternalShareIntent() {
        String path = new StubQRCodeGenerator().getQrCodeFilepath();
        Uri expected = FileProvider.getUriForFile(getApplicationContext(),
                BuildConfig.APPLICATION_ID + ".provider",
                new File(path));

        new MainMenuPage(rule)
                .assertOnPage()
                .clickOnMenu()
                .clickConfigureQR()
                .clickOnId(R.id.menu_item_share);

        // Check that we've actually generated a QR code to share
        File qrcodeFile = new File(path);
        assertTrue(qrcodeFile.exists());

        // should be two Intents, 1. to QRCodeTabsActivity 2. Share QR Code Intent
        assertThat(Intents.getIntents().size(), equalTo(2));
        Intent receivedIntent = Intents.getIntents().get(1);
        assertThat(receivedIntent, hasAction(Intent.ACTION_CHOOSER));

        // test title
        assertThat(receivedIntent, hasExtras(hasEntry(Intent.EXTRA_TITLE,
                getApplicationContext().getString(R.string.share_qrcode))));

        // test SEND intent
        assertThat(receivedIntent, hasExtraWithKey(Intent.EXTRA_INTENT));
        Intent sendIntent = receivedIntent.getParcelableExtra(Intent.EXTRA_INTENT);

        assertThat(sendIntent, hasAction(Intent.ACTION_SEND));
        assertThat(sendIntent, hasType("image/*"));
        assertThat(sendIntent, hasExtras(hasEntry(Intent.EXTRA_STREAM, expected)));
    }

    // StubQRCodeGenerator is a class that is injected during this test
    // to verify that the QRCode is generated and shown to user correctly
    private static class StubQRCodeGenerator implements QRCodeGenerator {

        private static final int CHECKER_BACKGROUND_DRAWABLE_ID = R.drawable.checker_background;

        @Override
        public Observable<Bitmap> generateQRCode(Collection<String> selectedPasswordKeys) {
            return Observable.create(emitter -> {
                Bitmap bitmap =
                        BitmapFactory.decodeResource(
                                getApplicationContext().getResources(),
                                CHECKER_BACKGROUND_DRAWABLE_ID);
                emitter.onNext(bitmap);
                emitter.onComplete();
            });
        }

        @Override
        public String getQrCodeFilepath() {
            return getApplicationContext().getExternalFilesDir(null) + File.separator + "test-collect-settings.png";
        }

        int getDrawableID() {
            return CHECKER_BACKGROUND_DRAWABLE_ID;
        }

        public void setup() {
            Bitmap bitmap =
                    BitmapFactory.decodeResource(
                            getApplicationContext().getResources(),
                            CHECKER_BACKGROUND_DRAWABLE_ID);
            saveBitmap(bitmap);
        }

        public void teardown() {
            File file = new File(getQrCodeFilepath());
            if (file.exists()) {
                file.delete();
            }
        }

        private void saveBitmap(Bitmap bitmap) {
            try (FileOutputStream out = new FileOutputStream(getQrCodeFilepath())) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public Bitmap generateQRBitMap(String data, int sideLength) throws IOException, WriterException {
            // don't use this in this test, so okay to return null
            return null;
        }

        @Override
        public String getMd5CachePath() {
            // don't use this in this test, so okay to return null
            return null;
        }
    }
}
