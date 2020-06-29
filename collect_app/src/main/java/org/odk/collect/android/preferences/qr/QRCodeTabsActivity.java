package org.odk.collect.android.preferences.qr;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;

import org.odk.collect.android.R;
import org.odk.collect.android.activities.CollectAbstractActivity;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.android.listeners.PermissionListener;
import org.odk.collect.android.preferences.PreferencesProvider;
import org.odk.collect.android.preferences.utilities.SettingsUtils;
import org.odk.collect.android.utilities.ActivityAvailability;
import org.odk.collect.android.utilities.FileProvider;
import org.odk.collect.android.utilities.PermissionUtils;
import org.odk.collect.android.utilities.QRCodeUtils;
import org.odk.collect.android.utilities.ToastUtils;
import org.odk.collect.async.Scheduler;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.DataFormatException;

import javax.inject.Inject;

import timber.log.Timber;

import static org.odk.collect.android.preferences.qr.QRCodeMenuDelegate.SELECT_PHOTO;

public class QRCodeTabsActivity extends CollectAbstractActivity {

    private static String[] fragmentTitleList;

    @Inject
    QRCodeGenerator qrCodeGenerator;

    @Inject
    ActivityAvailability activityAvailability;

    @Inject
    FileProvider fileProvider;

    @Inject
    PreferencesProvider preferencesProvider;

    @Inject
    Scheduler scheduler;

    private QRCodeMenuDelegate menuDelegate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DaggerUtils.getComponent(this).inject(this);
        setContentView(R.layout.qrcode_tab);

        initToolbar();
        menuDelegate = new QRCodeMenuDelegate(this, activityAvailability, qrCodeGenerator, fileProvider, preferencesProvider, scheduler);

        new PermissionUtils().requestCameraPermission(this, new PermissionListener() {
            @Override
            public void granted() {
                setupViewPager();
            }

            @Override
            public void denied() {
                finish();
            }
        });
    }

    private void setupViewPager() {
        fragmentTitleList = new String[]{getString(R.string.scan_qr_code_fragment_title),
                getString(R.string.view_qr_code_fragment_title)};

        ViewPager2 viewPager = findViewById(R.id.viewPager);
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        QRCodeTabsAdapter adapter = new QRCodeTabsAdapter(this);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> tab.setText(fragmentTitleList[position])).attach();
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setTitle(getString(R.string.configure_via_qr_code));
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menuDelegate.onCreateOptionsMenu(getMenuInflater(), menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (menuDelegate.onOptionsItemSelected(item)) {
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SELECT_PHOTO && resultCode == Activity.RESULT_OK) {
            try {
                boolean qrCodeFound = false;
                if (data != null) {
                    final Uri imageUri = data.getData();
                    if (imageUri != null) {
                        final InputStream imageStream = getContentResolver().openInputStream(imageUri);

                        final Bitmap bitmap = BitmapFactory.decodeStream(imageStream);
                        if (bitmap != null) {
                            String response = QRCodeUtils.decodeFromBitmap(bitmap);
                            if (response != null) {
                                qrCodeFound = true;
                                SettingsUtils.applySettings(this, response);
                            }
                        }
                    }
                }
                if (!qrCodeFound) {
                    ToastUtils.showLongToast(R.string.qr_code_not_found);
                }
            } catch (FormatException | NotFoundException | ChecksumException e) {
                Timber.i(e);
                ToastUtils.showLongToast(R.string.qr_code_not_found);
            } catch (DataFormatException | IOException | OutOfMemoryError | IllegalArgumentException e) {
                Timber.e(e);
                ToastUtils.showShortToast(getString(R.string.invalid_qrcode));
            }
        }
    }
}
