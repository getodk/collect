package org.odk.collect.android.fragments.viewmodels;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.adapters.AbstractSelectListAdapter;

@SuppressWarnings("PMD.MissingStaticMethodInNonInstantiatableClass")
public class SelectMinimalViewModel extends ViewModel {
    private final AbstractSelectListAdapter selectListAdapter;
    private final FormEntryPrompt formEntryPrompt;

    private SelectMinimalViewModel(AbstractSelectListAdapter selectListAdapter, FormEntryPrompt formEntryPrompt) {
        this.selectListAdapter = selectListAdapter;
        this.formEntryPrompt = formEntryPrompt;
    }

    public AbstractSelectListAdapter getSelectListAdapter() {
        return selectListAdapter;
    }

    public FormEntryPrompt getFormEntryPrompt() {
        return formEntryPrompt;
    }

    public static class Factory implements ViewModelProvider.Factory {
        private final AbstractSelectListAdapter selectListAdapter;
        private final FormEntryPrompt formEntryPrompt;

        public Factory(AbstractSelectListAdapter selectListAdapter, FormEntryPrompt formEntryPrompt) {
            this.selectListAdapter = selectListAdapter;
            this.formEntryPrompt = formEntryPrompt;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new SelectMinimalViewModel(selectListAdapter, formEntryPrompt);
        }
    }
}
