package org.odk.collect.android.activities

import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.odk.collect.android.formmanagement.FormNavigator

class EditInstanceResultCallbackTest {

    @Test
    fun `onActivityResult does nothing when result is null`() {
        val formNavigator = mock<FormNavigator>()
        EditInstanceResultCallback(mock(), formNavigator).onActivityResult(null)
        verify(formNavigator, never()).editInstance(any(), any())
    }
}
