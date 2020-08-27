package org.odk.collect.android.fragments.viewmodels;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import org.odk.collect.android.adapters.AbstractSelectListAdapter;

@SuppressWarnings("PMD.MissingStaticMethodInNonInstantiatableClass")
public class SelectMinimalViewModel extends ViewModel {
    private final AbstractSelectListAdapter selectListAdapter;
    private final boolean isFlex;
    private final boolean isAutoComplete;

    private SelectMinimalViewModel(AbstractSelectListAdapter selectListAdapter, boolean isFlex, boolean isAutoComplete) {
        this.selectListAdapter = selectListAdapter;
        this.isFlex = isFlex;
        this.isAutoComplete = isAutoComplete;
    }

    public AbstractSelectListAdapter getSelectListAdapter() {
        return selectListAdapter;
    }

    public boolean isFlex() {
        return isFlex;
    }

    public boolean isAutoComplete() {
        return isAutoComplete;
    }

    public static class Factory implements ViewModelProvider.Factory {
        private final AbstractSelectListAdapter selectListAdapter;
        private final boolean isFlex;
        private final boolean isAutoComplete;

        public Factory(AbstractSelectListAdapter selectListAdapter, boolean isFlex, boolean isAutoComplete) {
            this.selectListAdapter = selectListAdapter;
            this.isFlex = isFlex;
            this.isAutoComplete = isAutoComplete;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new SelectMinimalViewModel(selectListAdapter, isFlex, isAutoComplete);
        }
    }
}
