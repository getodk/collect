/*
 * Copyright (C) 2009 JavaRosa
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

package org.odk.collect.android.logic;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Vector;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.GroupDef;
import org.javarosa.core.model.IDataReference;
import org.javarosa.core.model.IFormElement;
import org.javarosa.core.model.SubmissionProfile;
import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.services.transport.payload.ByteArrayPayload;
import org.javarosa.form.api.FormEntryCaption;
import org.javarosa.form.api.FormEntryController;
import org.javarosa.form.api.FormEntryModel;
import org.javarosa.form.api.FormEntryPrompt;
import org.javarosa.model.xform.XFormSerializingVisitor;
import org.javarosa.model.xform.XPathReference;
import org.javarosa.xform.parse.XFormParser;
import org.javarosa.xpath.XPathParseTool;
import org.javarosa.xpath.expr.XPathExpression;
import org.odk.collect.android.exception.JavaRosaException;
import org.odk.collect.android.views.ODKView;

import android.util.Log;

/**
 * This class is a wrapper for Javarosa's FormEntryController. In theory, if you wanted to replace
 * javarosa as the form engine, you should only need to replace the methods in this file. Also, we
 * haven't wrapped every method provided by FormEntryController, only the ones we've needed so far.
 * Feel free to add more as necessary.
 *
 * @author carlhartung
 */
public class FormController {

    private static final String t = "FormController";
    private File mMediaFolder;
    private File mInstancePath;
    private FormEntryController mFormEntryController;
    private FormIndex mIndexWaitingForData = null;
    private String mItemsetHash = null;

    public static final boolean STEP_INTO_GROUP = true;
    public static final boolean STEP_OVER_GROUP = false;

    /**
     * OpenRosa metadata tag names.
     */
    private static final String INSTANCE_ID = "instanceID";
    private static final String INSTANCE_NAME = "instanceName";

    /**
     * OpenRosa metadata of a form instance.
     *
     * Contains the values for the required metadata
     * fields and nothing else.
     *
     * @author mitchellsundt@gmail.com
     *
     */
    public static final class InstanceMetadata {
        public final String instanceId;
        public final String instanceName;

        InstanceMetadata( String instanceId, String instanceName ) {
            this.instanceId = instanceId;
            this.instanceName = instanceName;
        }
    };


    public FormController(File mediaFolder, FormEntryController fec, File instancePath) {
    	mMediaFolder = mediaFolder;
        mFormEntryController = fec;
        mInstancePath = instancePath;
    }

    public FormDef getFormDef() {
        return mFormEntryController.getModel().getForm();
    }

    public void setItemsetHash(String hash) {
        mItemsetHash = hash;
    }

    public String getItemsetHash() {
    	return mItemsetHash;
    }

    public File getMediaFolder() {
    	return mMediaFolder;
    }

    public File getInstancePath() {
    	return mInstancePath;
    }

    public void setInstancePath(File instancePath) {
    	mInstancePath = instancePath;
    }

    public void setIndexWaitingForData(FormIndex index) {
    	mIndexWaitingForData = index;
    }

    public FormIndex getIndexWaitingForData() {
    	return mIndexWaitingForData;
    }

    /**
     * For logging purposes...
     *
     * @param index
     * @return xpath value for this index
     */
    public String getXPath(FormIndex index) {
    	String value;
    	switch ( getEvent() ) {
    	case FormEntryController.EVENT_BEGINNING_OF_FORM:
    		value = "beginningOfForm";
    		break;
    	case FormEntryController.EVENT_END_OF_FORM:
    		value = "endOfForm";
    		break;
    	case FormEntryController.EVENT_GROUP:
    		value = "group." + index.getReference().toString();
    		break;
    	case FormEntryController.EVENT_QUESTION:
    		value = "question." + index.getReference().toString();
    		break;
    	case FormEntryController.EVENT_PROMPT_NEW_REPEAT:
    		value = "promptNewRepeat." + index.getReference().toString();
    		break;
    	case FormEntryController.EVENT_REPEAT:
    		value = "repeat." + index.getReference().toString();
    		break;
    	case FormEntryController.EVENT_REPEAT_JUNCTURE:
    		value = "repeatJuncture." + index.getReference().toString();
    		break;
		default:
			value = "unexpected";
    		break;
    	}
    	return value;
    }

