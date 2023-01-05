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
package org.odk.collect.android.utilities

import android.content.res.Configuration
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import org.javarosa.form.api.FormEntryPrompt
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.odk.collect.androidshared.utils.ScreenUtils

class AppearancesTest {
    private val formEntryPrompt = mock<FormEntryPrompt>()
    private val screenUtils = mock<ScreenUtils>()

    @Test
    fun `getSanitizedAppearanceHint returns an empty string if there is no appearance`() {
        assertEquals(Appearances.getSanitizedAppearanceHint(formEntryPrompt), Appearances.NO_APPEARANCE)

        whenever(formEntryPrompt.appearanceHint).thenReturn("")
        assertEquals(Appearances.getSanitizedAppearanceHint(formEntryPrompt), Appearances.NO_APPEARANCE)
    }

    @Test
    fun `getSanitizedAppearanceHint returns lowercase appearance`() {
        whenever(formEntryPrompt.appearanceHint).thenReturn("BLAH")
        assertEquals(Appearances.getSanitizedAppearanceHint(formEntryPrompt), "blah")
    }

    @Test
    fun `getSanitizedAppearanceHint ignores search function`() {
        whenever(formEntryPrompt.appearanceHint).thenReturn("blah search('fruits') blah")
        assertEquals(Appearances.getSanitizedAppearanceHint(formEntryPrompt), "blah  blah")
    }

    @Test
    fun `hasAppearance returns false if there is no appearance`() {
        assertFalse(Appearances.hasAppearance(formEntryPrompt, "blah"))

        whenever(formEntryPrompt.appearanceHint).thenReturn("")
        assertFalse(Appearances.hasAppearance(formEntryPrompt, "blah"))
    }

    @Test
    fun `hasAppearance returns false if given appearance is not found`() {
        whenever(formEntryPrompt.appearanceHint).thenReturn("something else something")
        assertFalse(Appearances.hasAppearance(formEntryPrompt, "blah"))
    }

    @Test
    fun `hasAppearance returns true if given appearance is found`() {
        whenever(formEntryPrompt.appearanceHint).thenReturn("something BLAH something")
        assertTrue(Appearances.hasAppearance(formEntryPrompt, "blah"))
    }

    @Test
    fun `getNumberOfColumns returns 1 if there is no appearance`() {
        assertEquals(1, Appearances.getNumberOfColumns(formEntryPrompt, mock()))

        whenever(formEntryPrompt.appearanceHint).thenReturn("")
        assertEquals(1, Appearances.getNumberOfColumns(formEntryPrompt, mock()))
    }

    @Test
    fun `getNumberOfColumns returns a correct number for valid appearances`() {
        whenever(formEntryPrompt.appearanceHint).thenReturn("columns-2")
        assertEquals(2, Appearances.getNumberOfColumns(formEntryPrompt, mock()))

        whenever(formEntryPrompt.appearanceHint).thenReturn("blah COLUMNS-10")
        assertEquals(10, Appearances.getNumberOfColumns(formEntryPrompt, mock()))

        whenever(formEntryPrompt.appearanceHint).thenReturn("columns-10 quick")
        assertEquals(10, Appearances.getNumberOfColumns(formEntryPrompt, mock()))

        whenever(formEntryPrompt.appearanceHint).thenReturn("columns-5 autocomplete")
        assertEquals(5, Appearances.getNumberOfColumns(formEntryPrompt, mock()))

        whenever(formEntryPrompt.appearanceHint).thenReturn("Columns-5 ")
        assertEquals(5, Appearances.getNumberOfColumns(formEntryPrompt, mock()))

        whenever(formEntryPrompt.appearanceHint).thenReturn("columns-5  ")
        assertEquals(5, Appearances.getNumberOfColumns(formEntryPrompt, mock()))

        whenever(formEntryPrompt.appearanceHint).thenReturn("  columns-5")
        assertEquals(5, Appearances.getNumberOfColumns(formEntryPrompt, mock()))

        whenever(formEntryPrompt.appearanceHint).thenReturn("quick columns-5")
        assertEquals(5, Appearances.getNumberOfColumns(formEntryPrompt, mock()))

        whenever(formEntryPrompt.appearanceHint).thenReturn("compact-5")
        assertEquals(5, Appearances.getNumberOfColumns(formEntryPrompt, mock()))

        whenever(formEntryPrompt.appearanceHint).thenReturn("COMPACT-9")
        assertEquals(9, Appearances.getNumberOfColumns(formEntryPrompt, mock()))

        whenever(formEntryPrompt.appearanceHint).thenReturn("columns-9")
        assertEquals(9, Appearances.getNumberOfColumns(formEntryPrompt, mock()))
    }

