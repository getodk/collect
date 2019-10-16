package org.odk.collect.android.widgets;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;

import java.util.ArrayList;

import timber.log.Timber;

@SuppressLint("ViewConstructor")
public class LikertWidget extends ItemsWidget {


    private LinearLayout view;
    private RadioGroup radioGroup;
    private RadioButton checkedButton;

    ArrayList<RadioButton> buttons;
    public LikertWidget(Context context, FormEntryPrompt prompt, boolean displayLabel, boolean autoAdvance) {
        super(context, prompt);

        view = (LinearLayout) getLayoutInflater().inflate(R.layout.likert_layout, this, false);
        radioGroup = (RadioGroup) view.findViewById(R.id.likert_scale);

        setRadioButtons();
        setButtonListener();

        addAnswerView(view);
    }

    /**
     * Default place to put the answer
     * (below the help text or question text if there is no help text)
     * If you have many elements, use this first
     * and use the standard addView(view, params) to place the rest
     */
    protected void addAnswerView(View v) {
        if (v == null) {
            Timber.e("cannot add a null view as an answerView");
            return;
        }
        // default place to add answer
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.BELOW, getHelpTextLayout().getId());

        params.setMargins(10, 0, 10, 0);
        addView(v, params);
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
