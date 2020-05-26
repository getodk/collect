package org.odk.collect.android.preferences.qr;

import android.Manifest;
import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.core.util.Pair;
import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import com.google.zxing.WriterException;

import org.json.JSONException;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.MainMenuActivity;
import org.odk.collect.android.injection.config.AppDependencyModule;
import org.odk.collect.android.support.ResetStateRule;
import org.odk.collect.android.support.RunnableRule;
import org.odk.collect.android.support.pages.MainMenuPage;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;

import dagger.Provides;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.isInternal;
import static org.hamcrest.Matchers.not;

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
                public QRCodeGenerator providesQRCodeGenerator(Context context) {
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

    // StubQRCodeGenerator is a class that is injected during this test
    // to verify that the QRCode is generated and shown to user correctly
    private static class StubQRCodeGenerator implements QRCodeGenerator {

        private static final int CHECKER_BACKGROUND_DRAWABLE_ID = R.drawable.checker_background;

        @Override
        public Pair<Bitmap, String> getQRCode(Collection<String> selectedPasswordKeys) throws JSONException, NoSuchAlgorithmException, IOException, WriterException {
            Bitmap bitmap =
                    BitmapFactory.decodeResource(
                            getApplicationContext().getResources(),
                            CHECKER_BACKGROUND_DRAWABLE_ID);
            return new Pair<>(bitmap, getQRCodeFilepath());
        }

        public String getQRCodeFilepath() {
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
            File file = new File(getQRCodeFilepath());
            if (file.exists()) {
                file.delete();
            }
        }

        private void saveBitmap(Bitmap bitmap) {
            try (FileOutputStream out = new FileOutputStream(getQRCodeFilepath())) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
