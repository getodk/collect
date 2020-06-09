package org.odk.collect.android.feature.settings;

import android.Manifest;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import com.google.zxing.WriterException;

import org.json.JSONException;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.injection.config.AppDependencyModule;
import org.odk.collect.android.preferences.qr.QRCodeGenerator;
import org.odk.collect.android.support.CollectTestRule;
import org.odk.collect.android.support.CountingScheduler;
import org.odk.collect.android.support.CountingSchedulerIdlingResource;
import org.odk.collect.android.support.IdlingResourceRule;
import org.odk.collect.android.support.ResetStateRule;
import org.odk.collect.android.support.RunnableRule;
import org.odk.collect.android.support.pages.GeneralSettingsPage;
import org.odk.collect.android.support.pages.MainMenuPage;
import org.odk.collect.async.CoroutineScheduler;
import org.odk.collect.async.Scheduler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

@RunWith(AndroidJUnit4.class)
public class ConfigureWithQRCodeTest {

    private final CollectTestRule rule = new CollectTestRule();
    private final StubQRCodeGenerator stubQRCodeGenerator = new StubQRCodeGenerator();
    private final CountingScheduler countingScheduler = new CountingScheduler(new CoroutineScheduler());

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
                public QRCodeGenerator providesQRCodeGenerator(Context context) {
                    return stubQRCodeGenerator;
                }

                @Override
                public Scheduler providesScheduler() {
                    return countingScheduler;
                }
            }))
            .around(new IdlingResourceRule(new CountingSchedulerIdlingResource(countingScheduler)))
            .around(new RunnableRule(stubQRCodeGenerator::setup))
            .around(rule);

    @After
    public void teardown() {
        // Clean up files created by stub generator
        stubQRCodeGenerator.teardown();
    }

    @Test
    public void clickConfigureQRCode_opensScanner() {
        rule.mainMenu()
                .clickOnMenu()
                .clickConfigureQR()
                .checkIsIdDisplayed(R.id.zxing_barcode_surface);
    }

    @Test
    public void clickConfigureQRCode_andClickingOnView_showsQRCode() {
        rule.mainMenu()
                .clickOnMenu()
                .clickConfigureQR()
                .clickView()
                .assertImageViewShowsImage(R.id.ivQRcode, BitmapFactory.decodeResource(
                        getApplicationContext().getResources(),
                        stubQRCodeGenerator.getDrawableID()
                ));
    }

    @Test
    public void whenThereIsAnAdminPassword_canRemoveFromQRCode() {
        rule.mainMenu()
                .clickOnMenu()
                .clickAdminSettings()
                .clickOnString(R.string.admin_password)
                .inputText("blah")
                .clickOKOnDialog()
                .pressBack(new MainMenuPage(rule))

                .clickOnMenu()
                .clickConfigureQRWithAdminPassword("blah")
                .clickView()
                .clickOnString(R.string.qrcode_with_admin_password)
                .clickOnString(R.string.admin_password)
                .clickOnString(R.string.generate)
                .assertText(R.string.qrcode_without_passwords);
    }

    @Test
    public void whenThereIsAServerPassword_canRemoveFromQRCode() {
        rule.mainMenu()
                .clickOnMenu()
                .clickGeneralSettings()
                .clickServerSettings()
                .clickServerPassword()
                .inputText("blah")
                .clickOKOnDialog()
                .pressBack(new GeneralSettingsPage(rule))
                .pressBack(new MainMenuPage(rule))

                .clickOnMenu()
                .clickConfigureQR()
                .clickView()
                .clickOnString(R.string.qrcode_with_server_password)
                .clickOnString(R.string.server_password)
                .clickOnString(R.string.generate)
                .assertText(R.string.qrcode_without_passwords);
    }

    private static class StubQRCodeGenerator implements QRCodeGenerator {

        private static final int CHECKER_BACKGROUND_DRAWABLE_ID = R.drawable.checker_background;

        @Override
        public String generateQRCode(Collection<String> selectedPasswordKeys) throws JSONException, NoSuchAlgorithmException, IOException, WriterException {
            return getQRCodeFilePath();
        }

        public void setup() {
            Bitmap bitmap = BitmapFactory.decodeResource(
                    getApplicationContext().getResources(),
                    getDrawableID());
            saveBitmap(bitmap);
        }

        public void teardown() {
            File file = new File(getQRCodeFilePath());
            if (file.exists()) {
                file.delete();
            }
        }

        String getQRCodeFilePath() {
            return getApplicationContext().getExternalFilesDir(null) + File.separator + "test-collect-settings.png";
        }

        int getDrawableID() {
            return CHECKER_BACKGROUND_DRAWABLE_ID;
        }

        private void saveBitmap(Bitmap bitmap) {
            try (FileOutputStream out = new FileOutputStream(getQRCodeFilePath())) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
