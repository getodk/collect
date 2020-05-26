package org.odk.collect.android.widgets;

import android.view.View;

import androidx.annotation.NonNull;

import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.DateData;
import org.joda.time.LocalDateTime;
import org.junit.Test;
import org.mockito.Mock;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.widgets.base.GeneralDateTimeWidgetTest;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.when;

public class DateWidgetTest extends GeneralDateTimeWidgetTest<DateWidget, DateData> {

    @Mock
    QuestionDef questionDef;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        when(formEntryPrompt.getQuestion()).thenReturn(questionDef);
        when(questionDef.getAppearanceAttr()).thenReturn("");
    }

    @NonNull
    @Override
    public DateWidget createWidget() {
        return new DateWidget(activity, new QuestionDetails(formEntryPrompt, "formAnalyticsID"));
    }

    @NonNull
    @Override
    public DateData getNextAnswer() {
        return new DateData(getNextDateTime().toDate());
    }

    @Override
    public DateData getInitialAnswer() {
        return getNextAnswer();
    }

    @Test
    public void setData() {
        DateWidget widget = getSpyWidget();
        LocalDateTime date = new LocalDateTime().withYear(2010).withMonthOfYear(5).withDayOfMonth(12);
        widget.setBinaryData(date);
        assertFalse(widget.isWaitingForData());
        assertFalse(widget.isNullAnswer);
        assertEquals(widget.getAnswer().getDisplayText(), new DateData(date.toDate()).getDisplayText());
    }

    @Test
    public void usingReadOnlyOptionShouldMakeAllClickableElementsDisabled() {
        when(formEntryPrompt.isReadOnly()).thenReturn(true);

        assertThat(getSpyWidget().dateButton.getVisibility(), is(View.GONE));
    }
}