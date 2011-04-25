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

import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.GroupDef;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.form.api.FormEntryCaption;
import org.javarosa.form.api.FormEntryController;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.views.ODKView;

import android.util.Log;

import java.util.ArrayList;

/**
 * This class is a wrapper for Javarosa's FormEntryController. In theory, if you wanted to replace
 * javarosa as the form engine, you should only need to replace the methods in this file. Also, we
 * haven't wrapped every method provided by FormEntryController, only the ones we've needed so far.
 * Feel free to add more as necessary.
 * 
 * @author carlhartung
 */
public class FormController {

    private FormEntryController mFormEntryController;

    public static final boolean STEP_INTO_GROUP = true;
    public static final boolean STEP_OVER_GROUP = false;


    public FormController(FormEntryController fec) {
        mFormEntryController = fec;
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
     * @return true if current FormIndex is readonly. false otherwise.
     */
    public boolean isIndexReadonly() {
        return mFormEntryController.getModel().isIndexReadonly();
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
     * @return the FormEntryPrompt for the current FormIndex.
     */
    public FormEntryPrompt getQuestionPrompt() {
        return mFormEntryController.getModel().getQuestionPrompt();
    }


    /**
     * @return A String containing the title of rht ecurrent form.
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


    /**
     * @return an array of FormEntryCaptions for the current FormIndex. This is how we get group
     *         information Group 1 > Group 2> etc... The element at [size-1] is the current question
     *         text, with group names decreasing in hierarchy until array element at [0] is the root
     */
    public FormEntryCaption[] getCaptionHierarchy() {
        return mFormEntryController.getModel().getCaptionHierarchy();
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
     * TODO: We need a good description of what this does, exactly, and why.
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
    public FormInstance getInstance() {
        return mFormEntryController.getModel().getForm().getInstance();
    }


    /**
     * A convenience method for determining if the current FormIndex is in a group that is/should be
     * displayed as a multi-question view. This is useful for returning from the formhierarchy view
     * to a selected index.
     * 
     * @return true if current index should be displayed as part of a formentry group. false
     *         otherwise.
     */
    public boolean indexIsInFieldList() {
        return indexIsInFieldList(mFormEntryController.getModel().getFormIndex());
    }


    /**
     * A convenience method for determining if the current FormIndex is in a group that is/should be
     * displayed as a multi-question view. This is useful for returning from the formhierarchy view
     * to a selected index.
     * 
     * @param index
     * @return
     */
    private boolean indexIsInFieldList(FormIndex index) {
        // if this isn't a group, return right away
        if (!(mFormEntryController.getModel().getForm().getChild(index) instanceof GroupDef)) {
            return false;
        }

        GroupDef gd = (GroupDef) mFormEntryController.getModel().getForm().getChild(index); // exceptions?
        return (ODKView.FIELD_LIST.equalsIgnoreCase(gd.getAppearanceAttr()));
    }


    /**
     * Jumps the FormEntryController to the beginning of a group that should be displayed as a
     * multi-question per screen view.
     * 
     * @return the event representing the GROUP_EVENT for the beginning of this mutli-question view,
     *         or the current event if the current FormIndex isn't in a multi-question view. TODO:
     *         Does this need to be public? doesn't need to be public, but needs to get called by
     *         refresh view. I'd like to test this before changing
     */
    public int jumpToBeginningOfGroupIfIsFieldList() {
        int event = mFormEntryController.getModel().getEvent();
        if (event == FormEntryController.EVENT_QUESTION) {
            // caption[0..len-1]
            // caption[len-1] == the question itself
            // caption[len-2] == the first group it is contained in.
            FormEntryCaption[] captions = mFormEntryController.getModel().getCaptionHierarchy();
            if (captions.length < 2) {
                Log.e("carl", "not a group");
                return event;
            }
            FormEntryCaption grp = captions[captions.length - 2];
            if (indexIsInFieldList(grp.getIndex())) {
                return mFormEntryController.jumpToIndex(grp.getIndex());
            }
        }

        return event;
    }


    /**
     * Attempts to save answer at the current FormIndex into the data model.
     * 
     * @param data
     * @return
     */
    public int answerQuestion(IAnswerData data) {
        return mFormEntryController.answerQuestion(data);
    }


    /**
     * Attempts to save answer into the given FormIndex into the data model.
     * 
     * @param index
     * @param data
     * @return
     */
    public int answerQuestion(FormIndex index, IAnswerData data) {
        return mFormEntryController.answerQuestion(index, data);
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
    public boolean saveAnswer(FormIndex index, IAnswerData data) {
        return mFormEntryController.saveAnswer(index, data);
    }


    /**
     * saveAnswer attempts to save the current answer into the data model without doing any
     * constraint checking. Only use this if you know what you're doing. For normal form filling you
     * should always use answerQuestion().
     * 
     * @param index
     * @param data
     * @return true if saved successfully, false otherwise.
     */
    public boolean saveAnswer(IAnswerData data) {
        return mFormEntryController.saveAnswer(data);
    }


    /**
     * Navigates forward in the form.
     * 
     * @return the next event that should be handled by a view.
     */
    public int stepToNextEvent(boolean stepOverGroup) {
        if (mFormEntryController.getModel().getEvent() == FormEntryController.EVENT_GROUP
                && indexIsInFieldList() && stepOverGroup) {
            return stepOverGroup();
        } else {
            return mFormEntryController.stepToNextEvent();
        }
    }


    /**
     * if using a view like HierarchyView that doesn't support multi-question per screen, step over
     * the group represented by the FormIndex.
     * 
     * @return
     */
    public int stepOverGroup() {
        ArrayList<FormIndex> indicies = new ArrayList<FormIndex>();
        GroupDef gd =
            (GroupDef) mFormEntryController.getModel().getForm()
                    .getChild(mFormEntryController.getModel().getFormIndex());
        FormIndex idxChild =
            mFormEntryController.getModel().incrementIndex(
                mFormEntryController.getModel().getFormIndex(), true); // descend into group
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
        return jumpToBeginningOfGroupIfIsFieldList();
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
     * Creates a new repeated instance of the group referenced by the specified FormIndex.
     * 
     * @param questionIndex
     */
    public void newRepeat(FormIndex questionIndex) {
        mFormEntryController.newRepeat(questionIndex);
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
        /*
         * TODO: No idea what happens when you try to call this on a FormIndex not in a repeat.
         * Should probably throw an error.
         */
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
     * TODO:  Can I call this with just one?
     * 
     * @return
     */
    public FormEntryPrompt[] getQuestionPrompts() {

        ArrayList<FormIndex> indicies = new ArrayList<FormIndex>();
        FormIndex currentIndex = mFormEntryController.getModel().getFormIndex();

        GroupDef gd = (GroupDef) mFormEntryController.getModel().getForm().getChild(currentIndex);
        FormIndex idxChild = mFormEntryController.getModel().incrementIndex(currentIndex, true); // descend                                                                                                 // into
                                                                                                 // group
        //TODO:  just use size here.
        for (Object child : gd.getChildren()) {
            indicies.add(idxChild);
            idxChild = mFormEntryController.getModel().incrementIndex(idxChild, false); // don't
                                                                                        // descend
        }

        FormEntryPrompt[] questions = new FormEntryPrompt[indicies.size()];
        for (int i = 0; i < indicies.size(); i++) {
            FormIndex index = indicies.get(i);

            if (mFormEntryController.getModel().getEvent(index) != FormEntryController.EVENT_QUESTION) {
                RuntimeException e =
                    new RuntimeException(
                            "Only questions are allowed in 'field-list'.  Bad node is: "
                                    + index.getReference().toString(false));
                throw e;
            }

            FormEntryPrompt p = mFormEntryController.getModel().getQuestionPrompt(index);
            questions[i] = p;
        }

        return questions;
    }


    /**
     * Returns an array of FormEntryCaptions for current FormIndex.
     * 
     * @return
     */
    public FormEntryCaption[] getGroupsForCurrentIndex() {
        // return an empty array if you ask for something impossible
        if (!(mFormEntryController.getModel().getEvent() == FormEntryController.EVENT_QUESTION
                || mFormEntryController.getModel().getEvent() == FormEntryController.EVENT_PROMPT_NEW_REPEAT || mFormEntryController
                .getModel().getEvent() == FormEntryController.EVENT_GROUP)) {
            return new FormEntryCaption[0];
        }

        // the first caption is the question, so we skip it if it's an EVENT_QUESTION
        // otherwise, the first caption is a group so we start at index 0
        int lastquestion = 1;
        if (mFormEntryController.getModel().getEvent() == FormEntryController.EVENT_PROMPT_NEW_REPEAT
                || mFormEntryController.getModel().getEvent() == FormEntryController.EVENT_GROUP) {
            lastquestion = 0;
        }
        // start debugging
        FormEntryCaption[] v = mFormEntryController.getModel().getCaptionHierarchy();
        for (int a = 0; a < v.length; a++) {
            Log.e("carl", "group: " + v[a].getLongText());
        } // end debugging
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
        FormEntryCaption[] groups = mFormEntryController.getModel().getCaptionHierarchy();
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
        FormEntryCaption[] groups = mFormEntryController.getModel().getCaptionHierarchy();
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
        FormEntryCaption[] groups = mFormEntryController.getModel().getCaptionHierarchy();
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
        FormEntryCaption[] groups = mFormEntryController.getModel().getCaptionHierarchy();
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

}
