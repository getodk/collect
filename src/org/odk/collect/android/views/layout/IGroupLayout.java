package org.odk.collect.android.views.layout;

import java.util.List;

import org.javarosa.core.model.FormIndex;
import org.javarosa.form.api.FormEntryCaption;
import org.odk.collect.android.views.GroupView;
import org.odk.collect.android.widgets.AbstractQuestionWidget;

public interface IGroupLayout {
	/**
	 * Construct the layout for the given view.
	 * 
	 * @param view under which to lay out widgets
	 * @param indices the javarosa fields to be rendered (ordered)
	 * @param instancePath the path to the storage for media captures
	 * @param groups the chain of groups, including this one, for display 
	 */
	public void buildView(GroupView view, List<FormIndex> indices, String instancePath, FormEntryCaption[] groups);
	
	/**
	 * @return the UI element that, by default, receives focus
	 */
	public AbstractQuestionWidget getDefaultFocus();
	
	/**
	 * Called to unregister all the UI elements from the model.
	 */
	public void unregister();

	/**
	 * Given a form index, return the folio view for that index.
	 * @param subIndex
	 * @return
	 */
	public AbstractQuestionWidget getQuestionWidget(FormIndex subIndex);
}
