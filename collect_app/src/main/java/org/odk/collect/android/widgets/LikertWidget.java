package org.odk.collect.android.widgets;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.appcompat.widget.AppCompatRadioButton;

import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.SelectOneData;
import org.javarosa.core.model.data.helper.Selection;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;
import org.odk.collect.android.utilities.ViewIds;

import java.util.ArrayList;

import timber.log.Timber;

@SuppressLint("ViewConstructor")
public class LikertWidget extends ItemsWidget {


    private LinearLayout view;
    private RadioGroup radioGroup;
    private RadioButton checkedButton;
//    private SeekBar seekBar;

    ArrayList<RadioButton> buttons;
//    View center;
    public LikertWidget(Context context, FormEntryPrompt prompt, boolean displayLabel, boolean autoAdvance) {
        super(context, prompt);

        view = (LinearLayout) getLayoutInflater().inflate(R.layout.likert_layout, this, false);
        radioGroup = (RadioGroup) view.findViewById(R.id.likert_scale);

        setRadioButtons();
        setButtonListener();

        addView(view);
    }

    public void setRadioButtons(){
        buttons = new ArrayList<RadioButton>();
        for (int i=0;i < radioGroup.getChildCount(); i++) {
            View v = radioGroup.getChildAt(i);
            System.out.println("i " + i);
            // This is always a linear layout
            if (v instanceof LinearLayout) {
                for(int j = 0; j < ((LinearLayout) v).getChildCount(); j++){

                    View v2 = ((LinearLayout) v).getChildAt(j);
                    if(v2 instanceof RadioButton){
                        buttons.add((RadioButton) v2);
                        System.out.println("inner " + j);
                    }
                }

            }
        }
    }

    public void setButtonListener(){
        System.out.println("Setting listeners ");
        for (RadioButton button: buttons) {
            System.out.println("button " + button.getId());
            button.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    RadioButton r = (RadioButton) v;
                    if(checkedButton != null){
                        checkedButton.setChecked(false);
                    }
                    checkedButton = r;
                    checkedButton.setChecked(true);
                }
            });
        }
    }


//    private void setUpAppearance() {
//        String appearance = getFormEntryPrompt().getQuestion().getAppearanceAttr();
//
//        loadAppearance(R.layout.likert_layout, R.id.seek_bar);
//
//        @LayoutRes int layoutId = R.layout.likert_layout;
//
//        @IdRes int seekBarId = R.id.seek_bar;
//
//
//    }
//
//    private void loadAppearance(@LayoutRes int layoutId, @IdRes int seekBarId) {
//        view = (LinearLayout) getLayoutInflater().inflate(layoutId, this, false);
//        seekBar = view.findViewById(seekBarId);
//
////        view.findViewById(seekBar);
//    }
//    @Override
//    protected void addQuestionMediaLayout(View v) {
//        center = new View(getContext());
//        RelativeLayout.LayoutParams centerParams = new RelativeLayout.LayoutParams(0, 0);
//        centerParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
//        center.setId(ViewIds.generateViewId());
//        addView(center, centerParams);
//
//        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
//                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
//        params.addRule(RelativeLayout.LEFT_OF, center.getId());
//        addView(v, params);
//    }

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
