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

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.javarosa.form.api.FormEntryPrompt;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.content.res.Configuration.SCREENLAYOUT_SIZE_LARGE;
import static android.content.res.Configuration.SCREENLAYOUT_SIZE_NORMAL;
import static android.content.res.Configuration.SCREENLAYOUT_SIZE_SMALL;
import static android.content.res.Configuration.SCREENLAYOUT_SIZE_XLARGE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class AppearancesTest {
    private final FormEntryPrompt formEntryPrompt = mock(FormEntryPrompt.class);

    @Test
    public void whenPromptDoesNotHaveAppearance_getSanitizedAppearanceHint_returnsNoAppearance() {
        assertEquals(Appearances.getSanitizedAppearanceHint(formEntryPrompt), Appearances.NO_APPEARANCE);
    }

    @Test
    public void whenPromptHasAppearance_getSanitizedAppearanceHint_returnsFormattedAppearance() {
        when(formEntryPrompt.getAppearanceHint()).thenReturn("BLAH");
        assertEquals(Appearances.getSanitizedAppearanceHint(formEntryPrompt), "blah");

        when(formEntryPrompt.getAppearanceHint()).thenReturn("blah");
        assertEquals(Appearances.getSanitizedAppearanceHint(formEntryPrompt), "blah");
    }

    @Test
    public void hasAppearance_returnsFalse_whenSanitizedAppearanceIsNotFound() {
        assertFalse(Appearances.hasAppearance(formEntryPrompt, "blah"));
    }

    @Test
    public void hasAppearance_returnsTrue_whenSanitizedAppearanceIsFound() {
        when(formEntryPrompt.getAppearanceHint()).thenReturn("BLAH");
        assertTrue(Appearances.hasAppearance(formEntryPrompt, "blah"));

        when(formEntryPrompt.getAppearanceHint()).thenReturn("blah");
        assertTrue(Appearances.hasAppearance(formEntryPrompt, "blah"));
    }

    @Test
    public void getNumberOfColumnsForColumnsNAppearanceTest() {
        when(formEntryPrompt.getAppearanceHint()).thenReturn("");
        assertEquals(1, Appearances.getNumberOfColumns(formEntryPrompt, null));

        when(formEntryPrompt.getAppearanceHint()).thenReturn("columns-2");
        assertEquals(2, Appearances.getNumberOfColumns(formEntryPrompt, null));

        when(formEntryPrompt.getAppearanceHint()).thenReturn("blah columns-10");
        assertEquals(10, Appearances.getNumberOfColumns(formEntryPrompt, null));

        when(formEntryPrompt.getAppearanceHint()).thenReturn("columns-10 quick");
        assertEquals(10, Appearances.getNumberOfColumns(formEntryPrompt, null));

        when(formEntryPrompt.getAppearanceHint()).thenReturn("columns-5 autocomplete");
        assertEquals(5, Appearances.getNumberOfColumns(formEntryPrompt, null));

        when(formEntryPrompt.getAppearanceHint()).thenReturn("columns-10quick");
        assertEquals(1, Appearances.getNumberOfColumns(formEntryPrompt, null));

        when(formEntryPrompt.getAppearanceHint()).thenReturn("columns-5autocomplete");
        assertEquals(1, Appearances.getNumberOfColumns(formEntryPrompt, null));

        when(formEntryPrompt.getAppearanceHint()).thenReturn("columns-5 ");
        assertEquals(5, Appearances.getNumberOfColumns(formEntryPrompt, null));

        when(formEntryPrompt.getAppearanceHint()).thenReturn("columns-5  ");
        assertEquals(5, Appearances.getNumberOfColumns(formEntryPrompt, null));

        when(formEntryPrompt.getAppearanceHint()).thenReturn("  columns-5");
        assertEquals(5, Appearances.getNumberOfColumns(formEntryPrompt, null));

        when(formEntryPrompt.getAppearanceHint()).thenReturn("quick columns-5");
        assertEquals(5, Appearances.getNumberOfColumns(formEntryPrompt, null));

        when(formEntryPrompt.getAppearanceHint()).thenReturn("compact-5");
        assertEquals(5, Appearances.getNumberOfColumns(formEntryPrompt, null));

        when(formEntryPrompt.getAppearanceHint()).thenReturn("compact-9");
        assertEquals(9, Appearances.getNumberOfColumns(formEntryPrompt, null));

        when(formEntryPrompt.getAppearanceHint()).thenReturn("columns-9");
        assertEquals(9, Appearances.getNumberOfColumns(formEntryPrompt, null));

        when(formEntryPrompt.getAppearanceHint()).thenReturn("columns--1");
        assertEquals(1, Appearances.getNumberOfColumns(formEntryPrompt, null));

        when(formEntryPrompt.getAppearanceHint()).thenReturn("columns--10");
        assertEquals(1, Appearances.getNumberOfColumns(formEntryPrompt, null));
    }

    @Test
    public void getNumberOfColumnsForColumnsAppearanceTest() {
        ScreenUtils screenUtils = mock(ScreenUtils.class);
        when(screenUtils.getScreenSizeConfiguration()).thenReturn(SCREENLAYOUT_SIZE_SMALL);

        when(formEntryPrompt.getAppearanceHint()).thenReturn("");
        assertEquals(1, Appearances.getNumberOfColumns(formEntryPrompt, screenUtils));

        when(formEntryPrompt.getAppearanceHint()).thenReturn("blah columns blah");
        assertEquals(2, Appearances.getNumberOfColumns(formEntryPrompt, screenUtils));

        when(screenUtils.getScreenSizeConfiguration()).thenReturn(SCREENLAYOUT_SIZE_NORMAL);
        assertEquals(3, Appearances.getNumberOfColumns(formEntryPrompt, screenUtils));

        when(screenUtils.getScreenSizeConfiguration()).thenReturn(SCREENLAYOUT_SIZE_LARGE);
        assertEquals(4, Appearances.getNumberOfColumns(formEntryPrompt, screenUtils));

        when(screenUtils.getScreenSizeConfiguration()).thenReturn(SCREENLAYOUT_SIZE_XLARGE);
        assertEquals(5, Appearances.getNumberOfColumns(formEntryPrompt, screenUtils));

        when(screenUtils.getScreenSizeConfiguration()).thenReturn(99999);
        assertEquals(3, Appearances.getNumberOfColumns(formEntryPrompt, screenUtils));
    }

    @Test
    public void isNoButtonsAppearance_returnsFalse_whenNoButtonsAppearanceIsNotFound() {
        assertFalse(Appearances.isNoButtonsAppearance(formEntryPrompt));
    }

    @Test
    public void isNoButtonsAppearance_returnsTrue_whenNoButtonsAppearanceIsFound() {
        when(formEntryPrompt.getAppearanceHint()).thenReturn("NO-BUTTONS");
        assertTrue(Appearances.isNoButtonsAppearance(formEntryPrompt));

        when(formEntryPrompt.getAppearanceHint()).thenReturn("no-buttons");
        assertTrue(Appearances.isNoButtonsAppearance(formEntryPrompt));

        when(formEntryPrompt.getAppearanceHint()).thenReturn("No-buttonsCompact");
        assertTrue(Appearances.isNoButtonsAppearance(formEntryPrompt));
    }

    @Test
    public void isCompactAppearance_returnsFalse_whenCompactAppearanceIsNotFound() {
        assertFalse(Appearances.isCompactAppearance(formEntryPrompt));
    }

    @Test
    public void isCompactAppearance_returnsTrue_whenCompactAppearanceIsFound() {
        when(formEntryPrompt.getAppearanceHint()).thenReturn("COMPACT");
        assertTrue(Appearances.isCompactAppearance(formEntryPrompt));

        when(formEntryPrompt.getAppearanceHint()).thenReturn("compact");
        assertTrue(Appearances.isCompactAppearance(formEntryPrompt));

        when(formEntryPrompt.getAppearanceHint()).thenReturn("CompactCompact");
        assertTrue(Appearances.isCompactAppearance(formEntryPrompt));
    }

    @Test
    public void useThousandSeparator_returnsFalse_whenThousandSepAppearanceIsNotFound() {
        assertFalse(Appearances.useThousandSeparator(formEntryPrompt));
    }

    @Test
    public void useThousandSeparator_returnsTrue_whenThousandSepAppearanceIsFound() {
        when(formEntryPrompt.getAppearanceHint()).thenReturn("THOUSANDS-SEP");
        assertTrue(Appearances.useThousandSeparator(formEntryPrompt));

        when(formEntryPrompt.getAppearanceHint()).thenReturn("thousands-sep");
        assertTrue(Appearances.useThousandSeparator(formEntryPrompt));

        when(formEntryPrompt.getAppearanceHint()).thenReturn("Thousands-sepCompact");
        assertTrue(Appearances.useThousandSeparator(formEntryPrompt));
    }

    @Test
    public void isFrontCameraAppearance_returnsFalse_whenFrontCameraAppearanceIsNotFound() {
        assertFalse(Appearances.isFrontCameraAppearance(formEntryPrompt));
    }

    @Test
    public void isFrontCameraAppearance_returnsTrue_whenFrontAppearanceIsFound() {
        when(formEntryPrompt.getAppearanceHint()).thenReturn("FRONT");
        assertTrue(Appearances.isFrontCameraAppearance(formEntryPrompt));

        when(formEntryPrompt.getAppearanceHint()).thenReturn("front");
        assertTrue(Appearances.isFrontCameraAppearance(formEntryPrompt));

        when(formEntryPrompt.getAppearanceHint()).thenReturn("FrontCompact");
        assertTrue(Appearances.isFrontCameraAppearance(formEntryPrompt));
    }

    @Test
    public void isFrontCameraAppearance_returnsTrue_whenNewFrontAppearanceIsFound() {
        when(formEntryPrompt.getAppearanceHint()).thenReturn("NEW-FRONT");
        assertTrue(Appearances.isFrontCameraAppearance(formEntryPrompt));

        when(formEntryPrompt.getAppearanceHint()).thenReturn("new-front");
        assertTrue(Appearances.isFrontCameraAppearance(formEntryPrompt));

        when(formEntryPrompt.getAppearanceHint()).thenReturn("New-frontCompact");
        assertTrue(Appearances.isFrontCameraAppearance(formEntryPrompt));
    }

    @Test
    public void isFrontCameraAppearance_returnsTrue_whenSelfieAppearanceIsFound() {
        when(formEntryPrompt.getAppearanceHint()).thenReturn("SELFIE");
        assertTrue(Appearances.isFrontCameraAppearance(formEntryPrompt));

        when(formEntryPrompt.getAppearanceHint()).thenReturn("selfie");
        assertTrue(Appearances.isFrontCameraAppearance(formEntryPrompt));

        when(formEntryPrompt.getAppearanceHint()).thenReturn("SelfieCompact");
        assertTrue(Appearances.isFrontCameraAppearance(formEntryPrompt));
    }

    @Test
    public void isFlexAppearance_returnsFalse_whenFlexAppearanceIsNotFound() {
        assertFalse(Appearances.isFlexAppearance(formEntryPrompt));
    }

    @Test
    public void isFlexAppearance_returnsFalse_whenAppearanceContainsCompactN() {
        when(formEntryPrompt.getAppearanceHint()).thenReturn("BLAH COMPACT-N_BLAH");
        assertFalse(Appearances.isFlexAppearance(formEntryPrompt));

        when(formEntryPrompt.getAppearanceHint()).thenReturn("blah compact-n_blah");
        assertFalse(Appearances.isFlexAppearance(formEntryPrompt));
    }

    @Test
    public void isFlexAppearance_returnsTrue_whenAppearanceContainsCompact() {
        when(formEntryPrompt.getAppearanceHint()).thenReturn("BLAH COMPACT_BLAH");
        assertTrue(Appearances.isFlexAppearance(formEntryPrompt));

        when(formEntryPrompt.getAppearanceHint()).thenReturn("blah compact_blah");
        assertTrue(Appearances.isFlexAppearance(formEntryPrompt));
    }

    @Test
    public void isFlexAppearance_returnsTrue_whenAppearanceContainsQuickCompact() {
        when(formEntryPrompt.getAppearanceHint()).thenReturn("BLAH QUICKCOMPACT_BLAH");
        assertTrue(Appearances.isFlexAppearance(formEntryPrompt));

        when(formEntryPrompt.getAppearanceHint()).thenReturn("blah quickcompact_blah");
        assertTrue(Appearances.isFlexAppearance(formEntryPrompt));
    }

    @Test
    public void isFlexAppearance_returnsTrue_whenAppearanceContainsColumnsPack() {
        when(formEntryPrompt.getAppearanceHint()).thenReturn("BLAH COLUMNS-PACK_BLAH");
        assertTrue(Appearances.isFlexAppearance(formEntryPrompt));

        when(formEntryPrompt.getAppearanceHint()).thenReturn("blah columns-pack_blah");
        assertTrue(Appearances.isFlexAppearance(formEntryPrompt));
    }
}
