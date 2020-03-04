package org.odk.collect.android.preferences;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.Nullable;

import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import org.odk.collect.android.BuildConfig;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.CollectAbstractActivity;
import org.odk.collect.android.adapters.TabAdapter;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.fragments.ShowQRCodeFragment;
import org.odk.collect.android.utilities.FileUtils;
import org.odk.collect.android.utilities.QRCodeUtils;
import org.odk.collect.android.utilities.ThemeUtils;
import org.odk.collect.android.utilities.ToastUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.zip.DataFormatException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import timber.log.Timber;

import androidx.appcompat.widget.Toolbar;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

public class QRCodeTabs extends CollectAbstractActivity {
    private TabAdapter adapter;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private Intent shareIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.qrcode_tab);
        initToolbar();
        viewPager = (ViewPager) this.findViewById(R.id.viewPager);
        tabLayout = (TabLayout) this.findViewById(R.id.tabLayout);
        adapter = new TabAdapter(getSupportFragmentManager());
        adapter.addFragment(new ShowQRCodeFragment(), "QR code");
        adapter.addFragment(new ShowQRCodeFragment(), "Scan");
        viewPager.setAdapter(adapter);
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
        }
        return super.onOptionsItemSelected(item);
    }
}
