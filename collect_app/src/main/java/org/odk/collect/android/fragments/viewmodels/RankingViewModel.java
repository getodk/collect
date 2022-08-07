package org.odk.collect.android.fragments.viewmodels;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import org.javarosa.core.model.SelectChoice;
import org.javarosa.form.api.FormEntryPrompt;

import java.util.List;

@SuppressWarnings("PMD.MissingStaticMethodInNonInstantiatableClass")
public class RankingViewModel extends ViewModel {

    private final List<SelectChoice> items;
    private final FormEntryPrompt formEntryPrompt;

    private RankingViewModel(List<SelectChoice> items, FormEntryPrompt formEntryPrompt) {
        this.items = items;
        this.formEntryPrompt = formEntryPrompt;
    }

    public List<SelectChoice> getItems() {
        return items;
    }

    public FormEntryPrompt getFormEntryPrompt() {
        return formEntryPrompt;
    }

    public static class Factory implements ViewModelProvider.Factory {
        private final List<SelectChoice> items;
        private final FormEntryPrompt formEntryPrompt;

        public Factory(List<SelectChoice> items, FormEntryPrompt formEntryPrompt) {
            this.items = items;
            this.formEntryPrompt = formEntryPrompt;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new RankingViewModel(items, formEntryPrompt);
        }
    }
}
