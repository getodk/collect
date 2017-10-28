package org.odk.collect.android.logic;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.GroupDef;
import org.javarosa.core.model.IFormElement;
import org.javarosa.core.model.ValidateOutcome;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.form.api.FormEntryCaption;
import org.javarosa.form.api.FormEntryController;
import org.javarosa.form.api.FormEntryModel;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.odk.collect.android.exception.JavaRosaException;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class FormControllerTest {

    @Mock
    File mockMediaFolder;
    @Mock
    File mockInstancePath;
    @Mock
    FormEntryController mockFormEntryController;
    @Mock
    FormEntryModel mockFormEntryModel;
    @Mock
    FormIndex mockFormIndex;
    @Mock
    FormDef mockForm;

    // UUT
    private FormController formController;


    @Before
    public void setup() throws Exception {
        when(mockFormEntryController.getModel()).thenReturn(mockFormEntryModel);
        when(mockFormEntryModel.getFormIndex()).thenReturn(mockFormIndex);
        when(mockFormEntryModel.getForm()).thenReturn(mockForm);

        this.formController = new FormController(mockMediaFolder,
                mockFormEntryController, mockInstancePath);
    }

    @Test
    public void getFormDef_ReturnsTheFormDefinitionFromJavaRosaAPI() throws Exception {

        assertEquals(formController.getFormDef(), mockForm);
    }

    @Test
    public void getMediaFolder_ReturnsTheMediaFileForTheController() throws Exception {
        assertEquals(formController.getMediaFolder(), mockMediaFolder);
    }


    @Test
    public void setInstancePath_AssignsTheValueOfTheControllersInstancePath() throws Exception {
        assertEquals(formController.getInstancePath(), mockInstancePath);

        formController.setInstancePath(null);
        assertNull(formController.getInstancePath());
    }


    @Test
    public void setIndexWaitingForData_AssignsTheValueOfTheControllersWaitingIndex() throws Exception {
        assertNull(formController.getIndexWaitingForData());

        formController.setIndexWaitingForData(mockFormIndex);
        assertEquals(formController.getIndexWaitingForData(), mockFormIndex);
    }

    /*TODO: Determine how TimerLogger is initialized
    @Test
    public void getTimerLogger_LazyLoadsTimerLogger() throws Exception {
        TimerLogger logger = formController.getTimerLogger();
        assertNotNull(logger);

        assertEquals(formController.getTimerLogger(), logger);
    }*/


    @Test
    public void getXPath_ReturnsStringLogValuesDependingOnTheEvent() throws Exception {
        TreeReference mockTreeReference = mock(TreeReference.class);
        when(mockFormIndex.getReference()).thenReturn(mockTreeReference);
        when(mockTreeReference.toString()).thenReturn("tree");

        when(mockFormEntryModel.getEvent()).thenReturn(FormEntryController.EVENT_BEGINNING_OF_FORM);
        assertEquals(formController.getXPath(mockFormIndex), "beginningOfForm");

        when(mockFormEntryModel.getEvent()).thenReturn(FormEntryController.EVENT_END_OF_FORM);
        assertEquals(formController.getXPath(mockFormIndex), "endOfForm");

        when(mockFormEntryModel.getEvent()).thenReturn(FormEntryController.EVENT_GROUP);
        assertEquals(formController.getXPath(mockFormIndex), "group.tree");

        when(mockFormEntryModel.getEvent()).thenReturn(FormEntryController.EVENT_QUESTION);
        assertEquals(formController.getXPath(mockFormIndex), "question.tree");

        when(mockFormEntryModel.getEvent()).thenReturn(FormEntryController.EVENT_PROMPT_NEW_REPEAT);
        assertEquals(formController.getXPath(mockFormIndex), "promptNewRepeat.tree");

        when(mockFormEntryModel.getEvent()).thenReturn(FormEntryController.EVENT_REPEAT);
        assertEquals(formController.getXPath(mockFormIndex), "repeat.tree");

        when(mockFormEntryModel.getEvent()).thenReturn(FormEntryController.EVENT_REPEAT_JUNCTURE);
        assertEquals(formController.getXPath(mockFormIndex), "repeatJuncture.tree");

        when(mockFormEntryModel.getEvent()).thenReturn(102030);
        assertEquals(formController.getXPath(mockFormIndex), "unexpected");
    }

    @Test
    public void getIndexFromXPath_ReturnsBeginningOrEndOfForm() throws Exception {
        FormIndex beginningIndex = formController.getIndexFromXPath("beginningOfForm");
        assertEquals(beginningIndex.getLocalIndex(), -1);
        assertTrue(beginningIndex.isBeginningOfFormIndex());

        FormIndex endIndex = formController.getIndexFromXPath("endOfForm");
        assertEquals(endIndex.getLocalIndex(), -1);
        assertTrue(endIndex.isEndOfFormIndex());
    }

    @Test
    public void getIndexFromXPath_ThrowsIllegalArgumentExceptionIfUnexpected() throws Exception {
        try {
            formController.getIndexFromXPath("unexpected");
        } catch (IllegalArgumentException e) {
            assertNotNull(e);
            assertEquals(e.getMessage(), "unexpected string from XPath");
        }
    }

    /*TODO: Figure out how candidate XPaths are found
    @Test
    public void getIndexFromXPath_TriesToFindIndexInMiddleOfForm() throws Exception {
    }*/


    @Test
    public void getEvent_GetsTheEventFromTheTheModelWithTheGivenIndex() throws Exception {
        when(mockFormEntryModel.getEvent(mockFormIndex)).thenReturn(1223);
        assertEquals(formController.getEvent(mockFormIndex), 1223);
    }

    @Test
    public void getFormIndex_ReturnsTheFormIndexFromTheModel() throws Exception {
        assertEquals(formController.getFormIndex(), mockFormIndex);
    }

    @Test
    public void getLanguages_ReturnsTheLanguagesAvailableFromTheModel() throws Exception {
        String[] languages = {"eng-us", "eng-uk"};
        when(mockFormEntryModel.getLanguages()).thenReturn(languages);

        assertEquals(formController.getLanguages()[0], "eng-us");
        assertEquals(formController.getLanguages()[1], "eng-uk");
    }

    @Test
    public void getFormTitle_ReturnsTheFormTitleFromTheModel() throws Exception {
        when(mockFormEntryModel.getFormTitle()).thenReturn("Title");
        assertEquals(formController.getFormTitle(), "Title");
    }

    @Test
    public void getFormLanguage_ReturnsTheLanguageOfTheFormFromTheModel() throws Exception {
        when(mockFormEntryModel.getLanguage()).thenReturn("eng-us");
        assertEquals(formController.getLanguage(), "eng-us");
    }

    /*TODO: Figure out what this method is doing
    @Test
    public void getBindAttribute_ReturnsTheBindAttributeValueForTheNamespaceAndName() throws Exception {

    }

    TODO: This method is private. Useful for when the method is used internally
    @Test
    public void getCaptionHierarchy_ReturnsTheHierarchyForTheFormModel() throws Exception {
        FormEntryCaption captionA = new FormEntryCaption();
        FormEntryCaption captionB = new FormEntryCaption();
        FormEntryCaption[] captionHierarchy = {captionA, captionB};

        when(mockFormEntryModel.getCaptionHierarchy()).thenReturn(captionHierarchy);

    }*/


    @Test
    public void getCaptionPrompt_ReturnsThePromptForTheFormModel() throws Exception {
        FormEntryCaption caption = new FormEntryCaption();
        when(mockFormEntryModel.getCaptionPrompt()).thenReturn(caption);

        assertEquals(formController.getCaptionPrompt(), caption);
    }

    @Test
    public void getCaptionPrompt_ReturnsThePromptForTheFormModelAtGivenIndex() throws Exception {
        FormEntryCaption caption = new FormEntryCaption();
        when(mockFormEntryModel.getCaptionPrompt(mockFormIndex)).thenReturn(caption);

        assertEquals(formController.getCaptionPrompt(mockFormIndex), caption);
    }

    @Test
    public void postProcessInstance_ReturnsTheValueOfDispatchingTheFormToProcess() throws Exception {
        when(mockForm.postProcessInstance()).thenReturn(true);
        assertTrue(formController.postProcessInstance());
    }

    /*TODO: This method is private. Useful for when the method is used internally
    @Test
    public void getInstance_ReturnsTheInstanceOfTheFormDefinition() throws Exception {
        FormInstance mockFormInstance = mock(FormInstance.class);
        when(mockForm.getInstance()).thenReturn(mockFormInstance);

    }*/


    @Test
    public void indexIsInFieldList_ReturnsFalseIfIndexIsNotInHandledEventType() throws Exception {
        // Only QUESTION, GROUP, AND REPEAT are currently supported
        when(mockFormEntryModel.getEvent(mockFormIndex)).thenReturn(FormEntryController.EVENT_PROMPT_NEW_REPEAT);

        boolean isInFieldList = formController.indexIsInFieldList();
        assertFalse(isInFieldList);
    }

    @Test
    public void indexIsInFieldList_ReturnsFalseIfTheQuestionHasNoGroup() throws Exception {
        // No group if there is one or less captions
        FormEntryCaption mockCaptionA = mock(FormEntryCaption.class);
        FormEntryCaption[] captions = new FormEntryCaption[]{ mockCaptionA };

        when(mockFormEntryModel.getCaptionHierarchy(mockFormIndex)).thenReturn(captions);
        when(mockFormEntryModel.getEvent(mockFormIndex)).thenReturn(FormEntryController.EVENT_QUESTION);

        boolean isInFieldList = formController.indexIsInFieldList();
        assertFalse(isInFieldList);
    }

    /**
     * Helper method set up similar cases for testing indexIsInFieldList(FormIndex)
     */
    private void setupIndexIsInFieldListMocks() {
        FormEntryCaption mockCaptionA = mock(FormEntryCaption.class);
        FormEntryCaption mockCaptionB = mock(FormEntryCaption.class);
        FormEntryCaption[] captions = new FormEntryCaption[]{ mockCaptionA, mockCaptionB };

        when(mockFormEntryModel.getCaptionHierarchy(mockFormIndex)).thenReturn(captions);
        when(mockCaptionA.getIndex()).thenReturn(mockFormIndex);
    }

    @Test
    public void indexIsInFieldList_EventQuestion_ReturnsFalseIfIndexElementIsNotAGroup() throws Exception {
        when(mockFormEntryModel.getEvent(mockFormIndex)).thenReturn(FormEntryController.EVENT_QUESTION);
        setupIndexIsInFieldListMocks();

        // Mock element is not type of GroupDef
        IFormElement mockElement = mock(IFormElement.class);
        when(mockForm.getChild(mockFormIndex)).thenReturn(mockElement);

        boolean isInFieldList = formController.indexIsInFieldList();
        assertFalse(isInFieldList);
    }

    @Test
    public void indexIsInFieldList_EventQuestion_ReturnsFalseIfGroupAppearanceIsNotEqualToFieldList() throws Exception {
        when(mockFormEntryModel.getEvent(mockFormIndex)).thenReturn(FormEntryController.EVENT_QUESTION);
        setupIndexIsInFieldListMocks();

        // Group is of wrong appearance attribute
        GroupDef mockGroup = mock(GroupDef.class);
        when(mockForm.getChild(mockFormIndex)).thenReturn(mockGroup);
        when(mockGroup.getAppearanceAttr()).thenReturn("not_list");

        boolean isInFieldList = formController.indexIsInFieldList();
        assertFalse(isInFieldList);
    }

    @Test
    public void indexIsInFieldList_EventQuestion_ReturnsTrueIfGroupAppearanceIsEqualToFieldList() throws Exception {
        when(mockFormEntryModel.getEvent(mockFormIndex)).thenReturn(FormEntryController.EVENT_QUESTION);
        setupIndexIsInFieldListMocks();

        GroupDef mockGroup = mock(GroupDef.class);
        when(mockForm.getChild(mockFormIndex)).thenReturn(mockGroup);
        when(mockGroup.getAppearanceAttr()).thenReturn("field-list");

        boolean isInFieldList = formController.indexIsInFieldList();
        assertTrue(isInFieldList);
    }

    // TODO: The following 6 test cases are nearly identical to the three above. This is a sign
    // TODO: that the code is overly repetitive and can be simplified
    @Test
    public void indexIsInFieldList_EventGroup_ReturnsFalseIfIndexElementIsNotAGroup() throws Exception {
        when(mockFormEntryModel.getEvent(mockFormIndex)).thenReturn(FormEntryController.EVENT_GROUP);
        setupIndexIsInFieldListMocks();

        // Mock element is not type of GroupDef
        IFormElement mockElement = mock(IFormElement.class);
        when(mockForm.getChild(mockFormIndex)).thenReturn(mockElement);

        boolean isInFieldList = formController.indexIsInFieldList();
        assertFalse(isInFieldList);
    }

    @Test
    public void indexIsInFieldList_EventGroup_ReturnsFalseIfGroupAppearanceIsNotEqualToFieldList() throws Exception {
        when(mockFormEntryModel.getEvent(mockFormIndex)).thenReturn(FormEntryController.EVENT_GROUP);
        when(mockFormEntryModel.getEvent()).thenReturn(FormEntryController.EVENT_GROUP);
        setupIndexIsInFieldListMocks();

        // Mock element is not type of GroupDef
        GroupDef mockGroup = mock(GroupDef.class);
        when(mockForm.getChild(mockFormIndex)).thenReturn(mockGroup);
        when(mockGroup.getAppearanceAttr()).thenReturn("not_list");

        boolean isInFieldList = formController.indexIsInFieldList();
        assertFalse(isInFieldList);

        // assert that this configuration is not a valid question prompt
        boolean isQuestion = formController.currentPromptIsQuestion();
        assertFalse(isQuestion);
    }

    @Test
    public void indexIsInFieldList_EventGroup_ReturnsTrueIfGroupAppearanceIsEqualToFieldList() throws Exception {
        when(mockFormEntryModel.getEvent(mockFormIndex)).thenReturn(FormEntryController.EVENT_GROUP);
        when(mockFormEntryModel.getEvent()).thenReturn(FormEntryController.EVENT_GROUP);
        setupIndexIsInFieldListMocks();

        // Mock element is not type of GroupDef
        GroupDef mockGroup = mock(GroupDef.class);
        when(mockForm.getChild(mockFormIndex)).thenReturn(mockGroup);
        when(mockGroup.getAppearanceAttr()).thenReturn("field-list");

        boolean isInFieldList = formController.indexIsInFieldList();
        assertTrue(isInFieldList);

        // assert that this configuration is also a valid question prompt
        boolean isQuestion = formController.currentPromptIsQuestion();
        assertTrue(isQuestion);
    }


    @Test
    public void indexIsInFieldList_EventRepeat_ReturnsFalseIfIndexElementIsNotAGroup() throws Exception {
        when(mockFormEntryModel.getEvent(mockFormIndex)).thenReturn(FormEntryController.EVENT_REPEAT);
        setupIndexIsInFieldListMocks();

        // Mock element is not type of GroupDef
        IFormElement mockElement = mock(IFormElement.class);
        when(mockForm.getChild(mockFormIndex)).thenReturn(mockElement);

        boolean isInFieldList = formController.indexIsInFieldList();
        assertFalse(isInFieldList);
    }

    @Test
    public void indexIsInFieldList_EventRepeat_ReturnsFalseIfGroupAppearanceIsNotEqualToFieldList() throws Exception {
        when(mockFormEntryModel.getEvent(mockFormIndex)).thenReturn(FormEntryController.EVENT_REPEAT);
        when(mockFormEntryModel.getEvent()).thenReturn(FormEntryController.EVENT_REPEAT);
        setupIndexIsInFieldListMocks();

        // Mock element is not type of GroupDef
        GroupDef mockGroup = mock(GroupDef.class);
        when(mockForm.getChild(mockFormIndex)).thenReturn(mockGroup);
        when(mockGroup.getAppearanceAttr()).thenReturn("not_list");

        boolean isInFieldList = formController.indexIsInFieldList();
        assertFalse(isInFieldList);

        // assert that this configuration is not a valid question prompt
        boolean isQuestion = formController.currentPromptIsQuestion();
        assertFalse(isQuestion);
    }

    @Test
    public void indexIsInFieldList_EventRepeat_ReturnsTrueIfGroupAppearanceIsEqualToFieldList() throws Exception {
        when(mockFormEntryModel.getEvent(mockFormIndex)).thenReturn(FormEntryController.EVENT_REPEAT);
        when(mockFormEntryModel.getEvent()).thenReturn(FormEntryController.EVENT_REPEAT);
        setupIndexIsInFieldListMocks();

        // Mock element is not type of GroupDef
        GroupDef mockGroup = mock(GroupDef.class);
        when(mockForm.getChild(mockFormIndex)).thenReturn(mockGroup);
        when(mockGroup.getAppearanceAttr()).thenReturn("field-list");

        // Assert it is the field list
        boolean isInFieldList = formController.indexIsInFieldList();
        assertTrue(isInFieldList);

        // assert that this configuration is also a valid question prompt
        boolean isQuestion = formController.currentPromptIsQuestion();
        assertTrue(isQuestion);
    }

    @Test
    public void currentPromptIsQuestion_ReturnsTrueIfEventTypeIsQuestion() throws Exception {
        when(mockFormEntryModel.getEvent()).thenReturn(FormEntryController.EVENT_QUESTION);

        boolean isQuestion = formController.currentPromptIsQuestion();
        assertTrue(isQuestion);
    }

    @Test
    public void answerQuestion_SavesDataAtIndexToTheController() throws Exception {
        IAnswerData mockData = mock(IAnswerData.class);

        formController.answerQuestion(mockFormIndex, mockData);
        verify(mockFormEntryController).answerQuestion(mockFormIndex, mockData, true);
    }

    @Test(expected = JavaRosaException.class)
    public void answerQuestion_ThrowsNewJavaRosaExceptionIfAnswerDidNotAnswer() throws Exception {
        when(mockFormEntryController.answerQuestion(any(FormIndex.class), any(IAnswerData.class), anyBoolean()))
                .thenThrow(new JavaRosaException(new Throwable()));

        IAnswerData mockData = mock(IAnswerData.class);
        formController.answerQuestion(mockFormIndex, mockData);
    }

    @Test
    public void validateAnswers_ReturnsAnswerOkayIfOutcomeIsNull() throws Exception {
        when(mockForm.validate(anyBoolean())).thenReturn(null);

        int value = formController.validateAnswers(false);
        assertEquals(value, FormEntryController.ANSWER_OK);
    }

    @Test
    public void validateAnswer_JumpsToIndexIfOutcomeIsReturned() throws Exception {
        ValidateOutcome outcome = mock(ValidateOutcome.class);
        when(mockForm.validate(anyBoolean())).thenReturn(outcome);

        formController.validateAnswers(false);
        verify(mockFormEntryController).jumpToIndex(null);
    }

    @Test(expected = JavaRosaException.class)
    public void saveAnswer_ThrowsNewJavaRosaExceptionIfAnswerDidNotSave() throws Exception {
        when(mockFormEntryController.saveAnswer(any(FormIndex.class), any(IAnswerData.class), anyBoolean()))
                .thenThrow(new JavaRosaException(new Throwable()));
    }

    
}