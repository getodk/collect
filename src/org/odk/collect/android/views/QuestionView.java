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

package org.odk.collect.android.views;

import java.io.File;

import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.form.api.FormEntryCaption;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.widgets.AbstractQuestionWidget;
import org.odk.collect.android.widgets.WidgetFactory;

import android.content.Context;
import android.os.Handler;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Responsible for using a {@link FormEntryCaption} and based on the question type and answer type,
 * displaying the appropriate widget. The class also sets (but does not save) and gets the answers
 * to questions.
 * 
 * @author Yaw Anokwa (yanokwa@gmail.com)
 * @author Carl Hartung (carlhartung@gmail.com)
 */

public class QuestionView extends AbstractFolioView {
 
    /**
     * Member data specific to rendering a question widget
     */
    private AbstractQuestionWidget mQuestionWidget = null;

    public QuestionView(Handler handler, FormIndex formIndex, Context context) {
        super(handler, formIndex, context);
    }

	private LinearLayout commonBuildView(File instanceDir, FormEntryCaption[] groups) {
		FormEntryPrompt formEntryPrompt = Collect.getInstance().getFormEntryController().getModel().getQuestionPrompt(formIndex);

		LinearLayout mView = new LinearLayout(getContext());
        mView.setOrientation(LinearLayout.VERTICAL);
        mView.setGravity(Gravity.TOP);
        mView.setPadding(0, 7, 0, 0);

        LinearLayout.LayoutParams mLayout =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
        mLayout.setMargins(10, 0, 10, 0);

        // put crumb trail on top...
        String crumbTrail = GroupView.getGroupText(groups);
        if (crumbTrail.length() > 0) {
            TextView tv = new TextView(getContext());
            tv.setText(crumbTrail.substring(0, crumbTrail.length() - 3));
            tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, AbstractQuestionWidget.TEXTSIZE - 7);
            tv.setPadding(0, 0, 0, 5);
            mView.addView(tv, AbstractQuestionWidget.COMMON_LAYOUT);
        }

        // if question or answer type is not supported, use text widget
        mQuestionWidget = WidgetFactory.createWidgetFromPrompt(handler, formEntryPrompt, getContext(), instanceDir);
        
        return mView;
	}

    /* (non-Javadoc)
	 * @see org.odk.collect.android.views.IFoliosView#buildView(org.javarosa.form.api.FormEntryCaption[])
	 */
    public void buildView(File instanceDir, FormEntryCaption[] groups) {
    	// build encapsulating layout
    	LinearLayout mView = commonBuildView(instanceDir, groups);

        LinearLayout.LayoutParams mLayout =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
        mLayout.setMargins(10, 0, 10, 0);
    	
    	// build actual widget...
    	mQuestionWidget.buildView(this);

    	// add the widget to the encapsulating layout
    	mView.addView(mQuestionWidget,mLayout);
    	
    	// set the scroll view's pane to the encapsulating layout
        addView(mView);
    }

	/* (non-Javadoc)
	 * @see org.odk.collect.android.views.IFolioView#getFormIndex()
	 */
	@Override
	public FormIndex getFormIndex() {
		return formIndex;
	}
    
    /* (non-Javadoc)
	 * @see org.odk.collect.android.views.IFolioView#unregister()
	 */
	@Override
	public void unregister() {
		mQuestionWidget.unregister();
	}

	/* (non-Javadoc)
	 * @see org.odk.collect.android.views.IFoliosView#getAnswer()
	 */
    public IAnswerData getAnswer() {
        return mQuestionWidget.getAnswer();
    }

    /* (non-Javadoc)
	 * @see org.odk.collect.android.views.IFoliosView#setBinaryData(java.lang.Object)
	 */
    public void setBinaryData(Object answer) {
    	mQuestionWidget.setBinaryData(answer);
    }

    /* (non-Javadoc)
	 * @see org.odk.collect.android.views.IFoliosView#clearAnswer()
	 */
    public void clearAnswer(boolean evaluateContraints) {
        mQuestionWidget.clearAnswer(evaluateContraints);
    }

    /* (non-Javadoc)
	 * @see org.odk.collect.android.views.IFoliosView#setFocus(android.content.Context)
	 */
    public void setFocus(Context context) {
		mQuestionWidget.setFocus(context);
    }

	@Override
	public boolean onDescendantRequestFocusChange(AbstractQuestionWidget qv,
			FormIndex fi, FocusChangeState focusState) {
		// Since we only have the one widget, allow anything.
		// Navigations off the widget will trigger saves of this widget's value.
		return true;
	}
}
