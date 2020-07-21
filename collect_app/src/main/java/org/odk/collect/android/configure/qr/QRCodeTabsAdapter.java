package org.odk.collect.android.configure.qr;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class QRCodeTabsAdapter extends FragmentStateAdapter {
    public QRCodeTabsAdapter(FragmentActivity fa) {
        super(fa);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new QRCodeScannerFragment();
            case 1:
                return new ShowQRCodeFragment();
            default:
                // should never reach here
                throw new IllegalArgumentException("Fragment position out of bounds");
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}