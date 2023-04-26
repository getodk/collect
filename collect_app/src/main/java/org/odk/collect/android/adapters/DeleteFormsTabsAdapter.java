package org.odk.collect.android.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentFactory;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import org.odk.collect.android.formlists.blankformlist.DeleteBlankFormFragment;
import org.odk.collect.android.fragments.SavedFormListFragment;

public class DeleteFormsTabsAdapter extends FragmentStateAdapter {

    private FragmentFactory fragmentFactory;
    private final boolean matchExactlyEnabled;

    public DeleteFormsTabsAdapter(FragmentActivity activity, boolean matchExactlyEnabled) {
        super(activity);
        this.fragmentFactory = activity.getSupportFragmentManager().getFragmentFactory();
        this.matchExactlyEnabled = matchExactlyEnabled;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new SavedFormListFragment();
            case 1:
                String className = DeleteBlankFormFragment.class.getName();
                return fragmentFactory.instantiate(Thread.currentThread().getContextClassLoader(), className);
            default:
                // should never reach here
                throw new IllegalArgumentException("Fragment position out of bounds");
        }
    }

    @Override
    public int getItemCount() {
        if (matchExactlyEnabled) {
            return 1;
        } else {
            return 2;
        }
    }
}
