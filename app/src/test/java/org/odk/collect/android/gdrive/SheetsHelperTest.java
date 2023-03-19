package org.odk.collect.android.gdrive;

import com.google.api.services.sheets.v4.model.Request;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.SpreadsheetProperties;
import com.google.api.services.sheets.v4.model.ValueRange;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.odk.collect.android.gdrive.sheets.SheetsHelper;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import androidx.test.ext.junit.runners.AndroidJUnit4;

/**
 * @author Shobhit Agarwal
 */
@RunWith(AndroidJUnit4.class)
public class SheetsHelperTest {

    @Mock
    private GoogleSheetsApi googleSheetsAPI;

    private SheetsHelper sheetsHelper;

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);
        sheetsHelper = spy(new SheetsHelper(googleSheetsAPI));
    }

    @Test
    public void resizeSpreadsheetTest() throws IOException {
        sheetsHelper.resizeSpreadSheet("spreadsheet_id", 1, 5);
        assertBatchUpdateCalled(1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void resizeSpreadsheetShouldThrowErrorWhenSheetIdLessThanZero() throws IOException {
        sheetsHelper.resizeSpreadSheet("spreadsheet_id", -1, 4);
        assertBatchUpdateCalled(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void resizeSpreadsheetShouldThrowErrorWhenColumnSizeLessThanOne() throws IOException {
        sheetsHelper.resizeSpreadSheet("spreadsheet_id", 0, 0);
        assertBatchUpdateCalled(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void insertRowShouldThrowErrorWhenValueRangeIsNull() throws IOException {
        sheetsHelper.insertRow("spreadsheet_id", "sheet_name", null);
    }

    @Test
    public void insertRowTest() throws IOException {
        ValueRange valueRange = new ValueRange();
        sheetsHelper.insertRow("spreadsheet_id", "sheet_name", valueRange);
        verify(googleSheetsAPI).insertRow("spreadsheet_id", "sheet_name", valueRange);
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateRowShouldThrowErrorWhenValueRangeIsNull() throws IOException {
        sheetsHelper.updateRow("spreadsheet_id", "sheet_name", null);
    }

    @Test
    public void updateRowTest() throws IOException {
        ValueRange valueRange = new ValueRange();
        sheetsHelper.updateRow("spreadsheet_id", "sheet_name!A1", valueRange);
        verify(googleSheetsAPI).updateRow("spreadsheet_id", "sheet_name!A1", valueRange);
    }

    @Test
    public void getSpreadsheetTest() throws IOException {
        Spreadsheet mockedSpreadsheet = mock(Spreadsheet.class);
        SpreadsheetProperties mockedProperties = mock(SpreadsheetProperties.class);

        doReturn("sheet_title").when(mockedProperties).getTitle();
        doReturn(mockedProperties).when(mockedSpreadsheet).getProperties();
        doReturn(mockedSpreadsheet).when(googleSheetsAPI).getSpreadsheet("spreadsheet_id");

        Spreadsheet spreadsheet = sheetsHelper.getSpreadsheet("spreadsheet_id");

        assertEquals(mockedSpreadsheet, spreadsheet);
        assertBatchUpdateCalled(1);
    }

    @Test
    public void whenNewSpreadsheetDetected_shouldBatchUpdateBeCalled() throws IOException {
        doReturn(true).when(sheetsHelper).isNewSpreadsheet("spreadsheet_id", "Sheet1");
        sheetsHelper.updateSpreadsheetLocaleForNewSpreadsheet("spreadsheet_id", "Sheet1");
        assertBatchUpdateCalled(1);
    }

    @Test
    public void whenExistingSpreadsheetDetected_shouldNotBatchUpdateBeCalled() throws IOException {
        doReturn(false).when(sheetsHelper).isNewSpreadsheet("spreadsheet_id", "Sheet1");
        sheetsHelper.updateSpreadsheetLocaleForNewSpreadsheet("spreadsheet_id", "Sheet1");
        assertBatchUpdateCalled(0);
    }

    @Test
    public void whenThereAreNoCellsInTheMainSheet_shouldIsNewSpreadsheetReturnTrue() throws IOException {
        ValueRange valueRange = mock(ValueRange.class);
        when(valueRange.getValues()).thenReturn(null);
        when(googleSheetsAPI.getSpreadsheet("spreadsheet_id", "Sheet1")).thenReturn(valueRange);
        assertThat(sheetsHelper.isNewSpreadsheet("spreadsheet_id", "Sheet1"), is(true));

        when(valueRange.getValues()).thenReturn(new LinkedList<>());
        when(googleSheetsAPI.getSpreadsheet("spreadsheet_id", "Sheet1")).thenReturn(valueRange);
        assertThat(sheetsHelper.isNewSpreadsheet("spreadsheet_id", "Sheet1"), is(true));
    }

    @Test
    public void whenThereAreCellsInTheMainSheet_shouldIsNewSpreadsheetReturnFalse() throws IOException {
        ValueRange valueRange = mock(ValueRange.class);
        List<List<Object>> cells = new LinkedList<>();
        cells.add(new LinkedList<>());
        when(valueRange.getValues()).thenReturn(cells);
        when(googleSheetsAPI.getSpreadsheet("spreadsheet_id", "Sheet1")).thenReturn(valueRange);
        assertThat(sheetsHelper.isNewSpreadsheet("spreadsheet_id", "Sheet1"), is(false));
    }

    private void assertBatchUpdateCalled(int timesInvocations) throws IOException {
        verify(googleSheetsAPI, times(timesInvocations)).batchUpdate(anyString(), ArgumentMatchers.<Request>anyList());
    }
}
