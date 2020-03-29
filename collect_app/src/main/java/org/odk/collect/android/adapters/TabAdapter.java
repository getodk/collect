package org.odk.collect.android.adapters;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import org.odk.collect.android.fragments.QRScannerFragment;
import org.odk.collect.android.fragments.ShowQRCodeFragment;

public class TabAdapter extends FragmentPagerAdapter  {
    private final Fragment[] myFragments = new Fragment[2];
    private final String[] myFragmentTitleList = {"Scan", "QR Code"};
    public TabAdapter(FragmentManager fm) {
        super(fm);
    }
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new QRScannerFragment();
            case 1:
                return new ShowQRCodeFragment();
            default:
                // should never reach here
                return null;
        }

    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        Fragment createdFragment = (Fragment) super.instantiateItem(container, position);
        // save the appropriate reference depending on position
        myFragments[position] = createdFragment;
        return createdFragment;
    }

    @Override
    public int getCount() {
        return myFragmentTitleList.length;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return myFragmentTitleList[position];
    }

    public Fragment getFragment(int position) {
        return myFragments[position];
    }
}