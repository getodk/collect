package org.odk.collect.android.views;

import org.javarosa.form.api.FormEntryCaption;
import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.GroupDef;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.form.api.FormEntryModel;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import org.odk.collect.android.views.layout.GroupLayoutFactory;
import org.odk.collect.android.views.layout.IGroupLayout;
import org.odk.collect.android.widgets.AbstractQuestionWidget;
import org.odk.collect.android.application.Collect;

/**
 * Renders a FolioView with multiple widgets based upon the 
 * value of the appearance attribute of the underlying javarosa
 * group.
 * 
 * @author imyjimmy@uw.edu
 * @author mitchellsundt@gmail.com
 *
 */
public class GroupView extends AbstractFolioView {
	
    /**
     * Member data common to rendering a group or question widget
     */
	private String t = "GroupView";
	
	private AbstractQuestionWidget lastViewInFocus = null;
	private AbstractQuestionWidget viewInFocus = null;
	
    private final IGroupLayout groupLayout;
    
	public GroupView(Handler handler, FormIndex formIndex, Context context) {
		super(handler, formIndex, context);
		FormDef fd = Collect.getInstance().getFormEntryController().getModel().getForm();
        GroupDef gd = (GroupDef) fd.getChild(formIndex); // may throw exceptions...
		groupLayout = GroupLayoutFactory.createGroupLayout(gd.getAppearanceAttr());
	}
	
	/**
	 * Navigate through the group, collecting and returning FormEntryPrompt objects
	 * for all the top-level questions within the group.
	 *  
	 * @param model the form entry model
	 * @return list of form indices for the top-level questions within the group
	 * @throws IllegalStateException if a repeating group
	 */
	private List<FormIndex> getTopLevelFormIndicesInGroup(FormEntryModel model) {
		FormDef fd = model.getForm();
        GroupDef gd = (GroupDef) fd.getChild(formIndex); // may throw exceptions...
        if ( gd.getRepeat() ) {
        	throw new IllegalStateException("Repeating group with field list appearance is not allowed.");
        }
        
        List<FormIndex> indicies = new ArrayList<FormIndex>();
        FormIndex idxChild = fd.incrementIndex(formIndex, true); // descend into group
        for (@SuppressWarnings("unused") Object child : gd.getChildren()) {
        	indicies.add(idxChild);
        	idxChild = fd.incrementIndex(idxChild, false); // don't descend
        }
        return indicies;
	}
	
    /* (non-Javadoc)
	 * @see org.odk.collect.android.views.IFoliosView#buildView(org.javarosa.form.api.FormEntryCaption[])
	 */
	public void buildView(String instancePath, FormEntryCaption[] groups) {

		FormEntryModel model = Collect.getInstance().getFormEntryController().getModel();
		List<FormIndex> indices = getTopLevelFormIndicesInGroup(model);
		
		groupLayout.buildView(this, indices, instancePath, groups);

		lastViewInFocus = groupLayout.getDefaultFocus();
        viewInFocus = lastViewInFocus;
	}
	
	public void onDescendantFocusChange(AbstractQuestionWidget qv, FormIndex idx, boolean hasFocus) {
		Log.i(t,"onDescendantFocusChange: " + qv.getFormIndex().toString() +
				"index: " + idx.toString() +
				"hasFocus: " + Boolean.toString(hasFocus));

		if ( hasFocus ) {
			if ( qv != viewInFocus && viewInFocus != null ) {
				// there is a new focus -- save data in old focus
				Log.i(t,"onDescendantFocusChange: invoking saveCurrentAnswer " + 
						viewInFocus.getFormIndex().toString());
				if ( !viewInFocus.saveAnswer(true) ) {
					// data save failed -- change focus back!
					viewInFocus.setFocus(this.getContext());
					return;
				}
				lastViewInFocus = viewInFocus;
			}
			viewInFocus = qv;
		} else if ( !hasFocus && qv == viewInFocus ) {
			if ( viewInFocus != null ) {
				// we are loosing focus -- save data
				Log.i(t,"onDescendantFocusChange: invoking saveCurrentAnswer " + 
						viewInFocus.getFormIndex().toString());
				if ( !viewInFocus.saveAnswer(true) ) {
					// data save failed -- change focus back!
					viewInFocus.setFocus(this.getContext());
					return;
				}
				lastViewInFocus = viewInFocus;
			}
			viewInFocus = null;
		}
	}

	/* (non-Javadoc)
	 * @see org.odk.collect.android.views.IFolioView#getFormIndex()
	 */
	@Override
	public FormIndex getFormIndex() {
    	if ( viewInFocus != null ) return viewInFocus.getFormIndex();
    	else if (lastViewInFocus != null ) return lastViewInFocus.getFormIndex();
    	else {
    		return groupLayout.getDefaultFocus().getFormIndex();
    	}
	}

    
    /* (non-Javadoc)
	 * @see org.odk.collect.android.views.IFoliosView#getAnswer()
	 */
    public IAnswerData getAnswer() {
    	if ( viewInFocus != null ) return viewInFocus.getAnswer();
    	else if (lastViewInFocus != null ) return lastViewInFocus.getAnswer();
    	else {
    		return null;
    	}
    }

    /* (non-Javadoc)
	 * @see org.odk.collect.android.views.IFoliosView#setBinaryData(java.lang.Object)
	 */
    public void setBinaryData(Object answer) {
    	if ( viewInFocus != null ) viewInFocus.setBinaryData(answer);
    	else if (lastViewInFocus != null ) lastViewInFocus.setBinaryData(answer);
    	else {
    		// log
    	}
    }

    /* (non-Javadoc)
	 * @see org.odk.collect.android.views.IFoliosView#clearAnswer()
	 */
    public void clearAnswer(boolean evaluateContraints) {
    	if ( viewInFocus != null ) viewInFocus.clearAnswer(evaluateContraints);
    	else if ( lastViewInFocus != null ) lastViewInFocus.clearAnswer(evaluateContraints);
    	else {
    		// log
    	}
    }

    /* (non-Javadoc)
	 * @see org.odk.collect.android.widgets.IQuestionWidget#unregister()
	 */
	@Override
	public void unregister() {
		groupLayout.unregister();
	}

    /* (non-Javadoc)
	 * @see org.odk.collect.android.views.IFoliosView#setFocus(android.content.Context)
	 */
    public void setFocus(Context context) {
    	if ( viewInFocus == null ) {
    		viewInFocus = lastViewInFocus;
    		
    		if ( viewInFocus == null ) {
    			// use the layout's default focus
    			viewInFocus = groupLayout.getDefaultFocus();
    		}
    	}
    	
    	viewInFocus.setFocus(context);
    }

	public void setSubFocus(FormIndex subIndex) {
		lastViewInFocus = groupLayout.getQuestionWidget(subIndex);
        viewInFocus = lastViewInFocus;
	}
}
