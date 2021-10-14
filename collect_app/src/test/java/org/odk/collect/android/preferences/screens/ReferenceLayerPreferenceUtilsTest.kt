package org.odk.collect.android.preferences.screens

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.odk.collect.android.geo.DirectoryReferenceLayerRepository
import org.odk.collect.android.preferences.CaptionedListPreference
import org.odk.collect.shared.TempFiles

@RunWith(AndroidJUnit4::class)
class ReferenceLayerPreferenceUtilsTest {

    @Test
    fun populateReferenceLayerPref_whenPrefValueNotInReferenceLayers_clearsPref() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val tempDirPath = TempFiles.createTempDir().absolutePath
        val referenceLayerRepository = DirectoryReferenceLayerRepository(tempDirPath)

        // Use mock to avoid explosions constructing pref in Robolectric
        val pref = mock<CaptionedListPreference> {
            on { getValue() } doReturn "something"
        }

        ReferenceLayerPreferenceUtils.populateReferenceLayerPref(
            context,
            referenceLayerRepository,
            pref
        )

        verify(pref).setValue(null)
    }
}