    public FormIndex getIndexFromXPath(String xPath) {
    	if ( xPath.equals("beginningOfForm") ) {
            return FormIndex.createBeginningOfFormIndex();
    	} else if ( xPath.equals("endOfForm") ) {
    		return FormIndex.createEndOfFormIndex();
    	} else if ( xPath.equals("unexpected") ) {
    		Log.e(t, "Unexpected string from XPath");
    		throw new IllegalArgumentException("unexpected string from XPath");
    	} else {
    		FormIndex returned = null;
			FormIndex saved = getFormIndex();
			// the only way I know how to do this is to step through the entire form
			// until the XPath of a form entry matches that of the supplied XPath
			try {
				jumpToIndex(FormIndex.createBeginningOfFormIndex());
				int event = stepToNextEvent(true);
				while ( event != FormEntryController.EVENT_END_OF_FORM ) {
					String candidateXPath = getXPath(getFormIndex());
					// Log.i(t, "xpath: " + candidateXPath);
					if ( candidateXPath.equals(xPath) ) {
						returned = getFormIndex();
						break;
					}
					event = stepToNextEvent(true);
				}
			} finally {
				jumpToIndex(saved);
			}
			return returned;
    	}
    }

    /**
     * returns the event for the current FormIndex.
     *
     * @return
     */
    public int getEvent() {
        return mFormEntryController.getModel().getEvent();
    }


    /**
     * returns the event for the given FormIndex.
     *
     * @param index
     * @return
     */
    public int getEvent(FormIndex index) {
        return mFormEntryController.getModel().getEvent(index);
    }


    /**
     * @return current FormIndex.
     */
    public FormIndex getFormIndex() {
        return mFormEntryController.getModel().getFormIndex();
    }


    /**
     * Return the langauges supported by the currently loaded form.
     *
     * @return Array of Strings containing the languages embedded in the XForm.
     */
    public String[] getLanguages() {
        return mFormEntryController.getModel().getLanguages();
    }


    /**
     * @return A String containing the title of the current form.
     */
    public String getFormTitle() {
        return mFormEntryController.getModel().getFormTitle();
    }


    /**
     * @return the currently selected language.
     */
    public String getLanguage() {
        return mFormEntryController.getModel().getLanguage();
    }

    public String getBindAttribute( String attributeNamespace, String attributeName) {
    	return getBindAttribute( getFormIndex(), attributeNamespace, attributeName );
    }

    public String getBindAttribute(FormIndex idx, String attributeNamespace, String attributeName) {
        return mFormEntryController.getModel().getForm().getMainInstance().resolveReference(
                idx.getReference()).getBindAttributeValue(attributeNamespace, attributeName);
    }

    /**
     * @return an array of FormEntryCaptions for the current FormIndex. This is how we get group
     *         information Group 1 > Group 2> etc... The element at [size-1] is the current question
     *         text, with group names decreasing in hierarchy until array element at [0] is the root
     */
    private FormEntryCaption[] getCaptionHierarchy() {
        return mFormEntryController.getModel().getCaptionHierarchy();
    }

    /**
     * @param index
     * @return an array of FormEntryCaptions for the supplied FormIndex. This is how we get group
     *         information Group 1 > Group 2> etc... The element at [size-1] is the current question
     *         text, with group names decreasing in hierarchy until array element at [0] is the root
     */
    private FormEntryCaption[] getCaptionHierarchy(FormIndex index) {
        return mFormEntryController.getModel().getCaptionHierarchy(index);
    }

    /**
     * Returns a caption prompt for the given index. This is used to create a multi-question per
     * screen view.
     *
     * @param index
     * @return
     */
    public FormEntryCaption getCaptionPrompt(FormIndex index) {
        return mFormEntryController.getModel().getCaptionPrompt(index);
    }


    /**
     * Return the caption for the current FormIndex. This is usually used for a repeat prompt.
     *
     * @return
     */
    public FormEntryCaption getCaptionPrompt() {
        return mFormEntryController.getModel().getCaptionPrompt();
    }


    /**
     * This fires off the jr:preload actions and events to save values like the
     * end time of a form.
     *
     * @return
     */
    public boolean postProcessInstance() {
        return mFormEntryController.getModel().getForm().postProcessInstance();
    }


    /**
     * TODO: We need a good description of what this does, exactly, and why.
     *
     * @return
     */
    private FormInstance getInstance() {
        return mFormEntryController.getModel().getForm().getInstance();
    }


    /**
     * A convenience method for determining if the current FormIndex is in a group that is/should be
     * displayed as a multi-question view. This is useful for returning from the formhierarchy view
     * to a selected index.
     *
     * @param index
     * @return
     */
    private boolean groupIsFieldList(FormIndex index) {
        // if this isn't a group, return right away
    	IFormElement element = mFormEntryController.getModel().getForm().getChild(index);
        if (!(element instanceof GroupDef)) {
            return false;
        }

        GroupDef gd = (GroupDef) element; // exceptions?
        return (ODKView.FIELD_LIST.equalsIgnoreCase(gd.getAppearanceAttr()));
    }

    private boolean repeatIsFieldList(FormIndex index) {
        // if this isn't a group, return right away
    	IFormElement element = mFormEntryController.getModel().getForm().getChild(index);
        if (!(element instanceof GroupDef)) {
            return false;
        }

        GroupDef gd = (GroupDef) element; // exceptions?
        return (ODKView.FIELD_LIST.equalsIgnoreCase(gd.getAppearanceAttr()));
    }

