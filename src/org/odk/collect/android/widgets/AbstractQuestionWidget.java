package org.odk.collect.android.widgets;

import org.javarosa.core.model.FormElementStateListener;
import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.form.api.FormEntryCaption;
import org.javarosa.form.api.FormEntryController;
import org.javarosa.form.api.FormEntryModel;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.views.IAVTLayout;
import org.odk.collect.android.views.AbstractFolioView;
import org.odk.collect.android.widgets.AbstractQuestionWidget.OnDescendantRequestFocusChangeListener.FocusChangeState;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * AbstractQuestionWidget is the main element in a single-question IFolioView (e.g., QuestionView).
 * QuestionView is a ScrollView, so widget designers don't need to worry about scrolling.  This 
 * abstract class replaces the IQuestionWidget interface.
 * <p> 
 * Each of these objects is associated with a single formIndex.  Look at the GroupView if you need
 * to display and control elements associated with multiple formIndex values.
 * <p>
 * Widgets notify their FolioView parents when they gain focus, as indicated by the user taking an
 * action that alters their UI values.  I.e., focus change is detected lazily after UI changes.
 * Navigating (e.g., tabbing) through the UI does not report focus change events.  The parents are 
 * responsible for the proper saving of answers (with or without constraint checking).  
 * <p>
 * Derived classes must implement @see {@link #buildViewBodyImpl()} to construct their UI elements.
 * The proper initialization of those elements with data values from the javarosa model should be deferred
 * until @see {@link #updateViewAfterAnswer()} which is called after <code>buildViewBodyImpl</code> and
 * is required to completely establish the state of the UI components (enabled, disabled, values, etc.)
 * based upon the <code>IAnswerData</code> in the javarosa model.
 * <p>
 * Derived classes also must implement @see {@link #getAnswer()} to return an <code>IAnswerData</code> 
 * object for saving into the javarosa model.
 * <p>
 * Derived classes should override @see {@link #setEnabled(boolean)} to propagate the enabled/disabled
 * status from the model's isRelevant and isReadOnly settings into the UI.
 * <p>
 * This class handles the setting up and tearing down of the linkages between the UI and the javarosa model.
 * 
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author mitchellsundt@gmail.com
 */
public abstract class AbstractQuestionWidget extends LinearLayout implements IBinaryWidget,
		org.javarosa.formmanager.view.IQuestionWidget {

	private static int idGenerator = 1211322;
	
	/**
	 * Generate a unique ID to keep Android UI happy when the screen orientation 
	 * changes.
	 * 
	 * @return
	 */
	public static int newUniqueId() {
		return ++idGenerator;		
	}
	
	/**
	 * Callback interface to ask interested parties if the input focus
	 * can change to the requested AbstractQuestionWidget.
	 */
	public static interface OnDescendantRequestFocusChangeListener {
		enum FocusChangeState {
			DIVERGE_VIEW_FROM_MODEL, // for data entry widgets
			FLUSH_CHANGE_TO_MODEL // for selection widgets
		}
		/**
		 * Ask the registered listener if it is OK to change focus to the given
		 * AbstractQuestionWidget.  Because of the lazy focus setting during touch
		 * mode operation, this callback is generally invoked after the view value
		 * in the requesting widget (qv) has diverged from the model value.
		 * <p>
		 * The exception to that rule is for trigger, barcode, image, audio, video
		 * and other launching commands, which should test for a true return value
		 * before taking their action.
		 * <p>
		 * A true return value indicates that the focus and value change were allowed, 
		 * while a false indicates that either the focus change or value change were 
		 * not allowed.  In general, if a false is returned, no further actions should
		 * be taken.
		 * <p> 
		 * If the FocusChangeState is DIVERGE_VIEW_FROM_MODEL, then when a focus 
		 * change is denied (the callback returns false), the widget (qv) making this 
		 * request will have had its view value reset to the associated model value 
		 * and the focus will remain on the previous widget.  Otherwise, when a focus
		 * change is allowed (the callback returns true), the view value that had been
		 * altered prior to this call is allowed to remain divergent from the model 
		 * value (it will not have been saved to the model).
		 * <p>
		 * If the FocusChangeState is FLUSH_CHANGE_TO_MODEL, then when the focus 
		 * change is denied, (the callback returns false) the widget (qv) making this 
		 * request will have had its view value reset to the associated model value and
		 * the focus will remain on the previous widget.  If the focus change is 
		 * allowed, the requesting widget (qv) value is then saved into the model.  If 
		 * that save fails (the callback returns false), the widget (qv) making the 
		 * request will have had its view value reset to the associated model value and 
		 * the focus remains on the requesting widget (qv).   Otherwise, if the focus 
		 * change and value change are allowed, the (the callback returns true), the 
		 * requesting view (qv) will have focus and the view value will match the model
		 * value.
		 * 
		 * @param qv  the requesting view
		 * @param fi  the form index associated with that view
		 * @param focusState	the state change requested
		 * @return true if the change is OK (i.e., no constraint violated).
		 */
		public abstract boolean onDescendantRequestFocusChange(AbstractQuestionWidget qv, FormIndex fi, FocusChangeState focusState);
	}

	/** standard text size for widget text */
	public final static int TEXTSIZE = 21;
	/** standard indenting for sub-elements */
	public final static LinearLayout.LayoutParams COMMON_LAYOUT;
	static {
		COMMON_LAYOUT =
            new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
		COMMON_LAYOUT.setMargins(10, 0, 10, 0);
	}

	/** handler for dispatching UI updates */
	protected final Handler handler;
	
	/** javarosa prompt rendered by this widget */
	protected final FormEntryPrompt prompt;
	
	/** callback for focus change events */
    protected OnDescendantRequestFocusChangeListener descendantRequestFocusChangeListener;

    /**
     * Update the enable/disable state of the UI element.
     * @see #refreshWidget(int)
     */
    private final Runnable doChangeEnabled = new Runnable() {
		@Override
		public void run() {
			FormEntryController fec = Collect.getInstance().getFormEntryController();
			FormEntryModel m = fec.getModel();
			
			final boolean isEnabled = m.isIndexRelevant(prompt.getIndex()) &&
									!m.isIndexReadonly(prompt.getIndex());
			
			setEnabled(isEnabled);
		}
    };

    /**
     * Update the values shown by the UI element.
     * @see #refreshWidget(int)
     */
    private final Runnable doChangeValue = new Runnable() {
		@Override
		public void run() {
			updateViewAfterAnswer();
		}
    };


    public AbstractQuestionWidget(Handler handler, Context context, FormEntryPrompt prompt) {
        super(context);
        this.handler = handler;
        this.prompt = prompt;
    }

    public final FormIndex getFormIndex() {
    	return prompt.getIndex();
    }
    
    /**
     * Access the data value represented by the UI of this widget
     * @return the answerData representation of the UI value
     */
    public abstract IAnswerData getAnswer();
	
	/**
	 * Default implementation logs an error.
	 */
	@Override
    public void setBinaryData(Object answer) {
        Log.e(getClass().getName(), "Attempted to setBinaryData() on a non-binary widget ");
    }

	/**
	 * Constructs the parts of the UI that are not the label and hint elements.
	 * Note that the constructed UI is synchronized with the model after returning
	 * from this method.  The registrations for model changes and for UI focus changes
	 * are established after this synchronization is complete.  
	 * 
	 * @see #buildViewEnd(AbstractFolioView)
	 */
    protected abstract void buildViewBodyImpl();

    /**
     * Add a TextView containing the hierarchy of groups to which the question belongs.
     */
    private final void AddGroupText(FormEntryCaption[] groups) {
        StringBuffer s = new StringBuffer("");
        String t = "";
        int i;

        // list all groups in one string
        for (FormEntryCaption g : groups) {
            i = g.getMultiplicity() + 1;
            t = g.getLongText();
            if (t != null) {
                s.append(t);
                if (g.repeats() && i > 0) {
                    s.append(" (" + i + ")");
                }
                s.append(" > ");
            }
        }

        // build view
        if (s.length() > 0) {
            TextView tv = new TextView(getContext());
            tv.setText(s.substring(0, s.length() - 3));
            tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, TEXTSIZE - 7);
            tv.setPadding(0, 0, 0, 5);
            addView(tv, COMMON_LAYOUT);
        }
    }


    /**
     * Add a Views containing the question text, audio (if applicable), and image (if applicable).
     * To satisfy the RelativeLayout constraints, we add the audio first if it exists, then the
     * TextView to fit the rest of the space, then the image if applicable.
     */
    private final void AddQuestionText(FormEntryPrompt p) {
        String imageURI = p.getImageText();
        String audioURI = p.getAudioText();
        String videoURI = p.getSpecialFormQuestionText("video");
    
        // shown when image is clicked
        String bigImageURI = p.getSpecialFormQuestionText("big-image");

        // Add the text view. Textview always exists, regardless of whether there's text.
        TextView questionText = new TextView(getContext());
        questionText.setText(p.getLongText());
        questionText.setTextSize(TypedValue.COMPLEX_UNIT_PX, TEXTSIZE);
        questionText.setTypeface(null, Typeface.BOLD);
        questionText.setPadding(0, 0, 0, 7);
        questionText.setId(AbstractQuestionWidget.newUniqueId()); // assign random id

        // Wrap to the size of the parent view
        questionText.setHorizontallyScrolling(false);

        // Create the layout for audio, image, text
        IAVTLayout mediaLayout = new IAVTLayout(getContext());
        mediaLayout.setAVT(questionText, audioURI, imageURI, videoURI, bigImageURI);

        addView(mediaLayout, COMMON_LAYOUT);
    }


    /**
     * Add a TextView containing the help text.
     */
    private final void AddHelpText(FormEntryPrompt p) {

        String s = p.getHelpText();

        if (s != null && !s.equals("")) {
            TextView tv = new TextView(getContext());
            tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, TEXTSIZE - 5);
            tv.setPadding(0, -5, 0, 7);
            // wrap to the widget of view
            tv.setHorizontallyScrolling(false);
            tv.setText(s);
            tv.setTypeface(null, Typeface.ITALIC);

            addView(tv, COMMON_LAYOUT);
        }
    }

    /**
     * Common functionality for constructing the widget.
     * 
     * @param groups
     */
    protected final void buildViewBoilerplate(FormEntryCaption[] groups) {
        setOrientation(LinearLayout.VERTICAL);
        setGravity(Gravity.TOP);
        setPadding(0, 7, 0, 0);

        // display which group you are in as well as the question
        AddGroupText(groups);
        AddQuestionText(prompt);
        AddHelpText(prompt);
    }

    /**
     * Construct the UI of this widget.  The passed-in IFolioView
     * is set to receive descendant focus change events.  It is not
     * necessarily the view parent of this widget.
     * 
     * @param fv the view to receive descendant focus change events.
     * @param groups the nested hierarchy of enclosing groups
     */
    public final void buildView(final AbstractFolioView fv, FormEntryCaption[] groups) {
    	buildViewStart(groups);
        buildViewBodyImpl();
        buildViewEnd(fv);
    }
    
    /**
     * Construct the text, image, audio and video labels and other 
     * boilerplate UI of this widget.
     * 
     * @param groups the nested hierarchy of enclosing groups
     */
    public final void buildViewStart(FormEntryCaption[] groups) {
    	buildViewBoilerplate(groups);
    }
    
    /**
     * Completes the synchronization of the UI with the model
     * and registers it to receive updates from model changes and
     * to report focus changes to its folio parent.
     * 
     * @param fv the view to receive descendant focus change events.
     */
    public final void buildViewEnd(AbstractFolioView fv) {
        updateViewAfterAnswer();
        setEnabled(Collect.getInstance().getFormEntryController().getModel().isIndexRelevant(prompt.getIndex()));

        prompt.register(this);
    	setOnDescendantFocusChangeListener(fv);
    }
    
    /**
     * Clear the answer with constraint checking...
     */
    public final boolean clearAnswer(boolean evaluateConstraints) {
    	if ( !prompt.isReadOnly() ) {
	        FormEntryController fec = Collect.getInstance().getFormEntryController();
			FormEntryModel model = fec.getModel();
			
			if (evaluateConstraints) {
				int saveStatus = fec.answerQuestion(prompt.getIndex(), null);
				updateViewAfterAnswer();

				if ( saveStatus != FormEntryController.ANSWER_OK ) {
					Collect.getInstance().createConstraintToast(
							model.getQuestionPrompt(prompt.getIndex())
							.getConstraintText(), saveStatus);
					return false;
				}
			} else {
				fec.saveAnswer(prompt.getIndex(), null);
				updateViewAfterAnswer();
			}
    	}
    	return true;
    }

    /** 
     * Save the answer with constraint checking...
     */
    public final boolean saveAnswer(boolean evaluateConstraints) {
    	if ( !prompt.isReadOnly() ) {
	        FormEntryController fec = Collect.getInstance().getFormEntryController();
			FormEntryModel model = fec.getModel();
			
			if (evaluateConstraints) {
				int saveStatus = fec.answerQuestion(prompt.getIndex(), getAnswer());
				if ( saveStatus != FormEntryController.ANSWER_OK ) {
					Collect.getInstance().createConstraintToast(
							model.getQuestionPrompt(prompt.getIndex())
							.getConstraintText(), saveStatus);
					return false;
				}
				updateViewAfterAnswer();

			} else {
				fec.saveAnswer(prompt.getIndex(), getAnswer());
				updateViewAfterAnswer();
			}
    	}
    	return true;
    }

    /**
     * Called within a group view if a UI change causes a constraint violation
     * so that the constraint failure can be fixed before continuing.
     */
    public final void resetViewFromAnswer() {
    	updateViewAfterAnswer();
    }
    
    /**
     * Detach the UI from the underlying Form data model.
	 */
	public final void unregister() {
		prompt.unregister();
	}

    protected abstract void updateViewAfterAnswer();

    /**
     * Set the UI focus to be this widget.  Handles figuring out what
     * to do with the input keyboard.
     * <p>
     * Most implementations don't need the soft keyboard...
     * 
     * @param context
     */
	public void setFocus(Context context) {
        // Hide the soft keyboard if it's showing.
		Collect.getInstance().hideSoftKeyboard(this);
    }
	
    /**
     * This is called by the javarosa framework whenever the underlying 
     * data values have changed.  Since saving data is a background task,
     * this may be called by the background task thread (though in most
     * cases it would be called by the UI thread).  As a result, we
     * cannot directly update the UI here, but must use a Runnable and 
     * handler to do the UI update. 
     * 
     * @see org.javarosa.formmanager.view.IQuestionWidget#refreshWidget(int)
     * 
     * @param changeFlags what has changed (relevance, readOnly, data)
     */
	@Override
	public final void refreshWidget(int changeFlags) {
		
		// not handled: CHANGE_LOCALE
		// not handled: CHANGE_REQUIRED
		if ( (changeFlags & FormElementStateListener.CHANGE_RELEVANT) != 0 ) {
			handler.post(doChangeEnabled);
		}
		if ( (changeFlags & FormElementStateListener.CHANGE_ENABLED) != 0 ) {
			handler.post(doChangeEnabled);
		}
		
		if ( (changeFlags & FormElementStateListener.CHANGE_DATA) != 0 ) {
			handler.post(doChangeValue);
		}
	}
	
	/**
	 * @return the descendantFocusChangeListener
	 */
	public final OnDescendantRequestFocusChangeListener getOnDescendantFocusChangeListener() {
		return descendantRequestFocusChangeListener;
	}

	/**
	 * @param descendantFocusChangeListener the descendantFocusChangeListener to set
	 */
	public final void setOnDescendantFocusChangeListener(
			OnDescendantRequestFocusChangeListener descendantFocusChangeListener) {
		this.descendantRequestFocusChangeListener = descendantFocusChangeListener;
	}

	/**
	 * Common routine to signal any descendant listener that a focus change has occurred.
	 * 
	 * @param hasFocus
	 */
	protected final boolean signalDescendant(FocusChangeState focusState) {
		if ( descendantRequestFocusChangeListener != null ) {
			Log.i(AbstractQuestionWidget.class.getName(), "signalDescendant: " + 
					getFormIndex().toString() + " " + focusState.toString());
			return descendantRequestFocusChangeListener.onDescendantRequestFocusChange(this, prompt.getIndex(), focusState);
		}
		return true;
	}
}