    @Test
    fun `getNumberOfColumns returns 1 for invalid appearances`() {
        whenever(formEntryPrompt.appearanceHint).thenReturn("columns-10quick")
        assertEquals(1, Appearances.getNumberOfColumns(formEntryPrompt, mock()))

        whenever(formEntryPrompt.appearanceHint).thenReturn("columns-5autocomplete")
        assertEquals(1, Appearances.getNumberOfColumns(formEntryPrompt, mock()))

        whenever(formEntryPrompt.appearanceHint).thenReturn("columns--1")
        assertEquals(1, Appearances.getNumberOfColumns(formEntryPrompt, mock()))

        whenever(formEntryPrompt.appearanceHint).thenReturn("columns--10")
        assertEquals(1, Appearances.getNumberOfColumns(formEntryPrompt, mock()))

        whenever(formEntryPrompt.appearanceHint).thenReturn("columns-")
        assertEquals(1, Appearances.getNumberOfColumns(formEntryPrompt, mock()))
    }

    @Test
    fun `getNumberOfColumns should return 2 for small screens if there is 'columns' appearance`() {
        whenever(formEntryPrompt.appearanceHint).thenReturn("columns")
        whenever(screenUtils.screenSizeConfiguration).thenReturn(Configuration.SCREENLAYOUT_SIZE_SMALL)
        assertEquals(2, Appearances.getNumberOfColumns(formEntryPrompt, screenUtils))
    }

    @Test
    fun `getNumberOfColumns should return 3 for normal screens if there is 'columns' appearance`() {
        whenever(formEntryPrompt.appearanceHint).thenReturn("columns")
        whenever(screenUtils.screenSizeConfiguration).thenReturn(Configuration.SCREENLAYOUT_SIZE_NORMAL)
        assertEquals(3, Appearances.getNumberOfColumns(formEntryPrompt, screenUtils))
    }

    @Test
    fun `getNumberOfColumns should return 4 for large screens if there is 'columns' appearance`() {
        whenever(formEntryPrompt.appearanceHint).thenReturn("columns")
        whenever(screenUtils.screenSizeConfiguration).thenReturn(Configuration.SCREENLAYOUT_SIZE_LARGE)
        assertEquals(4, Appearances.getNumberOfColumns(formEntryPrompt, screenUtils))
    }

    @Test
    fun `getNumberOfColumns should return 5 for extra large screens if there is 'columns' appearance`() {
        whenever(formEntryPrompt.appearanceHint).thenReturn("columns")
        whenever(screenUtils.screenSizeConfiguration).thenReturn(Configuration.SCREENLAYOUT_SIZE_XLARGE)
        assertEquals(5, Appearances.getNumberOfColumns(formEntryPrompt, screenUtils))
    }

    @Test
    fun `getNumberOfColumns should return 3 if there is 'columns' appearance and screen size can not be determined`() {
        whenever(formEntryPrompt.appearanceHint).thenReturn("columns")
        whenever(screenUtils.screenSizeConfiguration).thenReturn(99999)
        assertEquals(3, Appearances.getNumberOfColumns(formEntryPrompt, screenUtils))
    }

    @Test
    fun `isNoButtonsAppearance returns false when there is no appearance`() {
        assertFalse(Appearances.isNoButtonsAppearance(formEntryPrompt))

        whenever(formEntryPrompt.appearanceHint).thenReturn("")
        assertFalse(Appearances.isNoButtonsAppearance(formEntryPrompt))
    }

    @Test
    fun `isNoButtonsAppearance returns false when 'no-buttons' appearance is not found`() {
        whenever(formEntryPrompt.appearanceHint).thenReturn("blah")
        assertFalse(Appearances.isNoButtonsAppearance(formEntryPrompt))
    }

