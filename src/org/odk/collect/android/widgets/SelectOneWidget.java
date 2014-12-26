/*
 * Copyright (C) 2009 University of Washington
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.collect.android.widgets;

import java.util.ArrayList;
import java.util.List;

import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.SelectOneData;
import org.javarosa.core.model.data.helper.Selection;
import org.javarosa.form.api.FormEntryCaption;
import org.javarosa.form.api.FormEntryPrompt;
import org.javarosa.xpath.expr.XPathFuncExpr;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.external.ExternalDataUtil;
import org.odk.collect.android.external.ExternalSelectChoice;
import org.odk.collect.android.views.MediaLayout;

import android.content.Context;
import android.util.TypedValue;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;

/**
 * SelectOneWidgets handles select-one fields using radio buttons.
 * 
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */
public class SelectOneWidget extends QuestionWidget implements
		OnCheckedChangeListener {

	List<SelectChoice> mItems; // may take a while to compute
	ArrayList<RadioButton> buttons;

	public SelectOneWidget(Context context, FormEntryPrompt prompt) {
		super(context, prompt);

        // SurveyCTO-added support for dynamic select content (from .csv files)
        XPathFuncExpr xPathFuncExpr = ExternalDataUtil.getSearchXPathExpression(prompt.getAppearanceHint());
        if (xPathFuncExpr != null) {
            mItems = ExternalDataUtil.populateExternalChoices(prompt, xPathFuncExpr);
        } else {
            mItems = prompt.getSelectChoices();
        }
		buttons = new ArrayList<RadioButton>();

		// Layout holds the vertical list of buttons
		LinearLayout buttonLayout = new LinearLayout(context);

		String s = null;
		if (prompt.getAnswerValue() != null) {
			s = ((Selection) prompt.getAnswerValue().getValue()).getValue();
		}

		if (mItems != null) {
			for (int i = 0; i < mItems.size(); i++) {
				RadioButton r = new RadioButton(getContext());
				r.setText(prompt.getSelectChoiceText(mItems.get(i)));
				r.setTextSize(TypedValue.COMPLEX_UNIT_DIP, mAnswerFontsize);
				r.setTag(Integer.valueOf(i));
				r.setId(QuestionWidget.newUniqueId());
				r.setEnabled(!prompt.isReadOnly());
				r.setFocusable(!prompt.isReadOnly());

				buttons.add(r);

				if (mItems.get(i).getValue().equals(s)) {
					r.setChecked(true);
				}

				r.setOnCheckedChangeListener(this);

				String audioURI = null;
				audioURI = prompt.getSpecialFormSelectChoiceText(mItems.get(i),
						FormEntryCaption.TEXT_FORM_AUDIO);

                String imageURI;
                if (mItems.get(i) instanceof ExternalSelectChoice) {
                    imageURI = ((ExternalSelectChoice) mItems.get(i)).getImage();
                } else {
                    imageURI = prompt.getSpecialFormSelectChoiceText(mItems.get(i), FormEntryCaption.TEXT_FORM_IMAGE);
                }

				String videoURI = null;
				videoURI = prompt.getSpecialFormSelectChoiceText(mItems.get(i),
						"video");

				String bigImageURI = null;
				bigImageURI = prompt.getSpecialFormSelectChoiceText(
						mItems.get(i), "big-image");

				MediaLayout mediaLayout = new MediaLayout(getContext());
				mediaLayout.setAVT(prompt.getIndex(), "." + Integer.toString(i), r, audioURI, imageURI,
						videoURI, bigImageURI);

				if (i != mItems.size() - 1) {
					// Last, add the dividing line (except for the last element)
					ImageView divider = new ImageView(getContext());
					divider.setBackgroundResource(android.R.drawable.divider_horizontal_bright);
					mediaLayout.addDivider(divider);
				}
				buttonLayout.addView(mediaLayout);
			}
		}
		buttonLayout.setOrientation(LinearLayout.VERTICAL);

		// The buttons take up the right half of the screen
		LayoutParams buttonParams = new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.WRAP_CONTENT);

		addView(buttonLayout, buttonParams);
	}

	@Override
	public void clearAnswer() {
		for (RadioButton button : this.buttons) {
			if (button.isChecked()) {
				button.setChecked(false);
				return;
			}
		}
	}

	@Override
	public IAnswerData getAnswer() {
		int i = getCheckedId();
		if (i == -1) {
			return null;
		} else {
			SelectChoice sc = mItems.get(i);
			return new SelectOneData(new Selection(sc));
		}
	}

	@Override
	public void setFocus(Context context) {
		// Hide the soft keyboard if it's showing.
		InputMethodManager inputManager = (InputMethodManager) context
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		inputManager.hideSoftInputFromWindow(this.getWindowToken(), 0);
	}

	public int getCheckedId() {
		for (int i = 0; i < buttons.size(); ++i) {
			RadioButton button = buttons.get(i);
			if (button.isChecked()) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (!isChecked) {
			// If it got unchecked, we don't care.
			return;
		}

		for (RadioButton button : buttons ) {
			if (button.isChecked() && !(buttonView == button)) {
				button.setChecked(false);
			}
		}
		
		SelectChoice choice = mItems.get((Integer)buttonView.getTag());
		
		if ( choice != null ) {
       	Collect.getInstance().getActivityLogger().logInstanceAction(this, "onCheckedChanged", 
     	      choice.getValue(), mPrompt.getIndex());
		} else {
        Collect.getInstance().getActivityLogger().logInstanceAction(this, "onCheckedChanged", 
            "<no matching choice>", mPrompt.getIndex());
		}
	}

	@Override
	public void setOnLongClickListener(OnLongClickListener l) {
		for (RadioButton r : buttons) {
			r.setOnLongClickListener(l);
		}
	}

	@Override
	public void cancelLongPress() {
		super.cancelLongPress();
		for (RadioButton button : this.buttons) {
			button.cancelLongPress();
		}
	}

}