    /**
     * Tests if the FormIndex 'index' is located inside a group that is marked as a "field-list"
     *
     * @param index
     * @return true if index is in a "field-list". False otherwise.
     */
    private boolean indexIsInFieldList(FormIndex index) {
        int event = getEvent(index);
        if (event == FormEntryController.EVENT_QUESTION) {
            // caption[0..len-1]
            // caption[len-1] == the question itself
            // caption[len-2] == the first group it is contained in.
            FormEntryCaption[] captions = getCaptionHierarchy(index);
            if (captions.length < 2) {
                // no group
                return false;
            }
            FormEntryCaption grp = captions[captions.length - 2];
            return groupIsFieldList(grp.getIndex());
        } else if (event == FormEntryController.EVENT_GROUP) {
            return groupIsFieldList(index);
        } else if (event == FormEntryController.EVENT_REPEAT) {
        	return repeatIsFieldList(index);
        } else {
            // right now we only test Questions and Groups. Should we also handle
            // repeats?
            return false;
        }

    }

    public boolean currentPromptIsQuestion() {
        return (getEvent() == FormEntryController.EVENT_QUESTION
        		|| ((getEvent() == FormEntryController.EVENT_GROUP ||
        			 getEvent() == FormEntryController.EVENT_REPEAT)
        				&& indexIsInFieldList()));
    }

    /**
     * Tests if the current FormIndex is located inside a group that is marked as a "field-list"
     *
     * @return true if index is in a "field-list". False otherwise.
     */
    public boolean indexIsInFieldList() {
        return indexIsInFieldList(getFormIndex());
    }


    /**
     * Attempts to save answer at the current FormIndex into the data model.
     *
     * @param data
     * @return
     */
    private int answerQuestion(IAnswerData data) {
        return mFormEntryController.answerQuestion(data);
    }


    /**
     * Attempts to save answer into the given FormIndex into the data model.
     *
     * @param index
     * @param data
     * @return
     */
    public int answerQuestion(FormIndex index, IAnswerData data) throws JavaRosaException {
        try {
            return mFormEntryController.answerQuestion(index, data);
        } catch (Exception e) {
           throw new JavaRosaException(e);
        }
    }

    /**
     * Goes through the entire form to make sure all entered answers comply with their constraints.
     * Constraints are ignored on 'jump to', so answers can be outside of constraints. We don't
     * allow saving to disk, though, until all answers conform to their constraints/requirements.
     *
     *
     * @param markCompleted
     * @return ANSWER_OK and leave index unchanged or change index to bad value and return error type.
     */
    public int validateAnswers(Boolean markCompleted) {
        FormEntryController formEntryController = this.mFormEntryController;
        FormEntryModel formEntryModel = formEntryController.getModel();

        FormEntryModel formEntryModelToBeValidated = new FormEntryModel(formEntryModel.getForm());
        FormEntryController formEntryControllerToBeValidated = new FormEntryController(formEntryModelToBeValidated);
        FormController formControllerToBeValidated = new FormController(this.getMediaFolder(), formEntryControllerToBeValidated, this.getInstancePath());

        formControllerToBeValidated.jumpToIndex(FormIndex.createBeginningOfFormIndex());

        int event;
        while ((event =
                formControllerToBeValidated.stepToNextEvent(FormController.STEP_INTO_GROUP)) != FormEntryController.EVENT_END_OF_FORM) {
            if (event != FormEntryController.EVENT_QUESTION) {
                continue;
            } else {
                FormIndex formControllerToBeValidatedFormIndex = formControllerToBeValidated.getFormIndex();

                int saveStatus = formControllerToBeValidated.answerQuestion(formControllerToBeValidated.getQuestionPrompt().getAnswerValue());
                if (markCompleted && saveStatus != FormEntryController.ANSWER_OK) {
                    // jump to the error
                    this.jumpToIndex(formControllerToBeValidatedFormIndex);
                    return saveStatus;
                }
            }
        }

        return FormEntryController.ANSWER_OK;
    }


    /**
     * saveAnswer attempts to save the current answer into the data model without doing any
     * constraint checking. Only use this if you know what you're doing. For normal form filling you
     * should always use answerQuestion or answerCurrentQuestion.
     *
     * @param index
     * @param data
     * @return true if saved successfully, false otherwise.
     */
    public boolean saveAnswer(FormIndex index, IAnswerData data) throws JavaRosaException {
        try {
            return mFormEntryController.saveAnswer(index, data);
        } catch (Exception e) {
            throw new JavaRosaException(e);
        }
    }


