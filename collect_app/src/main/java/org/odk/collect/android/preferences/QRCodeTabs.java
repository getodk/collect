package org.odk.collect.android.preferences;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import org.odk.collect.android.BuildConfig;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.CollectAbstractActivity;
import org.odk.collect.android.activities.MainMenuActivity;
import org.odk.collect.android.activities.ScanQRCodeActivity;
import org.odk.collect.android.adapters.TabAdapter;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.fragments.QRScannerFragment;
import org.odk.collect.android.fragments.ShowQRCodeFragment;
import org.odk.collect.android.listeners.ActionListener;
import org.odk.collect.android.listeners.ViewPagerListener;
import org.odk.collect.android.utilities.FileUtils;
import org.odk.collect.android.utilities.LocaleHelper;
import org.odk.collect.android.utilities.QRCodeUtils;
import org.odk.collect.android.utilities.ToastUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.DataFormatException;
import timber.log.Timber;

import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;

import static org.odk.collect.android.activities.ActivityUtils.startActivityAndCloseAllOthers;

public class QRCodeTabs extends CollectAbstractActivity {
    private static final int SELECT_PHOTO = 111;

    private TabAdapter adapter;
    private Intent shareIntent;

    private int currentPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.qrcode_tab);
        initToolbar();
        ViewPager viewPager = (ViewPager) this.findViewById(R.id.viewPager);
        TabLayout tabLayout = (TabLayout) this.findViewById(R.id.tabLayout);
        adapter = new TabAdapter(getSupportFragmentManager());
        adapter.addFragment(new QRScannerFragment(), "Scan");
        adapter.addFragment(new ShowQRCodeFragment(), "QR code");
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener (new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                ViewPagerListener fragmentToShow = (ViewPagerListener)adapter.getItem(position);
                fragmentToShow.onResumeFragment();

                ViewPagerListener fragmentToHide = (ViewPagerListener)adapter.getItem(currentPosition);
                fragmentToHide.onPauseFragment();

                currentPosition = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        tabLayout.setupWithViewPager(viewPager);
        this.updateShareIntent();
    }


    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setTitle(getString(R.string.qr_code));
        setSupportActionBar(toolbar);
    }

    private void updateShareIntent() {
        // Initialize the intent to share QR Code
        shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("image/*");
        Uri uri =
                FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", new File(QRCodeUtils.getQrCodeFilepath()));
        FileUtils.grantFileReadPermissions(shareIntent, uri, this);
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_share:
                if (shareIntent != null) {
                    startActivity(Intent.createChooser(shareIntent, getString(R.string.share_qrcode)));
                }
                return true;
            case R.id.menu_item_scan_sd_card:
                Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                photoPickerIntent.setType("image/*");
                if (photoPickerIntent.resolveActivity(this.getPackageManager()) != null) {
                    startActivityForResult(photoPickerIntent, SELECT_PHOTO);
                } else {
                    ToastUtils.showShortToast(getString(R.string.activity_not_found, getString(R.string.choose_image)));
                    Timber.w(getString(R.string.activity_not_found, getString(R.string.choose_image)));
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SELECT_PHOTO) {
            if (resultCode == Activity.RESULT_OK) {
                try {
                    boolean qrCodeFound = false;
                    final Uri imageUri = data.getData();
                    if (imageUri != null) {
                        final InputStream imageStream = this.getContentResolver()
                                .openInputStream(imageUri);

                        final Bitmap bitmap = BitmapFactory.decodeStream(imageStream);
                        if (bitmap != null) {
                            String response = QRCodeUtils.decodeFromBitmap(bitmap);
                            if (response != null) {
                                qrCodeFound = true;
                                ScanQRCodeActivity.applySettings(this, response);
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
            } else {
                Timber.i("Choosing QR code from sdcard cancelled");
            }
        }
    }

    public static void applySettings(Activity activity, String content) {
        new PreferenceSaver(GeneralSharedPreferences.getInstance(), AdminSharedPreferences.getInstance()).fromJSON(content, new ActionListener() {
            @Override
            public void onSuccess() {
                Collect.getInstance().initializeJavaRosa();
                ToastUtils.showLongToast(Collect.getInstance().getString(R.string.successfully_imported_settings));
                final LocaleHelper localeHelper = new LocaleHelper();
                localeHelper.updateLocale(activity);
                startActivityAndCloseAllOthers(activity, MainMenuActivity.class);
            }

            @Override
            public void onFailure(Exception exception) {
                if (exception instanceof GeneralSharedPreferences.ValidationException) {
                    ToastUtils.showLongToast(Collect.getInstance().getString(R.string.invalid_qrcode));
                } else {
                    Timber.e(exception);
                }
            }
        });
    }
}
