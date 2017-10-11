package org.odk.collect.android.logic;

import org.javarosa.core.model.FormDef;
import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.instance.TreeReference;
import org.javarosa.form.api.FormEntryController;
import org.javarosa.form.api.FormEntryModel;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.odk.collect.android.utilities.TimerLogger;

import java.io.File;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FormControllerTest {

    @Mock
    File mockMediaFolder;
    @Mock
    FormEntryController mockFormEntryController;
    @Mock
    File mockInstancePath;
    @Mock
    FormIndex mockFormIndex;
    @Mock
    FormEntryModel mockFormEntryModel;



    // UUT
    private FormController formController;


    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(mockFormEntryController.getModel()).thenReturn(mockFormEntryModel);
        when(mockFormEntryModel.getFormIndex()).thenReturn(mockFormIndex);

        this.formController = new FormController(mockMediaFolder,
                mockFormEntryController, mockInstancePath);
    }

    @Test
    public void getFormDef_ReturnsTheFormDefinitionFromJavaRosaAPI() throws Exception {
        FormDef mockFormDefinition = mock(FormDef.class);
        when(mockFormEntryModel.getForm()).thenReturn(mockFormDefinition);

        assertEquals(formController.getFormDef(), mockFormDefinition);
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

// TODO: Determine how TimerLogger is initialized
//    @Test
//    public void getTimerLogger_LazyLoadsTimerLogger() throws Exception {
//        TimerLogger logger = formController.getTimerLogger();
//        assertNotNull(logger);
//
//        assertEquals(formController.getTimerLogger(), logger);
//    }


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
        } catch(IllegalArgumentException e) {
            assertNotNull(e);
            assertEquals(e.getMessage(), "unexpected string from XPath");
        }
    }

//    TODO: Figure out how candidate XPaths are found
//    @Test
//    public void getIndexFromXPath_TriesToFindIndexInMiddleOfForm() throws Exception {
//    }


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
        when(mockFormEntryModel.getFormTitle()).thenReturn("Title");
        assertEquals(formController.getFormTitle(), "Title");
    }

}