    /**
     * saveAnswer attempts to save the current answer into the data model without doing any
     * constraint checking. Only use this if you know what you're doing. For normal form filling you
     * should always use answerQuestion().
     *
     * @param data
     * @return true if saved successfully, false otherwise.
     */
    public boolean saveAnswer(IAnswerData data) throws JavaRosaException {
        try {
            return mFormEntryController.saveAnswer(data);
        } catch (Exception e) {
            throw new JavaRosaException(e);
        }
    }


    /**
     * Navigates forward in the form.
     *
     * @return the next event that should be handled by a view.
     */
    public int stepToNextEvent(boolean stepIntoGroup) {
        if ((getEvent() == FormEntryController.EVENT_GROUP ||
        	 getEvent() == FormEntryController.EVENT_REPEAT)
                && indexIsInFieldList() && !stepIntoGroup) {
            return stepOverGroup();
        } else {
            return mFormEntryController.stepToNextEvent();
        }
    }


    /**
     * If using a view like HierarchyView that doesn't support multi-question per screen, step over
     * the group represented by the FormIndex.
     *
     * @return
     */
    private int stepOverGroup() {
        ArrayList<FormIndex> indicies = new ArrayList<FormIndex>();
        GroupDef gd =
            (GroupDef) mFormEntryController.getModel().getForm()
                    .getChild(getFormIndex());
        FormIndex idxChild =
            mFormEntryController.getModel().incrementIndex(
                getFormIndex(), true); // descend into group
        for (int i = 0; i < gd.getChildren().size(); i++) {
            indicies.add(idxChild);
            // don't descend
            idxChild = mFormEntryController.getModel().incrementIndex(idxChild, false);
        }

        // jump to the end of the group
        mFormEntryController.jumpToIndex(indicies.get(indicies.size() - 1));
        return stepToNextEvent(STEP_OVER_GROUP);
    }

    /**
     * used to go up one level in the formIndex. That is, if you're at 5_0, 1 (the second question
     * in a repeating group), this method will return a FormInex of 5_0 (the start of the repeating
     * group). If your at index 16 or 5_0, this will return null;
     *
     * @param index
     * @return index
     */
    public FormIndex stepIndexOut(FormIndex index) {
        if (index.isTerminal()) {
            return null;
        } else {
            return new FormIndex(stepIndexOut(index.getNextLevel()), index);
        }
    }

    /**
     * Move the current form index to the index of the previous question in the form.
     * Step backward out of repeats and groups as needed. If the resulting question
     * is itself within a field-list, move upward to the group or repeat defining that
     * field-list.
     *
     * @return
     */
    public int stepToPreviousScreenEvent() throws JavaRosaException {
        try {
            if (getEvent() != FormEntryController.EVENT_BEGINNING_OF_FORM) {
                int event = stepToPreviousEvent();

                while (event == FormEntryController.EVENT_REPEAT_JUNCTURE ||
                       event == FormEntryController.EVENT_PROMPT_NEW_REPEAT ||
                       (event == FormEntryController.EVENT_QUESTION && indexIsInFieldList()) ||
                       ((event == FormEntryController.EVENT_GROUP
                         || event == FormEntryController.EVENT_REPEAT) && !indexIsInFieldList())) {
                    event = stepToPreviousEvent();
                }

                // Work-around for broken field-list handling from 1.1.7 which breaks either
                // build-generated forms or XLSForm-generated forms.  If the current group
                // is a GROUP with field-list and it is nested within a group or repeat with just
                // this containing group, and that is also a field-list, then return the parent group.
                if ( getEvent() == FormEntryController.EVENT_GROUP ) {
                    FormIndex currentIndex = getFormIndex();
                    IFormElement element = mFormEntryController.getModel().getForm().getChild(currentIndex);
                    if (element instanceof GroupDef) {
                        GroupDef gd = (GroupDef) element;
                        if ( ODKView.FIELD_LIST.equalsIgnoreCase(gd.getAppearanceAttr()) ) {
                            // OK this group is a field-list... see what the parent is...
                            FormEntryCaption[] fclist = this.getCaptionHierarchy(currentIndex);
                            if ( fclist.length > 1) {
                                FormEntryCaption fc = fclist[fclist.length-2];
                                GroupDef pd = (GroupDef) fc.getFormElement();
                                if ( pd.getChildren().size() == 1 &&
                                     ODKView.FIELD_LIST.equalsIgnoreCase(pd.getAppearanceAttr()) ) {
                                    mFormEntryController.jumpToIndex(fc.getIndex());
                                }
                            }
                        }
                    }
                }
            }
            return getEvent();
        } catch (RuntimeException e) {
            throw new JavaRosaException(e);
        }
    }

