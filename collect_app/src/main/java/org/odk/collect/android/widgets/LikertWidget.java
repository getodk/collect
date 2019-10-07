package org.odk.collect.android.widgets;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.RadioButton;
import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.SelectOneData;
import org.javarosa.core.model.data.helper.Selection;
import org.javarosa.form.api.FormEntryPrompt;

@SuppressLint("ViewConstructor")
public class LikertWidget extends ItemsWidget {

    public LikertWidget(Context context, FormEntryPrompt prompt, boolean displayLabel, boolean autoAdvance) {
        super(context, prompt);
        
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        /** TODO */
    }

    @Override
    public IAnswerData getAnswer() {
        /** TODO */
        return null;
    }

    @Override
    public void clearAnswer() {
        /** TODO */
    }
}
