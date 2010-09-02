package org.odk.collect.android.views;

import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.form.api.FormEntryCaption;
import org.javarosa.form.api.FormEntryController;
import org.javarosa.form.api.FormEntryModel;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.widgets.AbstractQuestionWidget.OnDescendantFocusChangeListener;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.widget.ScrollView;

/**
 * Common base class to simple question views, field list views, and 
 * complex mutually-exclusive rankings views.
 * 
 * @author mitchellsundt@gmail.com
 *
 */
public abstract class AbstractFolioView extends ScrollView implements OnDescendantFocusChangeListener {

	public final static int APPLICATION_FONTSIZE = 23;

	/**
	 * Member variables...
	 */

	/** handler object for posting UI updates to the view thread */
	protected final Handler handler;
	/** index corresponding to this view.  May be a group or a prompt */
    protected final FormIndex formIndex;

	public AbstractFolioView(Handler handler, FormIndex formIndex, Context context) {
		super(context);
		this.handler = handler;
		this.formIndex = formIndex;
	}

	public AbstractFolioView(Handler handler, FormIndex formIndex, Context context, AttributeSet attrs) {
        super(context, attrs);
        this.handler = handler;
		this.formIndex = formIndex;
    }

	public AbstractFolioView(Handler handler, FormIndex formIndex, Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
		this.formIndex = formIndex;
        this.handler = handler;
    }

	/**
	 * Create the appropriate widget for the form index specified in the constructor.
	 * This binds the UI to the form model.  The model should be detached from the
	 * UI with the unregister() call.  
	 * 
	 * Otherwise, the UI will never be garbage collected!	
	 */
	public abstract void buildView(String instancePath, FormEntryCaption[] groups);

	public abstract IAnswerData getAnswer();

	public abstract void setBinaryData(Object answer);

	public abstract void clearAnswer(boolean evaluateContraints);

	public abstract void setFocus(Context context);

	/**
	 * @return the formIndex for the question with focus.
	 */
	public abstract FormIndex getFormIndex();

	/**
	 * Unregister this UI element from the underlying form data model.
	 */
	public abstract void unregister();
	
	/**
	 * Obtain the handler object for UI updates...
	 */
	public final Handler getHandler() {
		return handler;
	}

	/**
	 * Attempt to save the answer to the current prompt into the data model.
	 * 
	 * @param evaluateConstraints
	 * @return true on success, false otherwise
	 */
	public boolean saveCurrentAnswer(boolean evaluateConstraints) {
		
		FormEntryController fec = Collect.getInstance().getFormEntryController();
		FormEntryModel model = fec.getModel();
		
		// since we have group and composite views, we have to ask the 
		// derived view what the actual form index is... don't use
		// the formIndex value!!!  It is for the enclosing group.
		FormIndex index = getFormIndex();
		
		if (!model.isIndexReadonly(index)
				&& model.getEvent(index) == FormEntryController.EVENT_QUESTION) {
			if (evaluateConstraints) {
				int saveStatus = fec.answerQuestion(index, getAnswer());

				if ( saveStatus != FormEntryController.ANSWER_OK ) {
					Collect.getInstance().createConstraintToast(
							model.getQuestionPrompt(index)
							.getConstraintText(), saveStatus);
					return false;
				}
			} else {
				fec.saveAnswer(index, getAnswer());
			}
		}
		return true;
	}
}