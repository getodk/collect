package org.odk.collect.android.javarosawrapper

import org.javarosa.core.model.FormDef
import org.javarosa.core.model.FormIndex
import org.javarosa.core.model.data.IAnswerData
import org.javarosa.core.model.instance.TreeReference
import org.javarosa.core.services.transport.payload.ByteArrayPayload
import org.javarosa.form.api.FormEntryCaption
import org.javarosa.form.api.FormEntryPrompt
import org.odk.collect.android.exception.JavaRosaException
import org.odk.collect.android.formentry.audit.AuditEventLogger
import org.odk.collect.entities.Entity
import java.io.File
import java.io.IOException
import java.util.stream.Stream

interface FormController {
    fun getFormDef(): FormDef?

    fun getMediaFolder(): File?

    fun getInstanceFile(): File?

    fun setInstanceFile(instanceFile: File?)

    fun getAbsoluteInstancePath(): String?

    fun getLastSavedPath(): String?

    fun getIndexWaitingForData(): FormIndex?

    fun setIndexWaitingForData(index: FormIndex?)

    fun getAuditEventLogger(): AuditEventLogger?

    /**
     * For logging purposes...
     *
     * @return xpath value for this index
     */
    fun getXPath(index: FormIndex?): String?

    fun getIndexFromXPath(xpath: String): FormIndex?

    /**
     * returns the event for the current FormIndex.
     */
    fun getEvent(): Int

    /**
     * returns the event for the given FormIndex.
     */
    fun getEvent(index: FormIndex?): Int

    /**
     * @return current FormIndex.
     */
    fun getFormIndex(): FormIndex?

    /**
     * @return the currently selected language.
     */
    fun getLanguage(): String?

    /**
     * Return the langauges supported by the currently loaded form.
     *
     * @return Array of Strings containing the languages embedded in the XForm.
     */
    fun getLanguages(): Array<String>?

    /**
     * Sets the current language.
     */
    fun setLanguage(language: String?)

    /**
     * @return A String containing the title of the current form.
     */
    fun getFormTitle(): String?

    /**
     * Return the caption for the current FormIndex. This is usually used for a repeat prompt.
     */
    fun getCaptionPrompt(): FormEntryCaption?

    /**
     * Returns a caption prompt for the given index. This is used to create a multi-question per
     * screen view.
     */
    fun getCaptionPrompt(index: FormIndex?): FormEntryCaption?

    /**
     * This fires off the jr:preload actions and events to save values like the
     * end time of a form.
     */
    fun finalizeForm()

    /**
     * Returns true if the question at the given FormIndex uses the search() appearance/function
     * of "fast itemset" feature.
     * <p>
     * Precondition: there is a question at the given FormIndex.
     */
    fun usesDatabaseExternalDataFeature(index: FormIndex): Boolean

    /**
     * Tests if the current FormIndex is located inside a group that is marked as a "field-list"
     *
     * @return true if index is in a "field-list". False otherwise.
     */
    fun indexIsInFieldList(): Boolean

    /**
     * Tests if the FormIndex 'index' is located inside a group that is marked as a "field-list"
     *
     * @return true if index is in a "field-list". False otherwise.
     */
    fun indexIsInFieldList(index: FormIndex?): Boolean

    fun currentPromptIsQuestion(): Boolean

    fun isCurrentQuestionFirstInForm(): Boolean

    /**
     * Attempts to save answer into the given FormIndex into the data model.
     */
    @Throws(JavaRosaException::class)
    fun answerQuestion(index: FormIndex?, data: IAnswerData?): Int

    /**
     * Goes through the entire form to make sure all entered answers comply with their constraints.
     * Constraints are ignored on 'jump to', so answers can be outside of constraints. We don't
     * allow saving to disk, though, until all answers conform to their constraints/requirements.
     *
     * @return ANSWER_OK and leave index unchanged or change index to bad value and return error
     * type.
     */
    @Throws(JavaRosaException::class)
    fun validateAnswers(markCompleted: Boolean): Int

    /**
     * saveAnswer attempts to save the current answer into the data model without doing any
     * constraint checking. Only use this if you know what you're doing. For normal form filling
     * you
     * should always use answerQuestion or answerCurrentQuestion.
     *
     * @return true if saved successfully, false otherwise.
     */
    @Throws(JavaRosaException::class)
    fun saveAnswer(index: FormIndex?, data: IAnswerData?): Boolean

    /**
     * Navigates forward in the form.
     *
     * @return the next event that should be handled by a view.
     */
    fun stepToNextEvent(stepIntoGroup: Boolean): Int

    /**
     * Navigates backward in the form.
     *
     * @return the event that should be handled by a view.
     */
    fun stepToPreviousEvent(): Int

    /**
     * Jumps to a given FormIndex.
     *
     * @return EVENT for the specified Index.
     */
    fun jumpToIndex(index: FormIndex?): Int

