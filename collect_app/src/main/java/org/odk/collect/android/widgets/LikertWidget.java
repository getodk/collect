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


//    private LinearLayout view;
    private SeekBar seekBar;
    ArrayList<RadioButton> buttons;
    View center;
    public LikertWidget(Context context, FormEntryPrompt prompt, boolean displayLabel, boolean autoAdvance) {
        super(context, prompt);
        LinearLayout buttonLayout = new LinearLayout(context);
        buttons = new ArrayList<>();

        if (items != null) {
            for (int i = 0; i < items.size(); i++) {

                System.out.println("item " + i);
                AppCompatRadioButton r = new AppCompatRadioButton(getContext());
                r.setId(ViewIds.generateViewId());
                r.setTag(i);
                r.setEnabled(!prompt.isReadOnly());
                r.setFocusable(!prompt.isReadOnly());

                buttons.add(r);

                TextView label = new TextView(getContext());
                label.setText(prompt.getSelectChoiceText(items.get(i)));
                label.setTextSize(TypedValue.COMPLEX_UNIT_DIP, getAnswerFontSize());
                label.setGravity(Gravity.CENTER_HORIZONTAL);
                if (!displayLabel) {
                    label.setVisibility(View.GONE);
                }


                LinearLayout answer = new LinearLayout(getContext());
                answer.setOrientation(LinearLayout.VERTICAL);
                LinearLayout.LayoutParams headerParams =
                        new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                                LayoutParams.WRAP_CONTENT);
                headerParams.gravity = Gravity.CENTER_HORIZONTAL;

                LinearLayout.LayoutParams buttonParams =
                        new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                                LayoutParams.WRAP_CONTENT);
                buttonParams.gravity = Gravity.CENTER_HORIZONTAL;
                final int labelId = ViewIds.generateViewId();
                label.setId(labelId);
                answer.addView(label, headerParams);
                answer.addView(r, buttonParams);
                answer.setPadding(4, 0, 4, 0);

                // Each button gets equal weight
                LinearLayout.LayoutParams answerParams =
                        new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                                LayoutParams.MATCH_PARENT);
                answerParams.weight = 1;

                buttonLayout.addView(answer, answerParams);


            }

            buttonLayout.setOrientation(LinearLayout.HORIZONTAL);

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            params.addRule(RelativeLayout.RIGHT_OF, center.getId());
            addView(buttonLayout, params);

        }else{
            System.out.println("no item ");
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
    @Override
    protected void addQuestionMediaLayout(View v) {
        center = new View(getContext());
        RelativeLayout.LayoutParams centerParams = new RelativeLayout.LayoutParams(0, 0);
        centerParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        center.setId(ViewIds.generateViewId());
        addView(center, centerParams);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.addRule(RelativeLayout.LEFT_OF, center.getId());
        addView(v, params);
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
