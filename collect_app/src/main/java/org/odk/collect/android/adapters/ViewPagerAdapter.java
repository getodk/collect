package org.odk.collect.android.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import org.odk.collect.android.activities.DataManagerList;
import org.odk.collect.android.activities.FormManagerList;

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
        return position == 0 ? DataManagerList.newInstance() : FormManagerList.newInstance();
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