    @Test
    fun `isNoButtonsAppearance returns true when 'no-buttons' appearance is found`() {
        whenever(formEntryPrompt.appearanceHint).thenReturn("NO-BUTTONS")
        assertTrue(Appearances.isNoButtonsAppearance(formEntryPrompt))

        whenever(formEntryPrompt.appearanceHint).thenReturn("no-buttons")
        assertTrue(Appearances.isNoButtonsAppearance(formEntryPrompt))

        whenever(formEntryPrompt.appearanceHint).thenReturn("No-buttonsCompact")
        assertTrue(Appearances.isNoButtonsAppearance(formEntryPrompt))
    }

    @Test
    fun `isCompactAppearance returns false when there is no appearance`() {
        assertFalse(Appearances.isCompactAppearance(formEntryPrompt))

        whenever(formEntryPrompt.appearanceHint).thenReturn("")
        assertFalse(Appearances.isCompactAppearance(formEntryPrompt))
    }

    @Test
    fun `isCompactAppearance returns false when 'compact' appearance is not found`() {
        whenever(formEntryPrompt.appearanceHint).thenReturn("blah")
        assertFalse(Appearances.isCompactAppearance(formEntryPrompt))
    }

    @Test
    fun `isCompactAppearance returns true when 'compact' appearance is found`() {
        whenever(formEntryPrompt.appearanceHint).thenReturn("COMPACT")
        assertTrue(Appearances.isCompactAppearance(formEntryPrompt))

        whenever(formEntryPrompt.appearanceHint).thenReturn("compact")
        assertTrue(Appearances.isCompactAppearance(formEntryPrompt))

        whenever(formEntryPrompt.appearanceHint).thenReturn("CompactCompact")
        assertTrue(Appearances.isCompactAppearance(formEntryPrompt))
    }

    @Test
    fun `useThousandSeparator returns false when there is no appearance`() {
        assertFalse(Appearances.useThousandSeparator(formEntryPrompt))

        whenever(formEntryPrompt.appearanceHint).thenReturn("")
        assertFalse(Appearances.useThousandSeparator(formEntryPrompt))
    }

    @Test
    fun `useThousandSeparator returns false when 'thousands-sep' appearance is not found`() {
        whenever(formEntryPrompt.appearanceHint).thenReturn("")
        assertFalse(Appearances.useThousandSeparator(formEntryPrompt))
    }

    @Test
    fun `useThousandSeparator returns true when 'thousands-sep' appearance is found`() {
        whenever(formEntryPrompt.appearanceHint).thenReturn("THOUSANDS-SEP")
        assertTrue(Appearances.useThousandSeparator(formEntryPrompt))

        whenever(formEntryPrompt.appearanceHint).thenReturn("thousands-sep")
        assertTrue(Appearances.useThousandSeparator(formEntryPrompt))

        whenever(formEntryPrompt.appearanceHint).thenReturn("Thousands-sepCompact")
        assertTrue(Appearances.useThousandSeparator(formEntryPrompt))
    }

    @Test
    fun `isFrontCameraAppearance returns false when there is no appearance`() {
        assertFalse(Appearances.isFrontCameraAppearance(formEntryPrompt))

        whenever(formEntryPrompt.appearanceHint).thenReturn("")
        assertFalse(Appearances.isFrontCameraAppearance(formEntryPrompt))
    }

    @Test
    fun `isFrontCameraAppearance returns false when non of supported appearances is not found`() {
        whenever(formEntryPrompt.appearanceHint).thenReturn("blah")
        assertFalse(Appearances.isFrontCameraAppearance(formEntryPrompt))
    }

    @Test
    fun `isFrontCameraAppearance returns true when front appearance is found`() {
        whenever(formEntryPrompt.appearanceHint).thenReturn("FRONT")
        assertTrue(Appearances.isFrontCameraAppearance(formEntryPrompt))

        whenever(formEntryPrompt.appearanceHint).thenReturn("front")
        assertTrue(Appearances.isFrontCameraAppearance(formEntryPrompt))

        whenever(formEntryPrompt.appearanceHint).thenReturn("FrontCompact")
        assertTrue(Appearances.isFrontCameraAppearance(formEntryPrompt))
    }

