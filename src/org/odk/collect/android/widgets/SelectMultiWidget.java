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

import java.util.Vector;

import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.SelectMultiData;
import org.javarosa.core.model.data.helper.Selection;
import org.javarosa.form.api.FormEntryCaption;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.views.AbstractFolioView;
import org.odk.collect.android.views.IAVTLayout;
import org.odk.collect.android.widgets.AbstractQuestionWidget.OnDescendantRequestFocusChangeListener.FocusChangeState;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.CompoundButton.OnCheckedChangeListener;

/**
 * SelctMultiWidget handles multiple selection fields using checkboxes.
 * 
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */
public class SelectMultiWidget extends AbstractQuestionWidget implements IMultipartSelectWidget, 
	OnCheckedChangeListener {
    
    private boolean insideUpdate = false;

    /**
     * The buttons ordering is the same as the prompt's Vector<SelectChoice>
     */
    final CheckBox[] buttons;

    public SelectMultiWidget(Handler handler, Context context, FormEntryPrompt prompt) {
        super(handler, context, prompt);
        int dim = 0;
        if ( prompt.getSelectChoices() != null ) {
        	dim = prompt.getSelectChoices().size();
        }
        if ( dim == 0 ) {
        	buttons = null;
        } else {
        	buttons = new CheckBox[dim];
        }
    }
    
    @Override
	public IAnswerData getAnswer() {
        Vector<Selection> vc = new Vector<Selection>();
        for (int i = 0; i < prompt.getSelectChoices().size(); i++) {
            CheckBox c = buttons[i];
            if (c.isChecked()) {
            	String value = prompt.getSelectChoices().get(i).getValue();
    			Log.i(SelectMultiWidget.class.getName(), "getAnswer checked: " + value);
                vc.add(new Selection(value));
            }
        }

        if (vc.size() == 0) {
            return null;
        } else {
            return new SelectMultiData(vc);
        }

    }

    @Override
    protected void buildViewBodyImpl() {
    	// buildStart
		Vector<SelectChoice> items = prompt.getSelectChoices();
		if ( items != null ) {
	    	for ( SelectChoice c : items ) {
	    		buildSelectElement(c);
	    	}
		}
    }

	@Override
	public ViewGroup buildSelectElement(SelectChoice sc) {
		Vector<SelectChoice> items = prompt.getSelectChoices();
		if ( items == null ) {
			// should never get here...
			throw new IllegalStateException("no selection choices!");
		}

		int i;
		for ( i = 0 ; i < items.size() ; ++i ) {
			if ( items.get(i).equals(sc) ) break;
		}
		
		if ( i == items.size() ) {
			throw new IllegalArgumentException("selection choice not found!");
		}

		if ( i < 0 ) {
	        // Add a dividing line above this element
	        ImageView divider = new ImageView(getContext());
	        
	        divider.setId(AbstractQuestionWidget.newUniqueId());
	        divider.setBackgroundResource(android.R.drawable.divider_horizontal_bright);
            addView(divider);
		}
		
        // no checkbox group so id by answer + offset
        CheckBox c = new CheckBox(getContext());
        buttons[i] = c;

        c.setId(AbstractQuestionWidget.newUniqueId());
        c.setText(prompt.getSelectChoiceText(sc));
        c.setTextSize(TypedValue.COMPLEX_UNIT_PX, AbstractFolioView.APPLICATION_FONTSIZE);
        c.setEnabled(!prompt.isReadOnly());
        
        // when clicked, check for readonly before toggling
        c.setOnCheckedChangeListener(this);

        String audioURI =
                prompt.getSpecialFormSelectChoiceText(sc, FormEntryCaption.TEXT_FORM_AUDIO);
        
        String imageURI =
                prompt.getSpecialFormSelectChoiceText(sc, FormEntryCaption.TEXT_FORM_IMAGE);

        String videoURI =
        		prompt.getSpecialFormSelectChoiceText(sc, "video");
        
        String bigImageURI = null;
        bigImageURI = prompt.getSpecialFormSelectChoiceText(sc, "big-image");
         
        IAVTLayout mediaLayout = new IAVTLayout(getContext());
        
        mediaLayout.setId(AbstractQuestionWidget.newUniqueId());
        mediaLayout.setAVT(c, audioURI, imageURI, videoURI, bigImageURI);
        
        addView(mediaLayout);
        return this;
	}

	@SuppressWarnings(value = { "unchecked" })
    protected void updateViewAfterAnswer() {
    	try {
    		insideUpdate = true;
	    	IAnswerData answer = prompt.getAnswerValue();
	    	Vector<Selection> ve;
	    	if ( (answer == null) || (answer.getValue() == null) ) {
	    		ve = new Vector<Selection>();
	    	} else {
	    		ve = (Vector<Selection>) answer.getValue();
	    	}
	
	    	if ( buttons != null ) {
		    	for ( int i = 0 ; i < buttons.length; ++i ) {
		            CheckBox c = buttons[i];
		            
		            String value = prompt.getSelectChoices().get(i).getValue();
		            boolean found = false;
		            for (Selection s : ve) {
		            	if ( value.equals(s.getValue())) {
		            		found = true;
		            		break;
		            	}
		            }
		            
					Log.i(SelectMultiWidget.class.getName(), 
							"updateViewAfterAnswer: " + value + " isChecked: " + Boolean.toString(found) );
		            c.setChecked(found);
		    	}
	    	}
    	} finally {
    		insideUpdate = false;
    	}
    }

    @Override
    public void setEnabled(boolean isEnabled) {
    	if ( buttons != null ) {
	    	for ( View v : buttons) {
	    		v.setEnabled(isEnabled && !prompt.isReadOnly());
	    	}
    	}
    }

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    	Log.i(SelectMultiWidget.class.getName(), 
    			"onCheckedChanged isChecked:" + Boolean.toString(isChecked));
    	// no-op if read-only
    	// no-op if insideUpdate
        if (!prompt.isReadOnly() && !insideUpdate) {
        	// hide the soft keyboard if it is displayed for some other control...
        	setFocus(getContext());
	        signalDescendant(FocusChangeState.FLUSH_CHANGE_TO_MODEL);
        }
	}
}
