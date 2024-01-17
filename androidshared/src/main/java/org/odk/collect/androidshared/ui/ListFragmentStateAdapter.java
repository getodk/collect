package org.odk.collect.androidshared.ui;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentFactory;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.List;

public class ListFragmentStateAdapter extends FragmentStateAdapter {

    private final FragmentFactory fragmentFactory;
    private final List<String> fragments;

    public ListFragmentStateAdapter(FragmentActivity activity, List<String> fragments) {
        super(activity);
        this.fragmentFactory = activity.getSupportFragmentManager().getFragmentFactory();
        this.fragments = fragments;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        String className = fragments.get(position);
        return fragmentFactory.instantiate(Thread.currentThread().getContextClassLoader(), className);
    }

    @Override
    public int getItemCount() {
        return fragments.size();
    }
}
