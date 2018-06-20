package org.odk.collect.android.widgets.warnings;

import com.google.common.collect.Lists;

import org.javarosa.core.model.SelectChoice;
import org.junit.Before;
import org.junit.Test;
import org.odk.collect.android.widgets.warnings.SpacesInUnderlyingValuesWarning.UnderlyingValuesChecker;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.odk.collect.android.widgets.warnings.SpacesInUnderlyingValuesWarning.SpacesInUnderlyingValues;

public class SpacesInUnderlyingValuesTest {

    private UnderlyingValuesChecker subject;

    @Before
    public void setUp() {
        subject = new SpacesInUnderlyingValues();
    }

    @Test
    public void doesNotDetectErrorWhenThereIsNone() {
        List<SelectChoice> items = Lists.newArrayList(
                new SelectChoice("label", "no_space")
        );

        subject.check(items);
        assertFalse(subject.hasInvalidValues());
    }

    @Test
    public void doesNotDetectErrorInEmptySet() {
        List<SelectChoice> items = Lists.newArrayList();

        subject.check(items);
        assertFalse(subject.hasInvalidValues());
    }

    @Test
    public void doesDetectSingleSpaceError() {
        List<SelectChoice> items = Lists.newArrayList(
                new SelectChoice("label", "with space")
        );

        subject.check(items);
        assertTrue(subject.hasInvalidValues());
    }

    @Test
    public void detectsMultipleErrors() {
        List<SelectChoice> items = Lists.newArrayList(
                new SelectChoice("label", "with space"),
                new SelectChoice("label2", "with space2")
        );

        subject.check(items);
        assertTrue(subject.hasInvalidValues());
    }

    @Test
    public void returnsInvalidValues() {
        List<SelectChoice> items = Lists.newArrayList(
                new SelectChoice("label", "with space"),
                new SelectChoice("label2", "with space2")
        );

        subject.check(items);
        assertEquals(subject.getInvalidValues().size(), 2);
    }

    @Test
    public void detectsSpaceInTheBeginningOfUnderlyingValue() {
        List<SelectChoice> items = Lists.newArrayList(
                new SelectChoice("label", " before")
        );

        subject.check(items);
        assertTrue(subject.hasInvalidValues());
    }

    @Test
    public void detectsSpaceInTheEndOfUnderlyingValue() {
        List<SelectChoice> items = Lists.newArrayList(
                new SelectChoice("label", "after ")
        );

        subject.check(items);
        assertTrue(subject.hasInvalidValues());
    }
}