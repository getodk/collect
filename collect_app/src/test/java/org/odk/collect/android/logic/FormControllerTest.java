package org.odk.collect.android.logic;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.form.api.FormEntryCaption;
import org.javarosa.form.api.FormEntryController;
import org.javarosa.form.api.FormEntryModel;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
        MockitoAnnotations.initMocks(this);
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
}