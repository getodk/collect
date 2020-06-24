package org.odk.collect.android.fragments.viewmodels;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.SelectChoice;

import java.util.List;

@SuppressWarnings("PMD.MissingStaticMethodInNonInstantiatableClass")
public class RankingViewModel extends ViewModel {

    private final List<SelectChoice> items;
    private final FormIndex formIndex;

    private RankingViewModel(List<SelectChoice> items, FormIndex formIndex) {
        this.items = items;
        this.formIndex = formIndex;
    }

    public List<SelectChoice> getItems() {
        return items;
    }

    public FormIndex getFormIndex() {
        return formIndex;
    }

    public static class Factory implements ViewModelProvider.Factory {
        private final List<SelectChoice> items;
        private final FormIndex formIndex;

        public Factory(List<SelectChoice> items, FormIndex formIndex) {
            this.items = items;
            this.formIndex = formIndex;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new RankingViewModel(items, formIndex);
        }
    }
}
