package org.odk.collect.android.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import org.odk.collect.android.fragments.BlankFormListFragment;
import org.odk.collect.android.fragments.SavedFormListFragment;

public class DeleteFormsTabsAdapter extends FragmentStateAdapter {

    private final boolean matchExactlyEnabled;
//    private ViewModelProvider.Factory viewModelFactory;

    public DeleteFormsTabsAdapter(FragmentActivity fa, boolean matchExactlyEnabled) {
        super(fa);
        this.matchExactlyEnabled = matchExactlyEnabled;
//        this.viewModelFactory = viewModelFactory;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new SavedFormListFragment();
            case 1:
//                return new DeleteBlankFormFragment(viewModelFactory);
                return new BlankFormListFragment();
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
