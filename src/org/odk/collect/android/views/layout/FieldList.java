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

import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

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

        // build view
        
        // put crumb trail on top...
        String crumbTrail = GroupView.getGroupText(groups);
        if (crumbTrail.length() > 0) {
            TextView tv = new TextView(view.getContext());
            tv.setText(crumbTrail.substring(0, crumbTrail.length() - 3));
            tv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, AbstractQuestionWidget.TEXTSIZE - 7);
            tv.setPadding(0, 0, 0, 5);
            mView.addView(tv, AbstractQuestionWidget.COMMON_LAYOUT);
        }

        LinearLayout.LayoutParams mLayout =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
        mLayout.setMargins(10, 0, 10, 0);

        boolean first = true;
        for ( FormIndex fi : indices) {
        	if ( !first ) {
                // Add a dividing line before all but the first element
                View divider = new View(view.getContext());
                
                divider.setId(AbstractQuestionWidget.newUniqueId());
                divider.setBackgroundResource(android.R.drawable.divider_horizontal_bright);
                divider.setMinimumHeight(3);
                mView.addView(divider);
        	}
        	first = false;
        	
    		FormEntryPrompt formEntryPrompt = Collect.getInstance().getFormEntryController().getModel().getQuestionPrompt(fi);
            // if question or answer type is not supported, use text widget
    		AbstractQuestionWidget mQuestionWidget = WidgetFactory.createWidgetFromPrompt(view.getHandler(), formEntryPrompt, view.getContext(), instancePath);

    		mQuestionWidget.buildView(view);
            
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
