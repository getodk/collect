package org.odk.collect.android.widgets;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;
import org.w3c.dom.Text;

import java.util.ArrayList;

import timber.log.Timber;

@SuppressLint("ViewConstructor")
public class LikertWidget extends ItemsWidget {


    private LinearLayout view;
    private RadioGroup radioGroup;
    private RadioButton checkedButton;

    ArrayList<TextView> textViews;
    ArrayList<ImageView> imageViews;
    ArrayList<RadioButton> buttons;
    public LikertWidget(Context context, FormEntryPrompt prompt, boolean displayIcons) {
        super(context, prompt);

        view = (LinearLayout) getLayoutInflater().inflate(R.layout.likert_layout, this, false);
        radioGroup = (RadioGroup) view.findViewById(R.id.likert_scale);

        setStructures();
        setButtonListener();
        if(displayIcons){
            showImages();
            hideTextViews();
        }
        addAnswerView(view);
    }

    /**
     * Default place to put the answer
     * (below the help text or question text if there is no help text)
     * If you have many elements, use this first
     * and use the standard addView(view, params) to place the rest
     */
    //TODO: Maybe try to call it from somewhere
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

    public void setStructures(){
        buttons = new ArrayList<>();
        imageViews = new ArrayList<>();
        textViews = new ArrayList<>();

        for (int i=0;i < radioGroup.getChildCount(); i++) {
            View v = radioGroup.getChildAt(i);
            if (v instanceof LinearLayout) {
                for(int j = 0; j < ((LinearLayout) v).getChildCount(); j++){
                    View v2 = ((LinearLayout) v).getChildAt(j);
                    if(v2 instanceof RadioButton){
                        buttons.add((RadioButton) v2);
                    }else if(v2 instanceof TextView){
                        textViews.add((TextView) v2);
                    }else if(v2 instanceof ImageView){
                        imageViews.add((ImageView) v2);
                    }
                }
            }
        }
    }

    public void setButtonListener(){
        for (RadioButton button: buttons) {
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

    public void showImages(){
        for(ImageView view: imageViews){
            view.setVisibility(View.VISIBLE);
        }
    }

    public void hideTextViews(){
        for(TextView view: textViews){
            view.setVisibility(View.GONE);
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