    /**
     * Move the current form index to the index of the next question in the form.
     * Stop if we should ask to create a new repeat group or if we reach the end of the form.
     * If we enter a group or repeat, return that if it is a field-list definition.
     * Otherwise, descend into the group or repeat searching for the first question.
     *
     * @return
     */
    public int stepToNextScreenEvent() throws JavaRosaException {
        try {
            if (getEvent() != FormEntryController.EVENT_END_OF_FORM) {
                int event;
                group_skip: do {
                    event = stepToNextEvent(FormController.STEP_OVER_GROUP);
                    switch (event) {
                        case FormEntryController.EVENT_QUESTION:
                        case FormEntryController.EVENT_END_OF_FORM:
                            break group_skip;
                        case FormEntryController.EVENT_PROMPT_NEW_REPEAT:
                            break group_skip;
                        case FormEntryController.EVENT_GROUP:
                        case FormEntryController.EVENT_REPEAT:
                            if (indexIsInFieldList()
                                    && getQuestionPrompts().length != 0) {
                                break group_skip;
                            }
                            // otherwise it's not a field-list group, so just skip it
                            break;
                        case FormEntryController.EVENT_REPEAT_JUNCTURE:
                            Log.i(t, "repeat juncture: "
                                    + getFormIndex().getReference());
                            // skip repeat junctures until we implement them
                            break;
                        default:
                            Log.w(t,
                                "JavaRosa added a new EVENT type and didn't tell us... shame on them.");
                            break;
                    }
                } while (event != FormEntryController.EVENT_END_OF_FORM);
            }
            return getEvent();
        } catch (RuntimeException e) {
            throw new JavaRosaException(e);
        }
    }


    /**
     * Move the current form index to the index of the first enclosing repeat
     * or to the start of the form.
     *
     * @return
     */
    public int stepToOuterScreenEvent() {
        FormIndex index = stepIndexOut(getFormIndex());
        int currentEvent = getEvent();

        // Step out of any group indexes that are present.
        while (index != null
                && getEvent(index) == FormEntryController.EVENT_GROUP) {
            index = stepIndexOut(index);
        }

        if (index == null) {
            jumpToIndex(FormIndex.createBeginningOfFormIndex());
        } else {
            if (currentEvent == FormEntryController.EVENT_REPEAT) {
                // We were at a repeat, so stepping back brought us to then previous level
                jumpToIndex(index);
            } else {
                // We were at a question, so stepping back brought us to either:
                // The beginning. or The start of a repeat. So we need to step
                // out again to go passed the repeat.
                index = stepIndexOut(index);
                if (index == null) {
                    jumpToIndex(FormIndex.createBeginningOfFormIndex());
                } else {
                    jumpToIndex(index);
                }
            }
        }
        return getEvent();
    }


    public static class FailedConstraint {
    	public final FormIndex index;
    	public final int status;

    	FailedConstraint(FormIndex index, int status) {
    		this.index = index;
    		this.status = status;
    	}
    }
    /**
     *
     * @param answers
     * @param evaluateConstraints
     * @return FailedConstraint of first failed constraint or null if all questions were saved.
     */
    public FailedConstraint saveAllScreenAnswers(LinkedHashMap<FormIndex,IAnswerData> answers, boolean evaluateConstraints) throws JavaRosaException {
    	if (currentPromptIsQuestion()) {
            Iterator<FormIndex> it = answers.keySet().iterator();
            while (it.hasNext()) {
                FormIndex index = it.next();
                // Within a group, you can only save for question events
                if (getEvent(index) == FormEntryController.EVENT_QUESTION) {
                	int saveStatus;
                	IAnswerData answer = answers.get(index);
                	if (evaluateConstraints) {
                		saveStatus = answerQuestion(index, answer);
                        if (saveStatus != FormEntryController.ANSWER_OK) {
                            return new FailedConstraint(index, saveStatus);
                        }
                    } else {
                        saveAnswer(index, answer);
                    }
                } else {
                    Log.w(t,
                        "Attempted to save an index referencing something other than a question: "
                                + index.getReference());
                }
            }
        }
    	return null;
    }


