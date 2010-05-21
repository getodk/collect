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
import org.javarosa.core.model.data.SelectMultiData;
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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioGroup.LayoutParams;

/**
 * SelctMultiWidget handles multiple selection fields using checkboxes.
 * 
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */
public class SelectMultiWidget extends LinearLayout implements IQuestionWidget {

    private final static int CHECKBOX_ID = 100;
    private boolean mCheckboxInit = true;
    Vector<SelectChoice> mItems;


    public SelectMultiWidget(Context context) {
        super(context);
    }


    public void clearAnswer() {
        int j = mItems.size();
        for (int i = 0; i < j; i++) {

            // no checkbox group so find by id + offset
            CheckBox c = ((CheckBox) findViewById(CHECKBOX_ID + i));
            if (c.isChecked()) {
                c.setChecked(false);
            }
        }
    }


    @SuppressWarnings("unchecked")
    public IAnswerData getAnswer() {
        Vector<Selection> vc = new Vector<Selection>();
        for (int i = 0; i < mItems.size(); i++) {
            CheckBox c = ((CheckBox) findViewById(CHECKBOX_ID + i));
            if (c.isChecked()) {
                vc.add(new Selection(mItems.get(i).getValue()));
            }
 
        }
        
        if (vc.size() == 0) {
            return null;
        } else {
            return new SelectMultiData(vc);
        }
        
    }


    @SuppressWarnings("unchecked")
    public void buildView(final FormEntryPrompt prompt) {
        mItems = prompt.getSelectChoices();

        setOrientation(LinearLayout.VERTICAL);

        Vector ve = new Vector();
        if (prompt.getAnswerValue() != null) {
            ve = (Vector) prompt.getAnswerValue().getValue();
        }

        if (prompt.getSelectChoices() != null) {
            
            for (int i = 0; i < mItems.size(); i++) {
             // no checkbox group so id by answer + offset
                CheckBox c = new CheckBox(getContext());

                // when clicked, check for readonly before toggling
                c.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if (!mCheckboxInit && prompt.isReadOnly()) {
                            if (buttonView.isChecked()) {
                                buttonView.setChecked(false);
                            } else {
                                buttonView.setChecked(true);
                            }
                        }
                    }
                });
                
                c.setId(CHECKBOX_ID + i);
                
                String imageURI = prompt.getSelectChoiceText(mItems.get(i), FormEntryCaption.TEXT_FORM_IMAGE);
                if(imageURI != null) {
                	try {
						Bitmap b = BitmapFactory.decodeStream(ReferenceManager._().DeriveReference(imageURI).getStream());
						BitmapDrawable bd = new BitmapDrawable(b);
						DisplayMetrics dm = Resources.getSystem().getDisplayMetrics();
						bd.setBounds(new Rect(0,0,b.getScaledWidth(dm),b.getScaledHeight(dm)));
						c.setCompoundDrawables(bd,null,null,null);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InvalidReferenceException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
                }
                
                c.setText(prompt.getSelectChoiceText(mItems.get(i)));
                c.setTextSize(TypedValue.COMPLEX_UNIT_PX, GlobalConstants.APPLICATION_FONTSIZE);

                for (int vi = 0; vi < ve.size(); vi++) {
                    // match based on value, not key
                    if (mItems.get(i).getValue().equals(((Selection) ve.elementAt(vi)).getValue())) {
                        c.setChecked(true);
                        break;
                    }
                    
                }
                
                c.setFocusable(!prompt.isReadOnly());
                c.setEnabled(!prompt.isReadOnly());
                
                
                if(prompt.getSelectTextForms(mItems.get(i)).contains(FormEntryCaption.TEXT_FORM_AUDIO)) {
                    String audioUri = prompt.getSelectChoiceText(mItems.get(i), FormEntryCaption.TEXT_FORM_AUDIO);
					AudioButton audioButton = new AudioButton(getContext(), audioUri);
					
					LinearLayout rl = (LinearLayout)View.inflate(this.getContext(),R.layout.radiobutton_linearlayout, null);
					View template = rl.findViewById(R.id.radiobuttontemplate);
					
					LinearLayout holder = new LinearLayout(this.getContext());
					holder.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
					
					c.setLayoutParams(template.getLayoutParams());
					
					holder.addView(c,template.getLayoutParams());
					holder.addView(audioButton);
					addView(holder);
                } else {
                    addView(c);
                }
            }
        }

        mCheckboxInit = false;
    }


    public void setFocus(Context context) {
        // Hide the soft keyboard if it's showing.
        InputMethodManager inputManager =
                (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(this.getWindowToken(), 0);
    }

}
