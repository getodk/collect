/*
 * Copyright 2019 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.odk.collect.android.utilities;

import org.javarosa.form.api.FormEntryPrompt;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class WidgetAppearanceUtilsTest {
    private final FormEntryPrompt formEntryPrompt = mock(FormEntryPrompt.class);

    @Test
    public void whenPromptDoesNotHaveAppearance_getSanitizedAppearanceHint_returnsNoAppearance() {
        assertEquals(WidgetAppearanceUtils.getSanitizedAppearanceHint(formEntryPrompt), WidgetAppearanceUtils.NO_APPEARANCE);
    }

    @Test
    public void whenPromptHasAppearance_getSanitizedAppearanceHint_returnsFormattedAppearance() {
        when(formEntryPrompt.getAppearanceHint()).thenReturn("BLAH");
        assertEquals(WidgetAppearanceUtils.getSanitizedAppearanceHint(formEntryPrompt), "blah");

        when(formEntryPrompt.getAppearanceHint()).thenReturn("blah");
        assertEquals(WidgetAppearanceUtils.getSanitizedAppearanceHint(formEntryPrompt), "blah");
    }

    @Test
    public void hasAppearance_returnsFalse_whenSanitizedAppearanceIsNotFound() {
        assertFalse(WidgetAppearanceUtils.hasAppearance(formEntryPrompt, "blah"));
    }

    @Test
    public void hasAppearance_returnsTrue_whenSanitizedAppearanceIsFound() {
        when(formEntryPrompt.getAppearanceHint()).thenReturn("BLAH");
        assertTrue(WidgetAppearanceUtils.hasAppearance(formEntryPrompt, "blah"));

        when(formEntryPrompt.getAppearanceHint()).thenReturn("blah");
        assertTrue(WidgetAppearanceUtils.hasAppearance(formEntryPrompt, "blah"));
    }

    @Test
    public void getNumberOfColumnsTest() {
        when(formEntryPrompt.getAppearanceHint()).thenReturn("");
        assertEquals(1, WidgetAppearanceUtils.getNumberOfColumns(formEntryPrompt, null));

        when(formEntryPrompt.getAppearanceHint()).thenReturn("columns-2");
        assertEquals(2, WidgetAppearanceUtils.getNumberOfColumns(formEntryPrompt, null));

        when(formEntryPrompt.getAppearanceHint()).thenReturn("columns-10");
        assertEquals(10, WidgetAppearanceUtils.getNumberOfColumns(formEntryPrompt, null));

        when(formEntryPrompt.getAppearanceHint()).thenReturn("columns-10 quick");
        assertEquals(10, WidgetAppearanceUtils.getNumberOfColumns(formEntryPrompt, null));

        when(formEntryPrompt.getAppearanceHint()).thenReturn("columns-5 autocomplete");
        assertEquals(5, WidgetAppearanceUtils.getNumberOfColumns(formEntryPrompt, null));

        when(formEntryPrompt.getAppearanceHint()).thenReturn("columns-10quick");
        assertEquals(1, WidgetAppearanceUtils.getNumberOfColumns(formEntryPrompt, null));

        when(formEntryPrompt.getAppearanceHint()).thenReturn("columns-5autocomplete");
        assertEquals(1, WidgetAppearanceUtils.getNumberOfColumns(formEntryPrompt, null));

        when(formEntryPrompt.getAppearanceHint()).thenReturn("columns-5 ");
        assertEquals(5, WidgetAppearanceUtils.getNumberOfColumns(formEntryPrompt, null));

        when(formEntryPrompt.getAppearanceHint()).thenReturn("columns-5  ");
        assertEquals(5, WidgetAppearanceUtils.getNumberOfColumns(formEntryPrompt, null));

        when(formEntryPrompt.getAppearanceHint()).thenReturn("  columns-5");
        assertEquals(5, WidgetAppearanceUtils.getNumberOfColumns(formEntryPrompt, null));

        when(formEntryPrompt.getAppearanceHint()).thenReturn("quick columns-5");
        assertEquals(5, WidgetAppearanceUtils.getNumberOfColumns(formEntryPrompt, null));

        when(formEntryPrompt.getAppearanceHint()).thenReturn("compact-5");
        assertEquals(5, WidgetAppearanceUtils.getNumberOfColumns(formEntryPrompt, null));

        when(formEntryPrompt.getAppearanceHint()).thenReturn("compact-9");
        assertEquals(9, WidgetAppearanceUtils.getNumberOfColumns(formEntryPrompt, null));

        when(formEntryPrompt.getAppearanceHint()).thenReturn("columns-9");
        assertEquals(9, WidgetAppearanceUtils.getNumberOfColumns(formEntryPrompt, null));

        when(formEntryPrompt.getAppearanceHint()).thenReturn("columns--1");
        assertEquals(1, WidgetAppearanceUtils.getNumberOfColumns(formEntryPrompt, null));

        when(formEntryPrompt.getAppearanceHint()).thenReturn("columns--10");
        assertEquals(1, WidgetAppearanceUtils.getNumberOfColumns(formEntryPrompt, null));
    }

    @Test
    public void isNoButtonsAppearance_returnsFalse_whenNoButtonsAppearanceIsNotFound() {
        assertFalse(WidgetAppearanceUtils.isNoButtonsAppearance(formEntryPrompt));
    }

    @Test
    public void isNoButtonsAppearance_returnsTrue_whenNoButtonsAppearanceIsFound() {
        when(formEntryPrompt.getAppearanceHint()).thenReturn("NO-BUTTONS");
        assertTrue(WidgetAppearanceUtils.isNoButtonsAppearance(formEntryPrompt));

        when(formEntryPrompt.getAppearanceHint()).thenReturn("no-buttons");
        assertTrue(WidgetAppearanceUtils.isNoButtonsAppearance(formEntryPrompt));

        when(formEntryPrompt.getAppearanceHint()).thenReturn("No-buttonsCompact");
        assertTrue(WidgetAppearanceUtils.isNoButtonsAppearance(formEntryPrompt));
    }

    @Test
    public void isCompactAppearance_returnsFalse_whenCompactAppearanceIsNotFound() {
        assertFalse(WidgetAppearanceUtils.isCompactAppearance(formEntryPrompt));
    }

    @Test
    public void isCompactAppearance_returnsTrue_whenCompactAppearanceIsFound() {
        when(formEntryPrompt.getAppearanceHint()).thenReturn("COMPACT");
        assertTrue(WidgetAppearanceUtils.isCompactAppearance(formEntryPrompt));

        when(formEntryPrompt.getAppearanceHint()).thenReturn("compact");
        assertTrue(WidgetAppearanceUtils.isCompactAppearance(formEntryPrompt));

        when(formEntryPrompt.getAppearanceHint()).thenReturn("CompactCompact");
        assertTrue(WidgetAppearanceUtils.isCompactAppearance(formEntryPrompt));
    }

    @Test
    public void useThousandSeparator_returnsFalse_whenThousandSepAppearanceIsNotFound() {
        assertFalse(WidgetAppearanceUtils.useThousandSeparator(formEntryPrompt));
    }

    @Test
    public void useThousandSeparator_returnsTrue_whenThousandSepAppearanceIsFound() {
        when(formEntryPrompt.getAppearanceHint()).thenReturn("THOUSANDS-SEP");
        assertTrue(WidgetAppearanceUtils.useThousandSeparator(formEntryPrompt));

        when(formEntryPrompt.getAppearanceHint()).thenReturn("thousands-sep");
        assertTrue(WidgetAppearanceUtils.useThousandSeparator(formEntryPrompt));

        when(formEntryPrompt.getAppearanceHint()).thenReturn("Thousands-sepCompact");
        assertTrue(WidgetAppearanceUtils.useThousandSeparator(formEntryPrompt));
    }

    @Test
    public void isFrontCameraAppearance_returnsFalse_whenFrontCameraAppearanceIsNotFound() {
        assertFalse(WidgetAppearanceUtils.isFrontCameraAppearance(formEntryPrompt));
    }

    @Test
    public void isFrontCameraAppearance_returnsTrue_whenFrontAppearanceIsFound() {
        when(formEntryPrompt.getAppearanceHint()).thenReturn("FRONT");
        assertTrue(WidgetAppearanceUtils.isFrontCameraAppearance(formEntryPrompt));

        when(formEntryPrompt.getAppearanceHint()).thenReturn("front");
        assertTrue(WidgetAppearanceUtils.isFrontCameraAppearance(formEntryPrompt));

        when(formEntryPrompt.getAppearanceHint()).thenReturn("FrontCompact");
        assertTrue(WidgetAppearanceUtils.isFrontCameraAppearance(formEntryPrompt));
    }

    @Test
    public void isFrontCameraAppearance_returnsTrue_whenNewFrontAppearanceIsFound() {
        when(formEntryPrompt.getAppearanceHint()).thenReturn("NEW-FRONT");
        assertTrue(WidgetAppearanceUtils.isFrontCameraAppearance(formEntryPrompt));

        when(formEntryPrompt.getAppearanceHint()).thenReturn("new-front");
        assertTrue(WidgetAppearanceUtils.isFrontCameraAppearance(formEntryPrompt));

        when(formEntryPrompt.getAppearanceHint()).thenReturn("New-frontCompact");
        assertTrue(WidgetAppearanceUtils.isFrontCameraAppearance(formEntryPrompt));
    }

    @Test
    public void isFrontCameraAppearance_returnsTrue_whenSelfieAppearanceIsFound() {
        when(formEntryPrompt.getAppearanceHint()).thenReturn("SELFIE");
        assertTrue(WidgetAppearanceUtils.isFrontCameraAppearance(formEntryPrompt));

        when(formEntryPrompt.getAppearanceHint()).thenReturn("selfie");
        assertTrue(WidgetAppearanceUtils.isFrontCameraAppearance(formEntryPrompt));

        when(formEntryPrompt.getAppearanceHint()).thenReturn("SelfieCompact");
        assertTrue(WidgetAppearanceUtils.isFrontCameraAppearance(formEntryPrompt));
    }

    @Test
    public void isFlexAppearance_returnsFalse_whenFlexAppearanceIsNotFound() {
        assertFalse(WidgetAppearanceUtils.isFlexAppearance(formEntryPrompt));
    }

    @Test
    public void isFlexAppearance_returnsFalse_whenAppearanceStartsWithCompactN() {
        when(formEntryPrompt.getAppearanceHint()).thenReturn("COMPACT-N_BLAH");
        assertFalse(WidgetAppearanceUtils.isFlexAppearance(formEntryPrompt));

        when(formEntryPrompt.getAppearanceHint()).thenReturn("compact-n_blah");
        assertFalse(WidgetAppearanceUtils.isFlexAppearance(formEntryPrompt));
    }

    @Test
    public void isFlexAppearance_returnsTrue_whenAppearanceStartsWithCompact() {
        when(formEntryPrompt.getAppearanceHint()).thenReturn("COMPACT_BLAH");
        assertTrue(WidgetAppearanceUtils.isFlexAppearance(formEntryPrompt));

        when(formEntryPrompt.getAppearanceHint()).thenReturn("compact_blah");
        assertTrue(WidgetAppearanceUtils.isFlexAppearance(formEntryPrompt));
    }

    @Test
    public void isFlexAppearance_returnsTrue_whenAppearanceStartsWithQuickCompact() {
        when(formEntryPrompt.getAppearanceHint()).thenReturn("QUICKCOMPACT_BLAH");
        assertTrue(WidgetAppearanceUtils.isFlexAppearance(formEntryPrompt));

        when(formEntryPrompt.getAppearanceHint()).thenReturn("quickcompact_blah");
        assertTrue(WidgetAppearanceUtils.isFlexAppearance(formEntryPrompt));
    }

    @Test
    public void isFlexAppearance_returnsTrue_whenAppearanceStartsWithColumnsPack() {
        when(formEntryPrompt.getAppearanceHint()).thenReturn("COLUMNS-PACK_BLAH");
        assertTrue(WidgetAppearanceUtils.isFlexAppearance(formEntryPrompt));

        when(formEntryPrompt.getAppearanceHint()).thenReturn("columns-pack_blah");
        assertTrue(WidgetAppearanceUtils.isFlexAppearance(formEntryPrompt));
    }
}