    /**
     * Navigates backward in the form.
     *
     * @return the event that should be handled by a view.
     */
    public int stepToPreviousEvent() {
        /*
         * Right now this will always skip to the beginning of a group if that group is represented
         * as a 'field-list'. Should a need ever arise to step backwards by only one step in a
         * 'field-list', this method will have to be updated.
         */

        mFormEntryController.stepToPreviousEvent();

        // If after we've stepped, we're in a field-list, jump back to the beginning of the group
        //

        if (indexIsInFieldList()
                && getEvent() == FormEntryController.EVENT_QUESTION) {
            // caption[0..len-1]
            // caption[len-1] == the question itself
            // caption[len-2] == the first group it is contained in.
            FormEntryCaption[] captions = getCaptionHierarchy();
            FormEntryCaption grp = captions[captions.length - 2];
            int event = mFormEntryController.jumpToIndex(grp.getIndex());
            // and test if this group or at least one of its children is relevant...
            FormIndex idx = grp.getIndex();
            if ( !mFormEntryController.getModel().isIndexRelevant(idx) ) {
            	return stepToPreviousEvent();
            }
            idx = mFormEntryController.getModel().incrementIndex(idx, true);
            while ( FormIndex.isSubElement(grp.getIndex(), idx) ) {
            	if ( mFormEntryController.getModel().isIndexRelevant(idx) ) {
            		return event;
            	}
                idx = mFormEntryController.getModel().incrementIndex(idx, true);
            }
            return stepToPreviousEvent();
        } else if ( indexIsInFieldList() && getEvent() == FormEntryController.EVENT_GROUP) {
            FormIndex grpidx = mFormEntryController.getModel().getFormIndex();
            int event = mFormEntryController.getModel().getEvent();
            // and test if this group or at least one of its children is relevant...
            if ( !mFormEntryController.getModel().isIndexRelevant(grpidx) ) {
            	return stepToPreviousEvent(); // shouldn't happen?
            }
            FormIndex idx = mFormEntryController.getModel().incrementIndex(grpidx, true);
            while ( FormIndex.isSubElement(grpidx, idx) ) {
            	if ( mFormEntryController.getModel().isIndexRelevant(idx) ) {
            		return event;
            	}
                idx = mFormEntryController.getModel().incrementIndex(idx, true);
            }
            return stepToPreviousEvent();
        }

        return getEvent();

    }


    /**
     * Jumps to a given FormIndex.
     *
     * @param index
     * @return EVENT for the specified Index.
     */
    public int jumpToIndex(FormIndex index) {
        return mFormEntryController.jumpToIndex(index);
    }


    /**
     * Creates a new repeated instance of the group referenced by the current FormIndex.
     *
     * @param questionIndex
     */
    public void newRepeat() {
        mFormEntryController.newRepeat();
    }


    /**
     * If the current FormIndex is within a repeated group, will find the innermost repeat, delete
     * it, and jump the FormEntryController to the previous valid index. That is, if you have group1
     * (2) > group2 (3) and you call deleteRepeat, it will delete the 3rd instance of group2.
     */
    public void deleteRepeat() {
        FormIndex fi = mFormEntryController.deleteRepeat();
        mFormEntryController.jumpToIndex(fi);
    }


    /**
     * Sets the current language.
     *
     * @param language
     */
    public void setLanguage(String language) {
        mFormEntryController.setLanguage(language);
    }


    /**
     * Returns an array of question promps.
     *
     * @return
     */
    public FormEntryPrompt[] getQuestionPrompts() throws RuntimeException {

        ArrayList<FormIndex> indicies = new ArrayList<FormIndex>();
        FormIndex currentIndex = getFormIndex();

        // For questions, there is only one.
        // For groups, there could be many, but we set that below
        FormEntryPrompt[] questions = new FormEntryPrompt[1];

    	IFormElement element = mFormEntryController.getModel().getForm().getChild(currentIndex);
        if (element instanceof GroupDef) {
            GroupDef gd = (GroupDef) element;
            // descend into group
            FormIndex idxChild = mFormEntryController.getModel().incrementIndex(currentIndex, true);

            if ( gd.getChildren().size() == 1 && getEvent(idxChild) == FormEntryController.EVENT_GROUP ) {
            	// if we have a group definition within a field-list attribute group, and this is the
            	// only child in the group, check to see if it is also a field-list appearance.
            	// If it is, then silently recurse into it to pick up its elements.
            	// Work-around for the inconsistent treatment of field-list groups and repeats in 1.1.7 that
            	// either breaks forms generated by build or breaks forms generated by XLSForm.
            	IFormElement nestedElement = mFormEntryController.getModel().getForm().getChild(idxChild);
                if (nestedElement instanceof GroupDef) {
                    GroupDef nestedGd = (GroupDef) nestedElement;
                    if ( ODKView.FIELD_LIST.equalsIgnoreCase(nestedGd.getAppearanceAttr()) ) {
                    	gd = nestedGd;
                    	idxChild = mFormEntryController.getModel().incrementIndex(idxChild, true);
                    }
                }
            }

            for (int i = 0; i < gd.getChildren().size(); i++) {
                indicies.add(idxChild);
                // don't descend
                idxChild = mFormEntryController.getModel().incrementIndex(idxChild, false);
            }

            // we only display relevant questions
            ArrayList<FormEntryPrompt> questionList = new ArrayList<FormEntryPrompt>();
            for (int i = 0; i < indicies.size(); i++) {
                FormIndex index = indicies.get(i);

                if (getEvent(index) != FormEntryController.EVENT_QUESTION) {
                    String errorMsg =
                        "Only questions are allowed in 'field-list'.  Bad node is: "
                                + index.getReference().toString(false);
                    RuntimeException e = new RuntimeException(errorMsg);
                    Log.e(t, errorMsg);
                    throw e;
                }

                // we only display relevant questions
                if (mFormEntryController.getModel().isIndexRelevant(index)) {
                    questionList.add(getQuestionPrompt(index));
                }
                questions = new FormEntryPrompt[questionList.size()];
                questionList.toArray(questions);
            }
        } else {
            // We have a quesion, so just get the one prompt
            questions[0] = getQuestionPrompt();
        }

        return questions;
    }


