package org.odk.collect.android.widgets;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.SeekBar;

import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;

import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.SelectOneData;
import org.javarosa.core.model.data.helper.Selection;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;

import timber.log.Timber;

@SuppressLint("ViewConstructor")
public class LikertWidget extends ItemsWidget {


    private LinearLayout view;
    private SeekBar seekBar;

    public LikertWidget(Context context, FormEntryPrompt prompt, boolean displayLabel, boolean autoAdvance) {
        super(context, prompt);

        setUpAppearance();

    }


    private void setUpAppearance() {
        String appearance = getFormEntryPrompt().getQuestion().getAppearanceAttr();

//        loadAppearance(R.layout.likert_layout, R.id.seek_bar);

        @LayoutRes int layoutId = R.layout.likert_layout;

        @IdRes int seekBarId = R.id.seek_bar;


    }

    private void loadAppearance(@LayoutRes int layoutId, @IdRes int seekBarId) {
        view = (LinearLayout) getLayoutInflater().inflate(layoutId, this, false);
        seekBar = view.findViewById(seekBarId);

//        view.findViewById(seekBar);
    }

    private LayoutInflater layoutInflater;

    private LayoutInflater getLayoutInflater() {

        // Only for testing purposes, this shouldn't actually be cached:
        if (this.layoutInflater != null) {
            return layoutInflater;
        }

        return LayoutInflater.from(getContext());
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
