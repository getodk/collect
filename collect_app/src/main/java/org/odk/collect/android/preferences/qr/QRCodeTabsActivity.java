package org.odk.collect.android.preferences.qr;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import org.odk.collect.android.BuildConfig;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.CollectAbstractActivity;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.android.utilities.FileUtils;
import org.odk.collect.android.utilities.QRCodeUtils;
import org.odk.collect.android.utilities.ToastUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import javax.inject.Inject;

import static org.odk.collect.android.preferences.AdminKeys.KEY_ADMIN_PW;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_PASSWORD;

public class QRCodeTabsActivity extends CollectAbstractActivity {
    private static final int SELECT_PHOTO = 111;
    private static String[] fragmentTitleList;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private Intent shareIntent;

    @Inject
    QRCodeGenerator qrCodeGenerator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DaggerUtils.getComponent(this).inject(this);
        setContentView(R.layout.qrcode_tab);
        initToolbar();

        fragmentTitleList = new String[]{getString(R.string.scan_qr_code_fragment_title),
                getString(R.string.view_qr_code_fragment_title)};

        ViewPager2 viewPager = (ViewPager2) this.findViewById(R.id.viewPager);
        TabLayout tabLayout = (TabLayout) this.findViewById(R.id.tabLayout);
        QRCodeTabsAdapter adapter = new QRCodeTabsAdapter(this);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager, new TabLayoutMediator.TabConfigurationStrategy() {
            @Override
            public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                tab.setText(fragmentTitleList[position]);
            }
        }).attach();
        this.updateShareIntent();
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setTitle(getString(R.string.configure_via_qr_code));
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

    private void startShareQRCodeIntent() {
        if (new File(QRCodeUtils.getQrCodeFilepath()).exists()) {
            startActivity(Intent.createChooser(shareIntent, getString(R.string.share_qrcode)));
        } else {
            Collection<String> keys = new ArrayList<>();
            keys.add(KEY_ADMIN_PW);
            keys.add(KEY_PASSWORD);
            Disposable disposable = qrCodeGenerator.generateQRCode(keys)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(bitmap -> {
                        startActivity(Intent.createChooser(shareIntent, getString(R.string.share_qrcode)));
                    }, Timber::e);
            compositeDisposable.add(disposable);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onDestroy() {
        compositeDisposable.dispose();
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_share:
                if (shareIntent != null) {
                    this.startShareQRCodeIntent();
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


}
