package org.odk.collect.android.formhierarchy;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import org.odk.collect.android.R;
import org.odk.collect.android.formentry.FormEntryViewModel;

public class FormHierarchyFragment extends Fragment {

    private final ViewModelProvider.Factory viewModelFactory;

    public FormHierarchyFragment(ViewModelProvider.Factory viewModelFactory) {
        super(R.layout.form_hierarchy_layout);

        this.viewModelFactory = viewModelFactory;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        FormEntryViewModel formEntryViewModel = viewModelFactory.create(FormEntryViewModel.class);
        requireActivity().setTitle(formEntryViewModel.getFormController().getFormTitle());
    }
}
