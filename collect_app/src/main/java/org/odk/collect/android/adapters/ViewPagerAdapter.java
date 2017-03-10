package org.odk.collect.android.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import org.odk.collect.android.fragments.DataManagerList;
import org.odk.collect.android.fragments.FormManagerList;

public class ViewPagerAdapter extends FragmentPagerAdapter {

    private CharSequence tabTitles[];


    public ViewPagerAdapter(FragmentManager fm, CharSequence mTitles[]) {
        super(fm);
        this.tabTitles = mTitles;
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
        return tabTitles.length;
    }
}