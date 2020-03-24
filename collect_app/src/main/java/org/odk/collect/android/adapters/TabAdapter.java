package org.odk.collect.android.adapters;

import java.util.ArrayList;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.List;
public class TabAdapter extends FragmentPagerAdapter {
    private final List<Fragment> myFragmentList = new ArrayList<>();
    private final List<String> myFragmentTitleList = new ArrayList<>();
    public TabAdapter(FragmentManager fm) {
        super(fm);
    }
    @Override
    public Fragment getItem(int position) {
        return myFragmentList.get(position);
    }
    public void addFragment(Fragment fragment, String title) {
        myFragmentList.add(fragment);
        myFragmentTitleList.add(title);
    }
    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return myFragmentTitleList.get(position);
    }
    @Override
    public int getCount() {
        return myFragmentList.size();
    }
}