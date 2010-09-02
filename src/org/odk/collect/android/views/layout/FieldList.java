package org.odk.collect.android.views.layout;

import java.util.ArrayList;
import java.util.List;

import org.javarosa.core.model.FormIndex;
import org.javarosa.form.api.FormEntryCaption;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.views.GroupView;
import org.odk.collect.android.widgets.AbstractQuestionWidget;
import org.odk.collect.android.widgets.WidgetFactory;

import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;

public class FieldList implements IGroupLayout {
	
	// maintain separate list of all question views...
	private List<AbstractQuestionWidget> viewList = new ArrayList<AbstractQuestionWidget>();
    
    public FieldList() {
    }

    @Override
	public void buildView(GroupView view, List<FormIndex> indices, String instancePath, FormEntryCaption[] groups) {

		LinearLayout mView = new LinearLayout(view.getContext());
        mView.setOrientation(LinearLayout.VERTICAL);
        mView.setGravity(Gravity.TOP);
        mView.setPadding(0, 7, 0, 0);

        LinearLayout.LayoutParams mLayout =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
        mLayout.setMargins(10, 0, 10, 0);
		
        for ( FormIndex fi : indices) {
        	
    		FormEntryPrompt formEntryPrompt = Collect.getInstance().getFormEntryController().getModel().getQuestionPrompt(fi);
            // if question or answer type is not supported, use text widget
    		AbstractQuestionWidget mQuestionWidget = WidgetFactory.createWidgetFromPrompt(view.getHandler(), formEntryPrompt, view.getContext(), instancePath);

    		mQuestionWidget.buildView(view, groups);
            
            mView.addView((View) mQuestionWidget, mLayout);
            viewList.add(mQuestionWidget);
    	}
        
        view.addView(mView);
	}

	@Override
	public AbstractQuestionWidget getDefaultFocus() {
		return viewList.get(0);
	}

	@Override
	public AbstractQuestionWidget getQuestionWidget(FormIndex subIndex) {
		for ( AbstractQuestionWidget qv : viewList ) {
			if ( qv.getFormIndex().equals(subIndex) ) return qv;
		}
		return null;
	}

	@Override
	public void unregister() {
		for ( AbstractQuestionWidget v : viewList ) {
			v.unregister();
		}
	}
}