    public FormEntryPrompt getQuestionPrompt(FormIndex index) {
        return mFormEntryController.getModel().getQuestionPrompt(index);
    }


    public FormEntryPrompt getQuestionPrompt() {
        return mFormEntryController.getModel().getQuestionPrompt();
    }

    public String getQuestionPromptConstraintText(FormIndex index) {
    	return mFormEntryController.getModel().getQuestionPrompt(index).getConstraintText();
    }

    public String getQuestionPromptRequiredText(FormIndex index) {
    	// look for the text under the requiredMsg bind attribute
		String constraintText = getBindAttribute(index, XFormParser.NAMESPACE_JAVAROSA, "requiredMsg");
		if (constraintText != null) {
	    	XPathExpression xPathRequiredMsg;
			try {
				xPathRequiredMsg = XPathParseTool.parseXPath("string(" + constraintText + ")");
			} catch(Exception e) {
				// Expected in probably most cases.
                // This is a string literal, so no need to evaluate anything.
                return constraintText;
			}

			if(xPathRequiredMsg != null) {
				try{
					FormDef form = mFormEntryController.getModel().getForm();
					TreeElement mTreeElement = form.getMainInstance().resolveReference(index.getReference());
					EvaluationContext ec = new EvaluationContext(form.exprEvalContext, mTreeElement.getRef());
					Object value = xPathRequiredMsg.eval(form.getMainInstance(), ec);
					if(value != "") {
						return (String)value;
					}
					return null;
				} catch(Exception e) {
					Log.e(t,"Error evaluating a valid-looking required xpath ", e);
					return constraintText;
				}
			} else {
				return constraintText;
			}
		}
		return null;
    }

    /**
     * Returns an array of FormEntryCaptions for current FormIndex.
     *
     * @return
     */
    public FormEntryCaption[] getGroupsForCurrentIndex() {
        // return an empty array if you ask for something impossible
        if (!(getEvent() == FormEntryController.EVENT_QUESTION
                || getEvent() == FormEntryController.EVENT_PROMPT_NEW_REPEAT
                || getEvent() == FormEntryController.EVENT_GROUP
                || getEvent() == FormEntryController.EVENT_REPEAT)) {
            return new FormEntryCaption[0];
        }

        // the first caption is the question, so we skip it if it's an EVENT_QUESTION
        // otherwise, the first caption is a group so we start at index 0
        int lastquestion = 1;
        if (getEvent() == FormEntryController.EVENT_PROMPT_NEW_REPEAT
                || getEvent() == FormEntryController.EVENT_GROUP
                || getEvent() == FormEntryController.EVENT_REPEAT) {
            lastquestion = 0;
        }

        FormEntryCaption[] v = getCaptionHierarchy();
        FormEntryCaption[] groups = new FormEntryCaption[v.length - lastquestion];
        for (int i = 0; i < v.length - lastquestion; i++) {
            groups[i] = v[i];
        }
        return groups;
    }


    /**
     * This is used to enable/disable the "Delete Repeat" menu option.
     *
     * @return
     */
    public boolean indexContainsRepeatableGroup() {
        FormEntryCaption[] groups = getCaptionHierarchy();
        if (groups.length == 0) {
            return false;
        }
        for (int i = 0; i < groups.length; i++) {
            if (groups[i].repeats())
                return true;
        }
        return false;
    }


    /**
     * The count of the closest group that repeats or -1.
     */
    public int getLastRepeatedGroupRepeatCount() {
        FormEntryCaption[] groups = getCaptionHierarchy();
        if (groups.length > 0) {
            for (int i = groups.length - 1; i > -1; i--) {
                if (groups[i].repeats()) {
                    return groups[i].getMultiplicity();

                }
            }
        }
        return -1;
    }


    /**
     * The name of the closest group that repeats or null.
     */
    public String getLastRepeatedGroupName() {
        FormEntryCaption[] groups = getCaptionHierarchy();
        // no change
        if (groups.length > 0) {
            for (int i = groups.length - 1; i > -1; i--) {
                if (groups[i].repeats()) {
                    return groups[i].getLongText();
                }
            }
        }
        return null;
    }


