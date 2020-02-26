package org.odk.collect.android.formentry;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.GroupDef;
import org.javarosa.core.model.IFormElement;
import org.odk.collect.android.logic.FormController;

import static org.javarosa.form.api.FormEntryController.EVENT_PROMPT_NEW_REPEAT;

public class FormEntryViewModel extends ViewModel {

    private final FormControllerProvider formControllerProvider;
    private final MutableLiveData<ViewUpdate> updates = new MutableLiveData<>(null);

    @Nullable
    private FormIndex jumpBackIndex;

    public FormEntryViewModel(FormControllerProvider formControllerProvider) {
        this.formControllerProvider = formControllerProvider;
    }

    public LiveData<ViewUpdate> getUpdates() {
        return updates;
    }

    public void promptForNewRepeat() {
        FormController formController = getFormController();

        FormIndex index = formController.getFormIndex();
        jumpBackIndex = index;
        FormDef formDef = formController.getFormDef();
        FormIndex parentRepeatGroup = null;

        while (index.getNextLevel() != null) {
            index = index.getNextLevel();

            IFormElement element = formDef.getChild(index);
            if (element instanceof GroupDef && ((GroupDef) element).getRepeat()) {
                parentRepeatGroup = index;
                break;
            }
        }

        if (parentRepeatGroup != null) {
            formController.jumpToIndex(parentRepeatGroup);
            formController.stepToNextEventType(EVENT_PROMPT_NEW_REPEAT);
        }

        updates.setValue(ViewUpdate.REFRESH);
    }

    public void cancelRepeatPrompt() {
        FormController formController = formControllerProvider.getFormController();

        if (jumpBackIndex != null) {
            formController.jumpToIndex(jumpBackIndex);
            updates.setValue(ViewUpdate.REFRESH);
        } else {
            updates.setValue(ViewUpdate.SHOW_NEXT);
        }
    }

    public enum ViewUpdate {
        REFRESH,
        SHOW_NEXT
    }

    private FormController getFormController() {
        return formControllerProvider.getFormController();
    }

    public static class Factory implements ViewModelProvider.Factory {

        private final FormControllerProvider formControllerProvider;

        public Factory(FormControllerProvider formControllerProvider) {
            this.formControllerProvider = formControllerProvider;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) new FormEntryViewModel(formControllerProvider);
        }
    }
}
