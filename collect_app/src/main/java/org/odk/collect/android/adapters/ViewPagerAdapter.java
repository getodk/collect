package org.odk.collect.android.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import org.odk.collect.android.fragments.Tab;

public class ViewPagerAdapter extends FragmentPagerAdapter {

    private CharSequence tabTitles[];
    private int PAGE_COUNT = 2;


    public ViewPagerAdapter(FragmentManager fm, CharSequence mTitles[], int mNumbOfTabsumb) {
        super(fm);

        this.tabTitles = mTitles;
        this.PAGE_COUNT = mNumbOfTabsumb;

    }

    @Override
    public Fragment getItem(int position) {
        return Tab.newInstance();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabTitles[position];
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }
}