    @Test
    fun `isFrontCameraAppearance returns true when new front appearance is found`() {
        whenever(formEntryPrompt.appearanceHint).thenReturn("NEW-FRONT")
        assertTrue(Appearances.isFrontCameraAppearance(formEntryPrompt))

        whenever(formEntryPrompt.appearanceHint).thenReturn("new-front")
        assertTrue(Appearances.isFrontCameraAppearance(formEntryPrompt))

        whenever(formEntryPrompt.appearanceHint).thenReturn("New-frontCompact")
        assertTrue(Appearances.isFrontCameraAppearance(formEntryPrompt))
    }

    @Test
    fun `isFrontCameraAppearance returns true when selfie appearance is found`() {
        whenever(formEntryPrompt.appearanceHint).thenReturn("SELFIE")
        assertTrue(Appearances.isFrontCameraAppearance(formEntryPrompt))

        whenever(formEntryPrompt.appearanceHint).thenReturn("selfie")
        assertTrue(Appearances.isFrontCameraAppearance(formEntryPrompt))

        whenever(formEntryPrompt.appearanceHint).thenReturn("SelfieCompact")
        assertTrue(Appearances.isFrontCameraAppearance(formEntryPrompt))
    }

    @Test
    fun `isFlexAppearance returns false when there is no appearance`() {
        assertFalse(Appearances.isFlexAppearance(formEntryPrompt))

        whenever(formEntryPrompt.appearanceHint).thenReturn("")
        assertFalse(Appearances.isFlexAppearance(formEntryPrompt))
    }

    @Test
    fun `isFlexAppearance returns false when appearance contains 'compact-n'`() {
        whenever(formEntryPrompt.appearanceHint).thenReturn("BLAH COMPACT-N_BLAH")
        assertFalse(Appearances.isFlexAppearance(formEntryPrompt))

        whenever(formEntryPrompt.appearanceHint).thenReturn("blah compact-n_blah")
        assertFalse(Appearances.isFlexAppearance(formEntryPrompt))
    }

    @Test
    fun `isFlexAppearance returns true when appearance contains 'compact'`() {
        whenever(formEntryPrompt.appearanceHint).thenReturn("BLAH COMPACT_BLAH")
        assertTrue(Appearances.isFlexAppearance(formEntryPrompt))

        whenever(formEntryPrompt.appearanceHint).thenReturn("blah compact_blah")
        assertTrue(Appearances.isFlexAppearance(formEntryPrompt))
    }

    @Test
    fun `isFlexAppearance returns true when appearance contains 'quickcompact'`() {
        whenever(formEntryPrompt.appearanceHint).thenReturn("BLAH QUICKCOMPACT_BLAH")
        assertTrue(Appearances.isFlexAppearance(formEntryPrompt))

        whenever(formEntryPrompt.appearanceHint).thenReturn("blah quickcompact_blah")
        assertTrue(Appearances.isFlexAppearance(formEntryPrompt))
    }

    @Test
    fun `isFlexAppearance returns true when appearance contains 'columns-pack'`() {
        whenever(formEntryPrompt.appearanceHint).thenReturn("BLAH COLUMNS-PACK_BLAH")
        assertTrue(Appearances.isFlexAppearance(formEntryPrompt))

        whenever(formEntryPrompt.appearanceHint).thenReturn("blah columns-pack_blah")
        assertTrue(Appearances.isFlexAppearance(formEntryPrompt))
    }

    @Test
    fun `isAutocomplete returns false when there is no appearance`() {
        assertFalse(Appearances.isAutocomplete(formEntryPrompt))

        whenever(formEntryPrompt.appearanceHint).thenReturn("")
        assertFalse(Appearances.isAutocomplete(formEntryPrompt))
    }

    @Test
    fun `isAutocomplete returns false when non of supported appearances is found`() {
        whenever(formEntryPrompt.appearanceHint).thenReturn("blah")
        assertFalse(Appearances.isAutocomplete(formEntryPrompt))
    }

    @Test
    fun `isAutocomplete returns true when 'autocomplete' or 'search' appearance is found`() {
        whenever(formEntryPrompt.appearanceHint).thenReturn("autocomplete")
        assertTrue(Appearances.isAutocomplete(formEntryPrompt))

        whenever(formEntryPrompt.appearanceHint).thenReturn("search")
        assertTrue(Appearances.isAutocomplete(formEntryPrompt))
    }
}
