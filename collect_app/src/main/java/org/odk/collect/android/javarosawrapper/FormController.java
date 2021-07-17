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

package org.odk.collect.android.javarosawrapper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.GroupDef;
import org.javarosa.core.model.IDataReference;
import org.javarosa.core.model.IFormElement;
import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.SubmissionProfile;
import org.javarosa.core.model.ValidateOutcome;
import org.javarosa.core.model.actions.setgeopoint.SetGeopointActionHandler;
import org.javarosa.core.model.condition.EvaluationContext;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.core.model.instance.FormInstance;
import org.javarosa.core.model.instance.TreeElement;
import org.javarosa.core.model.instance.TreeReference;
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
import org.odk.collect.android.externaldata.ExternalDataUtil;
import org.odk.collect.android.formentry.audit.AsyncTaskAuditEventWriter;
import org.odk.collect.android.formentry.audit.AuditConfig;
import org.odk.collect.android.formentry.audit.AuditEventLogger;
import org.odk.collect.android.utilities.Appearances;
import org.odk.collect.android.utilities.FileUtils;
import org.odk.collect.android.utilities.FormNameUtils;
import org.odk.collect.shared.strings.Md5;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import timber.log.Timber;

import static org.odk.collect.android.javarosawrapper.FormIndexUtils.getPreviousLevel;
import static org.odk.collect.android.javarosawrapper.FormIndexUtils.getRepeatGroupIndex;
import static org.odk.collect.android.utilities.ApplicationConstants.Namespaces.XML_OPENDATAKIT_NAMESPACE;

/**
 * This class is a wrapper for Javarosa's FormEntryController. In theory, if you wanted to replace
 * javarosa as the form engine, you should only need to replace the methods in this file. Also, we
 * haven't wrapped every method provided by FormEntryController, only the ones we've needed so far.
 * Feel free to add more as necessary.
 *
 * @author carlhartung
 */
public class FormController {

    public static final boolean STEP_INTO_GROUP = true;
    public static final boolean STEP_OVER_GROUP = false;

    /**
     * OpenRosa metadata tag names.
     */
    public static final String INSTANCE_ID = "instanceID";
    private static final String INSTANCE_NAME = "instanceName";

    /*
     * Non OpenRosa metadata tag names
     */
    private static final String AUDIT = "audit";
    public static final String AUDIT_FILE_NAME = "audit.csv";

    /*
     * Store the auditEventLogger object with the form controller state
     */
    private AuditEventLogger auditEventLogger;

    /**
     * OpenRosa metadata of a form instance.
     * <p>
     * Contains the values for the required metadata
     * fields and nothing else.
     *
     * @author mitchellsundt@gmail.com
     */
    public static final class InstanceMetadata {
        public final String instanceId;
        public final String instanceName;
        public final AuditConfig auditConfig;

        public InstanceMetadata(String instanceId, String instanceName, AuditConfig auditConfig) {
            this.instanceId = instanceId;
            this.instanceName = FormNameUtils.normalizeFormName(instanceName, false);
            this.auditConfig = auditConfig;
        }
    }

    private final File mediaFolder;
    @Nullable
    private File instanceFile;
    private final FormEntryController formEntryController;
    private FormIndex indexWaitingForData;

    public FormController(File mediaFolder, FormEntryController fec, File instanceFile) {
        this.mediaFolder = mediaFolder;
        formEntryController = fec;
        this.instanceFile = instanceFile;
    }

    public FormDef getFormDef() {
        return formEntryController.getModel().getForm();
    }

    public File getMediaFolder() {
        return mediaFolder;
    }

    @Nullable
    public File getInstanceFile() {
        return instanceFile;
    }

    public void setInstanceFile(File instanceFile) {
        this.instanceFile = instanceFile;
    }

    @Nullable
    public String getAbsoluteInstancePath() {
        return instanceFile != null ? instanceFile.getAbsolutePath() : null;
    }

    @Nullable
    public String getLastSavedPath() {
        return mediaFolder != null ? FileUtils.getLastSavedPath(mediaFolder) : null;
    }

    public void setIndexWaitingForData(FormIndex index) {
        indexWaitingForData = index;
    }

    public FormIndex getIndexWaitingForData() {
        return indexWaitingForData;
    }

    public AuditEventLogger getAuditEventLogger() {
        if (auditEventLogger == null) {
            AuditConfig auditConfig = getSubmissionMetadata().auditConfig;

            if (auditConfig != null) {
                auditEventLogger = new AuditEventLogger(auditConfig, new AsyncTaskAuditEventWriter(new File(instanceFile.getParentFile().getPath() + File.separator + AUDIT_FILE_NAME), auditConfig.isLocationEnabled(), auditConfig.isTrackingChangesEnabled(), auditConfig.isIdentifyUserEnabled(), auditConfig.isTrackChangesReasonEnabled()), this);
            } else {
                auditEventLogger = new AuditEventLogger(null, null, this);
            }
        }

        return auditEventLogger;
    }

