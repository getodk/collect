package org.odk.collect.android.fragments.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import org.javarosa.core.model.data.helper.Selection;
import org.jetbrains.annotations.NotNull;
import org.odk.collect.android.R;
import org.odk.collect.android.adapters.AbstractSelectListAdapter;
import org.odk.collect.android.databinding.SelectMinimalDialogLayoutBinding;
import org.odk.collect.android.fragments.viewmodels.SelectMinimalViewModel;
import org.odk.collect.material.MaterialFullScreenDialogFragment;

import java.util.List;

public abstract class SelectMinimalDialog extends MaterialFullScreenDialogFragment {
    private SelectMinimalDialogLayoutBinding binding;

    private boolean isFlex;
    private boolean isAutocomplete;

    protected SelectMinimalViewModel viewModel;
    private SearchView searchView;
    protected SelectMinimalDialogListener listener;

    public interface SelectMinimalDialogListener {
        void updateSelectedItems(List<Selection> items);
    }

    public SelectMinimalDialog() {
    }

    public SelectMinimalDialog(boolean isFlex, boolean isAutoComplete) {
        this.isFlex = isFlex;
        this.isAutocomplete = isAutoComplete;
    }

    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        if (context instanceof SelectMinimalDialogListener) {
            listener = (SelectMinimalDialogListener) context;
        }
        viewModel = new ViewModelProvider(this, new SelectMinimalViewModel.Factory(getAdapter(), isFlex, isAutocomplete)).get(SelectMinimalViewModel.class);
        if (viewModel.getSelectListAdapter() == null) {
            dismiss();
        }
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = SelectMinimalDialogLayoutBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NotNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initRecyclerView();
        initToolbar();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        viewModel.getSelectListAdapter().getProps().getAudioHelper().stop();
        binding = null;
    }

    @Override
    protected void onCloseClicked() {
        closeDialogAndSaveAnswers();
    }

    @Override
    protected void onBackPressed() {
        closeDialogAndSaveAnswers();
    }

    protected void closeDialogAndSaveAnswers() {
        viewModel.getSelectListAdapter().getFilter().filter("");
        listener.updateSelectedItems(viewModel.getSelectListAdapter().getSelectedItems());
        dismiss();
    }

    @Nullable
    @Override
    protected Toolbar getToolbar() {
        return getView().findViewById(R.id.toolbar);
    }

    protected abstract AbstractSelectListAdapter getAdapter();

    private void initToolbar() {
        getToolbar().setNavigationIcon(R.drawable.ic_arrow_back);

        if (viewModel.isAutoComplete()) {
            addSearchBar();
        }
    }

    private void addSearchBar() {
        getToolbar().inflateMenu(R.menu.select_minimal_dialog_menu);

        searchView = (SearchView) getToolbar().getMenu().findItem(R.id.menu_filter).getActionView();
        searchView.setQueryHint(getResources().getString(R.string.search));
        searchView.setMaxWidth(Integer.MAX_VALUE);
        searchView.onActionViewExpanded();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchView.clearFocus();
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                viewModel.getSelectListAdapter().getFilter().filter(newText);
                return false;
            }
        });
    }

    private void initRecyclerView() {
        binding.choicesRecyclerView.initRecyclerView(viewModel.getSelectListAdapter(), viewModel.isFlex());
    }
}