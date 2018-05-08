package org.odk.collect.android.utilities.gdrive;

import com.google.api.services.sheets.v4.model.Request;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.SpreadsheetProperties;
import com.google.api.services.sheets.v4.model.ValueRange;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;

import static junit.framework.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * @author Shobhit Agarwal
 */

@RunWith(PowerMockRunner.class)
@PrepareForTest({Spreadsheet.class, SpreadsheetProperties.class})
public class SheetsHelperTest {

    @Mock
    private SheetsHelper.SheetsService sheetsService;

    private SheetsHelper sheetsHelper;

    @Before
    public void setup() {
        sheetsHelper = spy(new SheetsHelper(sheetsService));
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
        verify(sheetsService).insertRow("spreadsheet_id", "sheet_name", valueRange);
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateRowShouldThrowErrorWhenValueRangeIsNull() throws IOException {
        sheetsHelper.updateRow("spreadsheet_id", "sheet_name", null);
    }

    @Test
    public void updateRowTest() throws IOException {
        ValueRange valueRange = new ValueRange();
        sheetsHelper.updateRow("spreadsheet_id", "sheet_name!A1", valueRange);
        verify(sheetsService).updateRow("spreadsheet_id", "sheet_name!A1", valueRange);
    }

    @Test
    public void getSpreadsheetTest() throws IOException {
        Spreadsheet mockedSpreadsheet = mock(Spreadsheet.class);
        SpreadsheetProperties mockedProperties = mock(SpreadsheetProperties.class);

        doReturn("sheet_title").when(mockedProperties).getTitle();
        doReturn(mockedProperties).when(mockedSpreadsheet).getProperties();
        doReturn(mockedSpreadsheet).when(sheetsService).getSpreadsheet("spreadsheet_id");

        Spreadsheet spreadsheet = sheetsHelper.getSpreadsheet("spreadsheet_id");

        assertEquals(mockedSpreadsheet, spreadsheet);
        assertBatchUpdateCalled(1);
    }

    private void assertBatchUpdateCalled(int timesInvocations) throws IOException {
        verify(sheetsService, times(timesInvocations)).batchUpdate(anyString(), ArgumentMatchers.<Request>anyList());
    }
}