    /**
     * For logging purposes...
     *
     * @return xpath value for this index
     */
    public String getXPath(FormIndex index) {
        String value;
        switch (getEvent(index)) {
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

    @Nullable
    public FormIndex getIndexFromXPath(String xpath) {
        switch (xpath) {
            case "beginningOfForm":
                return FormIndex.createBeginningOfFormIndex();
            case "endOfForm":
                return FormIndex.createEndOfFormIndex();
            case "unexpected":
                Timber.e("Unexpected string from XPath");
                return null;
            default:
                FormIndex returned = null;
                FormIndex saved = getFormIndex();
                // the only way I know how to do this is to step through the entire form
                // until the XPath of a form entry matches that of the supplied XPath
                try {
                    jumpToIndex(FormIndex.createBeginningOfFormIndex());
                    int event = stepToNextEvent(true);
                    while (event != FormEntryController.EVENT_END_OF_FORM) {
                        String candidateXPath = getXPath(getFormIndex());
                        if (candidateXPath.equals(xpath)) {
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
     */
    public int getEvent() {
        return formEntryController.getModel().getEvent();
    }

    /**
     * returns the event for the given FormIndex.
     */
    public int getEvent(FormIndex index) {
        return formEntryController.getModel().getEvent(index);
    }

    /**
     * @return current FormIndex.
     */
    public FormIndex getFormIndex() {
        return formEntryController.getModel().getFormIndex();
    }

    /**
     * Return the langauges supported by the currently loaded form.
     *
     * @return Array of Strings containing the languages embedded in the XForm.
     */
    public String[] getLanguages() {
        return formEntryController.getModel().getLanguages();
    }

    /**
     * @return A String containing the title of the current form.
     */
    public String getFormTitle() {
        return formEntryController.getModel().getFormTitle();
    }

    /**
     * Gets a unique, privacy-preserving identifier for the current form.
     *
     * @return md5 hash of the form title, a space, the form ID
     */
    public String getCurrentFormIdentifierHash() {
        String formIdentifier = "";

        if (getFormDef() != null) {
            String formID = getFormDef().getMainInstance().getRoot().getAttributeValue("", "id");
            formIdentifier = getFormTitle() + " " + formID;
        }

        return Md5.getMd5Hash(new ByteArrayInputStream(formIdentifier.getBytes()));
    }

    /**
     * @return the currently selected language.
     */
    public String getLanguage() {
        return formEntryController.getModel().getLanguage();
    }

    public String getBindAttribute(String attributeNamespace, String attributeName) {
        return getBindAttribute(getFormIndex(), attributeNamespace, attributeName);
    }

    public String getBindAttribute(FormIndex idx, String attributeNamespace, String attributeName) {
        return formEntryController.getModel().getForm().getMainInstance().resolveReference(
                idx.getReference()).getBindAttributeValue(attributeNamespace, attributeName);
    }

    /**
     * @return an array of FormEntryCaptions for the current FormIndex. This is how we get group
     * information Group 1 > Group 2> etc... The element at [size-1] is the current question
     * text, with group names decreasing in hierarchy until array element at [0] is the root
     */
    private FormEntryCaption[] getCaptionHierarchy() {
        return formEntryController.getModel().getCaptionHierarchy();
    }

    /**
     * @return an array of FormEntryCaptions for the supplied FormIndex. This is how we get group
     * information Group 1 > Group 2> etc... The element at [size-1] is the current question
     * text, with group names decreasing in hierarchy until array element at [0] is the root
     */
    private FormEntryCaption[] getCaptionHierarchy(FormIndex index) {
        return formEntryController.getModel().getCaptionHierarchy(index);
    }

    /**
     * Returns a caption prompt for the given index. This is used to create a multi-question per
     * screen view.
     */
    public FormEntryCaption getCaptionPrompt(FormIndex index) {
        return formEntryController.getModel().getCaptionPrompt(index);
    }

    /**
     * Return the caption for the current FormIndex. This is usually used for a repeat prompt.
     */
    public FormEntryCaption getCaptionPrompt() {
        return formEntryController.getModel().getCaptionPrompt();
    }

    /**
     * This fires off the jr:preload actions and events to save values like the
     * end time of a form.
     */
    public boolean postProcessInstance() {
        return formEntryController.getModel().getForm().postProcessInstance();
    }

    /**
     * TODO: We need a good description of what this does, exactly, and why.
     */
    private FormInstance getInstance() {
        return formEntryController.getModel().getForm().getInstance();
    }

    /**
     * A convenience method for determining if the current FormIndex is in a group that is/should
     * be
     * displayed as a multi-question view. This is useful for returning from the formhierarchy view
     * to a selected index.
     */
    private boolean groupIsFieldList(FormIndex index) {
        // if this isn't a group, return right away
        IFormElement element = formEntryController.getModel().getForm().getChild(index);
        if (!(element instanceof GroupDef) || element.getAppearanceAttr() == null) {
            return false;
        }

        return element.getAppearanceAttr().toLowerCase(Locale.ENGLISH).contains(Appearances.FIELD_LIST);
    }

    private boolean repeatIsFieldList(FormIndex index) {
        return groupIsFieldList(index);
    }

    /**
     * Returns the `appearance` attribute of the current index, if any.
     */
    public String getAppearanceAttr(@NonNull FormIndex index) {
        // FormDef can't have an appearance, it would throw an exception.
        if (index.isBeginningOfFormIndex()) {
            return null;
        }

        IFormElement element = formEntryController.getModel().getForm().getChild(index);
        return element.getAppearanceAttr();
    }

    /**
     * Returns true if the question at the given FormIndex uses the search() appearance/function
     * of "fast itemset" feature.
     * <p>
     * Precondition: there is a question at the given FormIndex.
     */
    public boolean usesDatabaseExternalDataFeature(@NonNull FormIndex index) {
        String queryAttribute = getFormDef().getChild(index).getAdditionalAttribute(null, "query");
        String appearanceAttribute = getAppearanceAttr(index);

        return appearanceAttribute != null && ExternalDataUtil.SEARCH_FUNCTION_REGEX.matcher(appearanceAttribute).find()
                || queryAttribute != null && queryAttribute.length() > 0;
    }

    /**
     * Tests if the FormIndex 'index' is located inside a group that is marked as a "field-list"
     *
     * @return true if index is in a "field-list". False otherwise.
     */
    public boolean indexIsInFieldList(FormIndex index) {
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
            // If at least one of the groups you are inside is a field list, your index is in a field list
            for (FormEntryCaption caption : captions) {
                if (groupIsFieldList(caption.getIndex())) {
                    return true;
                }
            }
            return false;
        } else if (event == FormEntryController.EVENT_GROUP) {
            return groupIsFieldList(index);
        } else if (event == FormEntryController.EVENT_REPEAT) {
            return repeatIsFieldList(index);
        } else {
            return false;
        }

    }

    /**
     * Tests if the current FormIndex is located inside a group that is marked as a "field-list"
     *
     * @return true if index is in a "field-list". False otherwise.
     */
    public boolean indexIsInFieldList() {
        return indexIsInFieldList(getFormIndex());
    }

    public boolean currentPromptIsQuestion() {
        return getEvent() == FormEntryController.EVENT_QUESTION
                || ((getEvent() == FormEntryController.EVENT_GROUP
                || getEvent() == FormEntryController.EVENT_REPEAT)
                && indexIsInFieldList());
    }

    public boolean isCurrentQuestionFirstInForm() {
        boolean isFirstQuestion = true;
        FormIndex originalFormIndex = getFormIndex();
        try {
            isFirstQuestion = stepToPreviousScreenEvent() == FormEntryController.EVENT_BEGINNING_OF_FORM
                    && stepToNextScreenEvent() != FormEntryController.EVENT_PROMPT_NEW_REPEAT;
        } catch (JavaRosaException e) {
            Timber.d(e);
        }
        jumpToIndex(originalFormIndex);
        return isFirstQuestion;
    }

    /**
     * Attempts to save answer into the given FormIndex into the data model.
     */
    public int answerQuestion(FormIndex index, IAnswerData data) throws JavaRosaException {
        try {
            return formEntryController.answerQuestion(index, data, true);
        } catch (Exception e) {
            throw new JavaRosaException(e);
        }
    }

    /**
     * Goes through the entire form to make sure all entered answers comply with their constraints.
     * Constraints are ignored on 'jump to', so answers can be outside of constraints. We don't
     * allow saving to disk, though, until all answers conform to their constraints/requirements.
     *
     * @return ANSWER_OK and leave index unchanged or change index to bad value and return error
     * type.
     */
    public int validateAnswers(boolean markCompleted) throws JavaRosaException {
        ValidateOutcome outcome = getFormDef().validate(markCompleted);
        if (outcome != null) {
            this.jumpToIndex(outcome.failedPrompt);
            if (indexIsInFieldList()) {
                stepToPreviousScreenEvent();
            }
            return outcome.outcome;
        }
        return FormEntryController.ANSWER_OK;
    }

    /**
     * saveAnswer attempts to save the current answer into the data model without doing any
     * constraint checking. Only use this if you know what you're doing. For normal form filling
     * you
     * should always use answerQuestion or answerCurrentQuestion.
     *
     * @return true if saved successfully, false otherwise.
     */
    public boolean saveAnswer(FormIndex index, IAnswerData data) throws JavaRosaException {
        try {
            return formEntryController.saveAnswer(index, data, true);
        } catch (Exception e) {
            String dataType = data != null ? data.getClass().toString() : null;
            String ref = index != null ? index.getReference().toString() : null;
            Timber.w("Error saving answer of type %s with ref %s for index %s",
                    dataType, ref, index);

            throw new JavaRosaException(e);
        }
    }

    /**
     * Navigates forward in the form.
     *
     * @return the next event that should be handled by a view.
     */
    public int stepToNextEvent(boolean stepIntoGroup) {
        if ((getEvent() == FormEntryController.EVENT_GROUP
                || getEvent() == FormEntryController.EVENT_REPEAT)
                && indexIsInFieldList() && !isGroupEmpty() && !stepIntoGroup) {
            return stepOverGroup();
        } else {
            return formEntryController.stepToNextEvent();
        }
    }

    /**
     * If using a view like HierarchyView that doesn't support multi-question per screen, step over
     * the group represented by the FormIndex.
     */
    public int stepOverGroup() {
        GroupDef gd =
                (GroupDef) formEntryController.getModel().getForm()
                        .getChild(getFormIndex());
        List<FormIndex> indices = getIndicesForGroup(gd);

        // jump to the end of the group
        formEntryController.jumpToIndex(indices.get(indices.size() - 1));
        return stepToNextEvent(STEP_OVER_GROUP);
    }

    /**
     * Move the current form index to the index of the previous question in the form.
     * Step backward out of repeats and groups as needed. If the resulting question
     * is itself within a field-list, move upward to the group or repeat defining that
     * field-list.
     */
    public int stepToPreviousScreenEvent() throws JavaRosaException {
        try {
            if (getEvent() != FormEntryController.EVENT_BEGINNING_OF_FORM) {
                int event = stepToPreviousEvent();

                while (event == FormEntryController.EVENT_REPEAT_JUNCTURE
                        || event == FormEntryController.EVENT_PROMPT_NEW_REPEAT
                        || (event == FormEntryController.EVENT_QUESTION && indexIsInFieldList())
                        || ((event == FormEntryController.EVENT_GROUP
                        || event == FormEntryController.EVENT_REPEAT)
                        && !indexIsInFieldList())) {
                    event = stepToPreviousEvent();
                }

                // Handle nested field-list group
                if (getEvent() == FormEntryController.EVENT_GROUP) {
                    FormIndex currentIndex = getFormIndex();

                    if (groupIsFieldList(currentIndex)) {
                        // jump to outermost containing field-list
                        FormEntryCaption[] fclist = this.getCaptionHierarchy(currentIndex);
                        for (FormEntryCaption caption : fclist) {
                            if (groupIsFieldList(caption.getIndex())) {
                                formEntryController.jumpToIndex(caption.getIndex());
                                break;
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
     */
    public int stepToNextScreenEvent() throws JavaRosaException {
        try {
            if (getEvent() != FormEntryController.EVENT_END_OF_FORM) {
                int event;
                group_skip:
                do {
                    event = stepToNextEvent(FormController.STEP_OVER_GROUP);
                    switch (event) {
                        case FormEntryController.EVENT_QUESTION:
                        case FormEntryController.EVENT_END_OF_FORM:
                        case FormEntryController.EVENT_PROMPT_NEW_REPEAT:
                            break group_skip;
                        case FormEntryController.EVENT_GROUP:
                        case FormEntryController.EVENT_REPEAT:
                            try {
                                if (indexIsInFieldList() && getQuestionPrompts().length != 0) {
                                    break group_skip;
                                }
                            } catch (FormDesignException e) {
                                break group_skip;
                            }
                            // otherwise it's not a field-list group, so just skip it
                            break;
                        case FormEntryController.EVENT_REPEAT_JUNCTURE:
                            Timber.i("repeat juncture: %s", getFormIndex().getReference().toString());
                            // skip repeat junctures until we implement them
                            break;
                        default:
                            Timber.w("JavaRosa added a new EVENT type and didn't tell us... shame on them.");
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
     * Move the current form index to the next event of the given type
     * (or the end if none is found).
     */
    public int stepToNextEventType(int eventType) {
        int event = getEvent();
        do {
            if (event == FormEntryController.EVENT_END_OF_FORM) {
                break;
            }
            event = stepToNextEvent(FormController.STEP_OVER_GROUP);
        } while (event != eventType);

        return event;
    }

    /**
     * Move the current form index to the index of the first displayable group
     * (that is, a repeatable group or a visible group),
     * or to the start of the form.
     */
    public int stepToOuterScreenEvent() {
        FormIndex index = getFormIndex();

        // Step out once to begin with if we're coming from a question.
        if (getEvent() == FormEntryController.EVENT_QUESTION) {
            index = getPreviousLevel(index);
        }

        // Save where we started from.
        FormIndex startIndex = index;

        // Step out once more no matter what.
        index = getPreviousLevel(index);

        // Step out of any group indexes that are present, unless they're visible.
        while (index != null
                && getEvent(index) == FormEntryController.EVENT_GROUP
                && !isDisplayableGroup(index)) {
            index = getPreviousLevel(index);
        }

        if (index == null) {
            jumpToIndex(FormIndex.createBeginningOfFormIndex());
        } else {
            if (isDisplayableGroup(startIndex)) {
                // We were at a displayable group, so stepping back brought us to the previous level
                jumpToIndex(index);
            } else {
                // We were at a question, so stepping back brought us to either:
                // The beginning, or the start of a displayable group. So we need to step
                // out again to go past the group.
                index = getPreviousLevel(index);
                if (index == null) {
                    jumpToIndex(FormIndex.createBeginningOfFormIndex());
                } else {
                    jumpToIndex(index);
                }
            }
        }
        return getEvent();
    }

    /**
     * Returns true if the index is either a repeatable group or a visible group.
     */
    public boolean isDisplayableGroup(FormIndex index) {
        int event = getEvent(index);
        return event == FormEntryController.EVENT_REPEAT
                || event == FormEntryController.EVENT_PROMPT_NEW_REPEAT
                || (event == FormEntryController.EVENT_GROUP
                && isPresentationGroup(index) && isLogicalGroup(index));
    }

    /**
     * Returns true if the group has a displayable label,
     * i.e. it's a "presentation group".
     */
    private boolean isPresentationGroup(FormIndex groupIndex) {
        String label = getCaptionPrompt(groupIndex).getShortText();
        return label != null;
    }

    /**
     * Returns true if the group has an XML `ref` attribute,
     * i.e. it's a "logical group".
     * <p>
     * TODO: Improve this nasty way to recreate what XFormParser#parseGroup does for nodes without a `ref`.
     */
    private boolean isLogicalGroup(FormIndex groupIndex) {
        TreeReference groupRef = groupIndex.getReference();
        TreeReference parentRef = groupRef.getParentRef();
        IDataReference absRef = FormDef.getAbsRef(new XPathReference(groupRef), parentRef);
        IDataReference bindRef = getCaptionPrompt(groupIndex).getFormElement().getBind();
        // If the group's bind is equal to what it would have been set to during parsing, it must not have a ref.
        return !absRef.equals(bindRef);
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
     * @return FailedConstraint of first failed constraint or null if all questions were saved.
     */
    public FailedConstraint saveAllScreenAnswers(HashMap<FormIndex, IAnswerData> answers, boolean evaluateConstraints) throws JavaRosaException {
        if (currentPromptIsQuestion()) {
            for (FormIndex index : answers.keySet()) {
                FailedConstraint failedConstraint = saveOneScreenAnswer(
                        index,
                        answers.get(index),
                        evaluateConstraints
                );

                if (failedConstraint != null) {
                    return failedConstraint;
                }
            }
        }

        return null;
    }

    public FailedConstraint saveOneScreenAnswer(FormIndex index, IAnswerData answer, boolean evaluateConstraints) throws JavaRosaException {
        // Within a group, you can only save for question events
        if (getEvent(index) == FormEntryController.EVENT_QUESTION) {
            if (evaluateConstraints) {
                int saveStatus = answerQuestion(index, answer);
                if (saveStatus != FormEntryController.ANSWER_OK) {
                    return new FailedConstraint(index, saveStatus);
                }
            } else {
                saveAnswer(index, answer);
            }
        } else {
            Timber.w("Attempted to save an index referencing something other than a question: %s",
                    index.getReference().toString());
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

        formEntryController.stepToPreviousEvent();

        // If after we've stepped, we're in a field-list, jump back to the beginning of the group
        //

        if (indexIsInFieldList()
                && getEvent() == FormEntryController.EVENT_QUESTION) {
            // caption[0..len-1]
            // caption[len-1] == the question itself
            // caption[len-2] == the first group it is contained in.
            FormEntryCaption[] captions = getCaptionHierarchy();
            FormEntryCaption grp = captions[captions.length - 2];
            int event = formEntryController.jumpToIndex(grp.getIndex());
            // and test if this group or at least one of its children is relevant...
            FormIndex idx = grp.getIndex();
            if (!formEntryController.getModel().isIndexRelevant(idx)) {
                return stepToPreviousEvent();
            }
            idx = formEntryController.getModel().incrementIndex(idx, true);
            while (FormIndex.isSubElement(grp.getIndex(), idx)) {
                if (formEntryController.getModel().isIndexRelevant(idx)) {
                    return event;
                }
                idx = formEntryController.getModel().incrementIndex(idx, true);
            }
            return stepToPreviousEvent();
        } else if (indexIsInFieldList() && getEvent() == FormEntryController.EVENT_GROUP) {
            FormIndex grpidx = formEntryController.getModel().getFormIndex();
            int event = formEntryController.getModel().getEvent();
            // and test if this group or at least one of its children is relevant...
            if (!formEntryController.getModel().isIndexRelevant(grpidx)) {
                return stepToPreviousEvent(); // shouldn't happen?
            }
            FormIndex idx = formEntryController.getModel().incrementIndex(grpidx, true);
            while (FormIndex.isSubElement(grpidx, idx)) {
                if (formEntryController.getModel().isIndexRelevant(idx)) {
                    return event;
                }
                idx = formEntryController.getModel().incrementIndex(idx, true);
            }
            return stepToPreviousEvent();
        }

        return getEvent();

    }

    /**
     * Jumps to a given FormIndex.
     *
     * @return EVENT for the specified Index.
     */
    public int jumpToIndex(FormIndex index) {
        return formEntryController.jumpToIndex(index);
    }

    /**
     * Jumps to the next prompt for a repeated instance of the group referenced by the current FormIndex.
     */
    public void jumpToNewRepeatPrompt() {
        FormIndex repeatGroupIndex = getRepeatGroupIndex(getFormIndex(), getFormDef());
        Integer depth = repeatGroupIndex.getDepth();
        Integer promptDepth = null;

        while (!depth.equals(promptDepth)) {
            stepToNextEventType(FormEntryController.EVENT_PROMPT_NEW_REPEAT);
            promptDepth = getFormIndex().getDepth();
        }
    }

    /**
     * Creates a new repeated instance of the group referenced by the current FormIndex.
     */
    public void newRepeat() {
        formEntryController.newRepeat();
    }

    /**
     * If the current FormIndex is within a repeated group, will find the innermost repeat, delete
     * it, and jump the FormEntryController to the previous valid index. That is, if you have
     * group1
     * (2) > group2 (3) and you call deleteRepeat, it will delete the 3rd instance of group2.
     */
    public void deleteRepeat() {
        FormIndex fi = formEntryController.deleteRepeat();
        formEntryController.jumpToIndex(fi);
    }

    /**
     * Sets the current language.
     */
    public void setLanguage(String language) {
        formEntryController.setLanguage(language);
    }

    /**
     * Returns an array of question prompts corresponding to the current {@link FormIndex}. These
     * are the prompts that should be displayed to the user and don't include any non-relevant
     * questions.
     * <p>
     * The array has a single element if there is a question at this {@link FormIndex} or multiple
     * elements if there is a group.
     *
     * @throws FormDesignException if there is a group at this {@link FormIndex} and it contains
     *                          elements that are not questions or regular (non-repeat) groups.
     */
    public FormEntryPrompt[] getQuestionPrompts() throws FormDesignException {
        // For questions, there is only one.
        // For groups, there could be many, but we set that below
        FormEntryPrompt[] questions = new FormEntryPrompt[0];

        IFormElement element = formEntryController.getModel().getForm().getChild(getFormIndex());
        if (element instanceof GroupDef) {
            GroupDef gd = (GroupDef) element;
            // we only display relevant questions
            List<FormEntryPrompt> questionList = new ArrayList<>();
            for (FormIndex index : getIndicesForGroup(gd)) {
                if (getEvent(index) != FormEntryController.EVENT_QUESTION) {
                    FormDesignException e = new FormDesignException("Repeats in 'field-list' groups " +
                            "are not supported. Please update the form design to remove the " +
                            "following repeat from a field list: " + index.getReference().toString(false));
                    throw e;
                }

                // we only display relevant questions
                if (formEntryController.getModel().isIndexRelevant(index)) {
                    questionList.add(getQuestionPrompt(index));
                }
                questions = new FormEntryPrompt[questionList.size()];
                questionList.toArray(questions);
            }
        } else {
            // We have a question, so just get the one prompt
            questions = new FormEntryPrompt[1];
            questions[0] = getQuestionPrompt();
        }

        return questions;
    }

    private boolean isGroupEmpty() {
        GroupDef group = (GroupDef) formEntryController.getModel().getForm().getChild(getFormIndex());
        return getIndicesForGroup(group).isEmpty();
    }

    /**
     * Recursively gets all indices contained in this group and its children
     */
    private List<FormIndex> getIndicesForGroup(GroupDef gd) {
        return getIndicesForGroup(gd,
                formEntryController.getModel().incrementIndex(getFormIndex(), true), false);
    }

    private List<FormIndex> getIndicesForGroup(GroupDef gd, FormIndex currentChildIndex, boolean jumpIntoRepeatGroups) {
        List<FormIndex> indices = new ArrayList<>();
        for (int i = 0; i < gd.getChildren().size(); i++) {
            final FormEntryModel formEntryModel = formEntryController.getModel();
            if (getEvent(currentChildIndex) == FormEntryController.EVENT_GROUP
                    || (jumpIntoRepeatGroups && getEvent(currentChildIndex) == FormEntryController.EVENT_REPEAT)) {
                IFormElement nestedElement = formEntryModel.getForm().getChild(currentChildIndex);
                if (nestedElement instanceof GroupDef) {
                    indices.addAll(getIndicesForGroup((GroupDef) nestedElement,
                            formEntryModel.incrementIndex(currentChildIndex, true), jumpIntoRepeatGroups));
                    currentChildIndex = formEntryModel.incrementIndex(currentChildIndex, false);
                }
            } else if (!jumpIntoRepeatGroups || getEvent(currentChildIndex) != FormEntryController.EVENT_PROMPT_NEW_REPEAT) {
                indices.add(currentChildIndex);
                currentChildIndex = formEntryModel.incrementIndex(currentChildIndex, false);
            }
        }
        return indices;
    }

    /**
     * @return true if a group contains at least one relevant question, otherwise false
     */
    public boolean isGroupRelevant() {
        GroupDef groupDef = (GroupDef) getCaptionPrompt().getFormElement();
        FormIndex currentChildIndex = formEntryController.getModel().incrementIndex(getFormIndex(), true);
        for (FormIndex index : getIndicesForGroup(groupDef, currentChildIndex, true)) {
            if (formEntryController.getModel().isIndexRelevant(index)) {
                return true;
            }
        }
        return false;
    }

    public FormEntryPrompt getQuestionPrompt(FormIndex index) {
        return formEntryController.getModel().getQuestionPrompt(index);
    }

    public FormEntryPrompt getQuestionPrompt() {
        return formEntryController.getModel().getQuestionPrompt();
    }

    public String getQuestionPromptConstraintText(FormIndex index) {
        return formEntryController.getModel().getQuestionPrompt(index).getConstraintText();
    }

    public boolean currentCaptionPromptIsQuestion() {
        return getCaptionPrompt().getFormElement() instanceof QuestionDef;
    }

    public String getQuestionPromptRequiredText(FormIndex index) {
        // look for the text under the requiredMsg bind attribute
        String constraintText = getBindAttribute(index, XFormParser.NAMESPACE_JAVAROSA,
                "requiredMsg");
        if (constraintText != null) {
            XPathExpression xpathRequiredMsg;
            try {
                xpathRequiredMsg = XPathParseTool.parseXPath("string(" + constraintText + ")");
            } catch (Exception e) {
                // Expected in probably most cases.
                // This is a string literal, so no need to evaluate anything.
                return constraintText;
            }

            if (xpathRequiredMsg != null) {
                try {
                    FormDef form = formEntryController.getModel().getForm();
                    TreeElement treeElement = form.getMainInstance().resolveReference(
                            index.getReference());
                    EvaluationContext ec = new EvaluationContext(form.getEvaluationContext(),
                            treeElement.getRef());
                    Object value = xpathRequiredMsg.eval(form.getMainInstance(), ec);
                    if (!value.equals("")) {
                        return (String) value;
                    }
                    return null;
                } catch (Exception e) {
                    Timber.e(e, "Error evaluating a valid-looking required xpath ");
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
        System.arraycopy(v, 0, groups, 0, v.length - lastquestion);
        return groups;
    }

    /**
     * This is used to enable/disable the "Delete Repeat" menu option.
     */
    public boolean indexContainsRepeatableGroup() {
        FormEntryCaption[] groups = getCaptionHierarchy();
        if (groups.length == 0) {
            return false;
        }
        for (int i = 0; i < groups.length; i++) {
            if (groups[i].repeats()) {
                return true;
            }
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
        if (groups == null || groups.length == 0) {
            return null;
        } else {
            return groups[groups.length - 1];
        }
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
     */
    private IDataReference getSubmissionDataReference() {
        FormDef formDef = formEntryController.getModel().getForm();
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
     * form.
     *
     * @return true if the submission is the entire form.  If it is,
     * then the submission can be re-opened for editing
     * after it was marked-as-complete (provided it has
     * not been encrypted).
     */
    public boolean isSubmissionEntireForm() {
        IDataReference sub = getSubmissionDataReference();
        return getInstance().resolveReference(sub) == null;
    }

    /**
     * Constructs the XML payload for a filled-in form instance. This payload
     * enables a filled-in form to be re-opened and edited.
     */
    public ByteArrayPayload getFilledInFormXml() throws IOException {
        // assume no binary data inside the model.
        FormInstance datamodel = getInstance();
        XFormSerializingVisitor serializer = new XFormSerializingVisitor();

        return (ByteArrayPayload) serializer.createSerializedPayload(datamodel);
    }

    /**
     * Extract the portion of the form that should be uploaded to the server.
     */
    public ByteArrayPayload getSubmissionXml() throws IOException {
        FormInstance instance = getInstance();
        XFormSerializingVisitor serializer = new XFormSerializingVisitor();
        return (ByteArrayPayload) serializer.createSerializedPayload(instance,
                getSubmissionDataReference());
    }

    /**
     * Traverse the submission looking for the first matching tag in depth-first order.
     */
    private TreeElement findDepthFirst(TreeElement parent, String name) {
        int len = parent.getNumChildren();
        for (int i = 0; i < len; ++i) {
            TreeElement e = parent.getChildAt(i);
            if (name.equals(e.getName())) {
                return e;
            } else if (e.getNumChildren() != 0) {
                TreeElement v = findDepthFirst(e, name);
                if (v != null) {
                    return v;
                }
            }
        }
        return null;
    }

    /**
     * Get the OpenRosa required metadata of the portion of the form beng submitted
     */
    public InstanceMetadata getSubmissionMetadata() {
        FormDef formDef = formEntryController.getModel().getForm();
        TreeElement rootElement = formDef.getInstance().getRoot();

        TreeElement trueSubmissionElement;
        // Determine the information about the submission...
        SubmissionProfile p = formDef.getSubmissionProfile();
        if (p == null || p.getRef() == null) {
            trueSubmissionElement = rootElement;
        } else {
            IDataReference ref = p.getRef();
            trueSubmissionElement = formDef.getInstance().resolveReference(ref);
            // resolveReference returns null if the reference is to the root element...
            if (trueSubmissionElement == null) {
                trueSubmissionElement = rootElement;
            }
        }

        // and find the depth-first meta block in this...
        TreeElement e = findDepthFirst(trueSubmissionElement, "meta");

        String instanceId = null;
        String instanceName = null;
        AuditConfig auditConfig = null;

        if (e != null) {
            List<TreeElement> v;

            // instance id...
            v = e.getChildrenWithName(INSTANCE_ID);
            if (v.size() == 1) {
                IAnswerData sa = v.get(0).getValue();
                if (sa != null) {
                    instanceId = sa.getDisplayText();
                }
            }

            // instance name...
            v = e.getChildrenWithName(INSTANCE_NAME);
            if (v.size() == 1) {
                IAnswerData sa = v.get(0).getValue();
                if (sa != null) {
                    instanceName = sa.getDisplayText();
                }
            }

            // timing element...
            v = e.getChildrenWithName(AUDIT);
            if (v.size() == 1) {

                TreeElement auditElement = v.get(0);

                String locationPriority = auditElement.getBindAttributeValue(XML_OPENDATAKIT_NAMESPACE, "location-priority");
                String locationMinInterval = auditElement.getBindAttributeValue(XML_OPENDATAKIT_NAMESPACE, "location-min-interval");
                String locationMaxAge = auditElement.getBindAttributeValue(XML_OPENDATAKIT_NAMESPACE, "location-max-age");
                boolean isTrackingChangesEnabled = Boolean.parseBoolean(auditElement.getBindAttributeValue(XML_OPENDATAKIT_NAMESPACE, "track-changes"));
                boolean isIdentifyUserEnabled = Boolean.parseBoolean(auditElement.getBindAttributeValue(XML_OPENDATAKIT_NAMESPACE, "identify-user"));
                String trackChangesReason = auditElement.getBindAttributeValue(XML_OPENDATAKIT_NAMESPACE, "track-changes-reasons");

                auditConfig = new AuditConfig.Builder()
                        .setMode(locationPriority)
                        .setLocationMinInterval(locationMinInterval)
                        .setLocationMaxAge(locationMaxAge)
                        .setIsTrackingChangesEnabled(isTrackingChangesEnabled)
                        .setIsIdentifyUserEnabled(isIdentifyUserEnabled)
                        .setIsTrackChangesReasonEnabled(trackChangesReason != null && trackChangesReason.equals("on-form-edit"))
                        .createAuditConfig();

                IAnswerData answerData = new StringData();
                answerData.setValue(AUDIT_FILE_NAME);
                auditElement.setValue(answerData);
            }
        }

        return new InstanceMetadata(instanceId, instanceName, auditConfig);
    }

    /**
     * Returns true if the current form definition audits user location in the background.
     */
    public boolean currentFormAuditsLocation() {
        AuditConfig auditConfig = getSubmissionMetadata().auditConfig;

        return auditConfig != null && auditConfig.isLocationEnabled();
    }

    /**
     * Returns true if the current form definition collects user location in the background either
     * because of the audit configuration or because it contains odk:setgeopoint actions.
     */
    public boolean currentFormCollectsBackgroundLocation() {
        return currentFormAuditsLocation() || getFormDef().hasAction(SetGeopointActionHandler.ELEMENT_NAME);
    }

    public IAnswerData getAnswer(TreeReference treeReference) {
        return getFormDef().getMainInstance().resolveReference(treeReference).getValue();
    }
}