    /**
     * If using a view like HierarchyView that doesn't support multi-question per screen, step over
     * the group represented by the FormIndex.
     */
    fun stepOverGroup(): Int

    /**
     * Move the current form index to the index of the previous question in the form.
     * Step backward out of repeats and groups as needed. If the resulting question
     * is itself within a field-list, move upward to the group or repeat defining that
     * field-list.
     */
    @Throws(JavaRosaException::class)
    fun stepToPreviousScreenEvent(): Int

    /**
     * Move the current form index to the index of the next question in the form.
     * Stop if we should ask to create a new repeat group or if we reach the end of the form.
     * If we enter a group or repeat, return that if it is a field-list definition.
     * Otherwise, descend into the group or repeat searching for the first question.
     */
    @Throws(JavaRosaException::class)
    fun stepToNextScreenEvent(): Int

    /**
     * Move the current form index to the index of the first displayable group
     * (that is, a repeatable group or a visible group),
     * or to the start of the form.
     */
    fun stepToOuterScreenEvent(): Int

    /**
     * Jumps to the next prompt for a repeated instance of the group referenced by the current FormIndex.
     */
    fun jumpToNewRepeatPrompt()

    /**
     * Returns true if the index is either a repeatable group or a visible group.
     */
    fun isDisplayableGroup(index: FormIndex?): Boolean

    @Throws(JavaRosaException::class)
    fun saveOneScreenAnswer(index: FormIndex?, data: IAnswerData?, evaluateConstraints: Boolean): FailedConstraint?

    /**
     * @return FailedConstraint of first failed constraint or null if all questions were saved.
     */
    @Throws(JavaRosaException::class)
    fun saveAllScreenAnswers(answers: HashMap<FormIndex, IAnswerData>?, evaluateConstraints: Boolean): FailedConstraint?

    /**
     * Creates a new repeated instance of the group referenced by the current FormIndex.
     */
    fun newRepeat()

    /**
     * If the current FormIndex is within a repeated group, will find the innermost repeat, delete
     * it, and jump the FormEntryController to the previous valid index. That is, if you have
     * group1
     * (2) > group2 (3) and you call deleteRepeat, it will delete the 3rd instance of group2.
     */
    fun deleteRepeat()

    fun getQuestionPrompt(): FormEntryPrompt?

    fun getQuestionPrompt(index: FormIndex?): FormEntryPrompt?

    /**
     * Returns an array of question prompts corresponding to the current {@link FormIndex}. These
     * are the prompts that should be displayed to the user and don't include any non-relevant
     * questions.
     * <p>
     * The array has a single element if there is a question at this {@link FormIndex} or multiple
     * elements if there is a group.
     *
     * @throws RepeatsInFieldListException if there is a group at this {@link FormIndex} and it contains
     *                          elements that are not questions or regular (non-repeat) groups.
     */
    @Throws(RepeatsInFieldListException::class)
    fun getQuestionPrompts(): Array<FormEntryPrompt>

    fun getQuestionPromptConstraintText(index: FormIndex?): String?

    fun getQuestionPromptRequiredText(index: FormIndex?): String?

    fun currentCaptionPromptIsQuestion(): Boolean

    /**
     * @return true if a group contains at least one relevant question, otherwise false
     */
    fun isGroupRelevant(): Boolean

    /**
     * Returns an array of FormEntryCaptions for current FormIndex.
     */
    fun getGroupsForCurrentIndex(): Array<FormEntryCaption>?

    /**
     * This is used to enable/disable the "Delete Repeat" menu option.
     */
    fun indexContainsRepeatableGroup(): Boolean

    /**
     * The count of the closest group that repeats or -1.
     */
    fun getLastRepeatedGroupRepeatCount(): Int

    /**
     * The name of the closest group that repeats or null.
     */
    fun getLastRepeatedGroupName(): String?

    /**
     * The text of closest group the prompt belongs to.
     */
    fun getLastGroupText(): String?

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
    fun isSubmissionEntireForm(): Boolean

    /**
     * Constructs the XML payload for a filled-in form instance. This payload
     * enables a filled-in form to be re-opened and edited.
     */
    @Throws(IOException::class)
    fun getFilledInFormXml(): ByteArrayPayload?

    /**
     * Extract the portion of the form that should be uploaded to the server.
     */
    @Throws(IOException::class)
    fun getSubmissionXml(): ByteArrayPayload?

    /**
     * Get the OpenRosa required metadata of the portion of the form beng submitted
     */
    fun getSubmissionMetadata(): InstanceMetadata?

    /**
     * Returns true if the current form definition audits user location in the background.
     */
    fun currentFormAuditsLocation(): Boolean

    /**
     * Returns true if the current form definition collects user location in the background either
     * because of the audit configuration or because it contains odk:setgeopoint actions.
     */
    fun currentFormCollectsBackgroundLocation(): Boolean

    fun getAnswer(treeReference: TreeReference?): IAnswerData?

    fun getEntities(): Stream<Entity>
}
