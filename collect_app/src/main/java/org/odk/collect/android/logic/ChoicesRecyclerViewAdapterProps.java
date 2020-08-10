package org.odk.collect.android.logic;

import android.content.Context;

import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.reference.ReferenceManager;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.audio.AudioHelper;

import java.util.List;

public class ChoicesRecyclerViewAdapterProps {
    private final Context context;
    protected List<SelectChoice> items;
    protected List<SelectChoice> filteredItems;
    protected final FormEntryPrompt prompt;
    private final ReferenceManager referenceManager;
    private final AudioHelper audioHelper;
    protected final int playColor;
    private final int numColumns;
    protected boolean noButtonsMode;

    public ChoicesRecyclerViewAdapterProps(Context context, List<SelectChoice> items,
                                           FormEntryPrompt prompt, ReferenceManager referenceManager, AudioHelper audioHelper,
                                           int playColor, int numColumns, boolean noButtonsMode) {
        this.context = context;
        this.items = items;
        this.filteredItems = items;
        this.prompt = prompt;
        this.referenceManager = referenceManager;
        this.audioHelper = audioHelper;
        this.playColor = playColor;
        this.numColumns = numColumns;
        this.noButtonsMode = noButtonsMode;
    }

    public Context getContext() {
        return context;
    }

    public List<SelectChoice> getItems() {
        return items;
    }

    public List<SelectChoice> getFilteredItems() {
        return filteredItems;
    }

    public FormEntryPrompt getPrompt() {
        return prompt;
    }

    public ReferenceManager getReferenceManager() {
        return referenceManager;
    }

    public AudioHelper getAudioHelper() {
        return audioHelper;
    }

    public int getPlayColor() {
        return playColor;
    }

    public int getNumColumns() {
        return numColumns;
    }

    public boolean isNoButtonsMode() {
        return noButtonsMode;
    }

    public void setFilteredItems(List<SelectChoice> filteredItems) {
        this.filteredItems = filteredItems;
    }
}