    /**
     * The closest group the prompt belongs to.
     *
     * @return FormEntryCaption
     */
    private FormEntryCaption getLastGroup() {
        FormEntryCaption[] groups = getCaptionHierarchy();
        if (groups == null || groups.length == 0)
            return null;
        else
            return groups[groups.length - 1];
    }


    /**
     * The repeat count of closest group the prompt belongs to.
     */
    public int getLastRepeatCount() {
        if (getLastGroup() != null) {
            return getLastGroup().getMultiplicity();
        }
        return -1;

    }


    /**
     * The text of closest group the prompt belongs to.
     */
    public String getLastGroupText() {
        if (getLastGroup() != null) {
            return getLastGroup().getLongText();
        }
        return null;
    }

    /**
     * Find the portion of the form that is to be submitted
     *
     * @return
     */
    private IDataReference getSubmissionDataReference() {
        FormDef formDef = mFormEntryController.getModel().getForm();
        // Determine the information about the submission...
        SubmissionProfile p = formDef.getSubmissionProfile();
        if (p == null || p.getRef() == null) {
            return new XPathReference("/");
        } else {
            return p.getRef();
        }
    }

    /**
     * Once a submission is marked as complete, it is saved in the
     * submission format, which might be a fragment of the original
     * form or might be a SMS text string, etc.
     *
     * @return true if the submission is the entire form.  If it is,
     *              then the submission can be re-opened for editing
     *              after it was marked-as-complete (provided it has
     *              not been encrypted).
     */
    public boolean isSubmissionEntireForm() {
        IDataReference sub = getSubmissionDataReference();
        return ( getInstance().resolveReference(sub) == null );
    }

    /**
     * Constructs the XML payload for a filled-in form instance. This payload
     * enables a filled-in form to be re-opened and edited.
     *
     * @return
     * @throws IOException
     */
    public ByteArrayPayload getFilledInFormXml() throws IOException {
        // assume no binary data inside the model.
        FormInstance datamodel = getInstance();
        XFormSerializingVisitor serializer = new XFormSerializingVisitor();
        ByteArrayPayload payload =
        		(ByteArrayPayload) serializer.createSerializedPayload(datamodel);

        return payload;
    }

    /**
     * Extract the portion of the form that should be uploaded to the server.
     *
     * @return
     * @throws IOException
     */
    public ByteArrayPayload getSubmissionXml() throws IOException {
        FormInstance instance = getInstance();
        XFormSerializingVisitor serializer = new XFormSerializingVisitor();
        ByteArrayPayload payload =
                (ByteArrayPayload) serializer.createSerializedPayload(instance,
                                                   getSubmissionDataReference());
        return payload;
    }

    /**
     * Traverse the submission looking for the first matching tag in depth-first order.
     *
     * @param parent
     * @param name
     * @return
     */
    private TreeElement findDepthFirst(TreeElement parent, String name) {
        int len = parent.getNumChildren();
        for ( int i = 0; i < len ; ++i ) {
            TreeElement e = parent.getChildAt(i);
            if ( name.equals(e.getName()) ) {
                return e;
            } else if ( e.getNumChildren() != 0 ) {
                TreeElement v = findDepthFirst(e, name);
                if ( v != null ) return v;
            }
        }
        return null;
    }

    /**
     * Get the OpenRosa required metadata of the portion of the form beng submitted
     * @return
     */
    public InstanceMetadata getSubmissionMetadata() {
        FormDef formDef = mFormEntryController.getModel().getForm();
        TreeElement rootElement = formDef.getInstance().getRoot();

        TreeElement trueSubmissionElement;
        // Determine the information about the submission...
        SubmissionProfile p = formDef.getSubmissionProfile();
        if ( p == null || p.getRef() == null ) {
            trueSubmissionElement = rootElement;
        } else {
            IDataReference ref = p.getRef();
            trueSubmissionElement = formDef.getInstance().resolveReference(ref);
            // resolveReference returns null if the reference is to the root element...
            if ( trueSubmissionElement == null ) {
                trueSubmissionElement = rootElement;
            }
        }

        // and find the depth-first meta block in this...
        TreeElement e = findDepthFirst(trueSubmissionElement, "meta");

        String instanceId = null;
        String instanceName = null;

        if ( e != null ) {
            Vector<TreeElement> v;

            // instance id...
            v = e.getChildrenWithName(INSTANCE_ID);
            if ( v.size() == 1 ) {
                StringData sa = (StringData) v.get(0).getValue();
                if ( sa != null ) {
                	instanceId = (String) sa.getValue();
                }
            }

            // instance name...
            v = e.getChildrenWithName(INSTANCE_NAME);
            if ( v.size() == 1 ) {
                StringData sa = (StringData) v.get(0).getValue();
                if ( sa != null ) {
                    instanceName = (String) sa.getValue();
                }
            }
        }

        return new InstanceMetadata(instanceId,instanceName);
    }

}
