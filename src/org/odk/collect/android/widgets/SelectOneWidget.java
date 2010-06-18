/*
 * Copyright (C) 2009 University of Washington
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.collect.android.widgets;

import java.io.IOException;
import java.util.Vector;

import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.SelectOneData;
import org.javarosa.core.model.data.helper.Selection;
import org.javarosa.core.reference.InvalidReferenceException;
import org.javarosa.core.reference.ReferenceManager;
import org.javarosa.form.api.FormEntryCaption;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;
import org.odk.collect.android.logic.GlobalConstants;
import org.odk.collect.android.views.AudioButton;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.CompoundButton.OnCheckedChangeListener;

/**
 * SelectOneWidgets handles select-one fields using radio buttons.
 * 
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */
public class SelectOneWidget extends RadioGroup implements IQuestionWidget, OnCheckedChangeListener {

 //   int mRadioChecked = -1;
    Vector<SelectChoice> mItems;
    
    Vector<RadioButton> buttons;


    public SelectOneWidget(Context context) {
        super(context);
    }


    public void clearAnswer() {
        clearCheck();
    }


    public IAnswerData getAnswer() {
        int i = getCheckedId();
        if (i == -1) {
            return null;
        } else {
            String s = mItems.elementAt(i).getValue();
            return new SelectOneData(new Selection(s));
        }
    }

    public void buildView(final FormEntryPrompt prompt) {
        mItems = prompt.getSelectChoices();
        buttons = new Vector<RadioButton>();

        String s = null;
        if (prompt.getAnswerValue() != null) {
            s = ((Selection)prompt.getAnswerValue().getValue()).getValue();
        }

        if (prompt.getSelectChoices() != null) {
            for (int i = 0; i < mItems.size(); i++) {
                RadioButton r = new RadioButton(getContext());
                r.setOnCheckedChangeListener(this);
                
                r.setText(prompt.getSelectChoiceText(mItems.get(i)));
                r.setTextSize(TypedValue.COMPLEX_UNIT_PX, GlobalConstants.APPLICATION_FONTSIZE);
                r.setId(i);
                
                String imageURI = prompt.getSelectChoiceText(mItems.get(i), "image");
                if(imageURI != null) {
                	try {
						Bitmap b = BitmapFactory.decodeStream(ReferenceManager._().DeriveReference(imageURI).getStream());
						BitmapDrawable bd = new BitmapDrawable(b);
						DisplayMetrics dm = Resources.getSystem().getDisplayMetrics();
						bd.setBounds(new Rect(0,0,b.getScaledWidth(dm),b.getScaledHeight(dm)));
						r.setCompoundDrawables(bd,null,null,null);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InvalidReferenceException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
                }
                
                
                r.setEnabled(!prompt.isReadOnly());
                r.setFocusable(!prompt.isReadOnly());
                
                if(prompt.getSelectTextForms(mItems.get(i)).contains(FormEntryCaption.TEXT_FORM_AUDIO)) {
                    String audioUri = prompt.getSelectChoiceText(mItems.get(i), FormEntryCaption.TEXT_FORM_AUDIO);
					AudioButton audioButton = new AudioButton(getContext(), audioUri);
					
					LinearLayout rl = (LinearLayout)View.inflate(this.getContext(),R.layout.radiobutton_linearlayout, null);
					View template = rl.findViewById(R.id.radiobuttontemplate);
					
					LinearLayout holder = new LinearLayout(this.getContext());
					holder.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
					
					r.setLayoutParams(template.getLayoutParams());
					
					holder.addView(r,template.getLayoutParams());
					holder.addView(audioButton);
					addView(holder);
                } else {
                    addView(r);
                }
                
                this.buttons.add(r);

                if (mItems.get(i).getValue().equals(s)) {
                    r.setChecked(true);
                    //mRadioChecked = i;
                }

            }
        }
    }


    public void setFocus(Context context) {
        // Hide the soft keyboard if it's showing.
        InputMethodManager inputManager =
                (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(this.getWindowToken(), 0);
    }
    
    public int getCheckedId() {
    	for(RadioButton button : this.buttons) {
    		if(button.isChecked()) {
    			return button.getId();
    		}
    	}
    	return -1;
    }


	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		//I don't know what this does...
        /*if (mRadioChecked != -1 && prompt.isReadOnly()) {
            SelectOneWidget.this.check(mRadioChecked);
        }*/
		if(!isChecked) {
			//If it got unchecked, we don't care.
			return;
		}
		
		for(RadioButton button : this.buttons) {
    		if(button.isChecked() && !(buttonView == button)) {
    			button.setChecked(false);
    		}
    	}
	}

}
