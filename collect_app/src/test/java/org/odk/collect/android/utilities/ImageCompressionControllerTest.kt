package org.odk.collect.android.utilities

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.javarosa.core.model.instance.TreeElement
import org.javarosa.form.api.FormEntryPrompt
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import org.odk.collect.android.widgets.QuestionWidget
import org.odk.collect.androidshared.bitmap.ImageCompressor

@RunWith(AndroidJUnit4::class)
class ImageCompressionControllerTest {

    private val context = ApplicationProvider.getApplicationContext<Application>()
    private val formEntryPrompt = mock<FormEntryPrompt>()
    private val questionWidget = mock<QuestionWidget>().also {
        whenever(it.formEntryPrompt).thenReturn(formEntryPrompt)
    }
    private val treeElement = mock<TreeElement>().also {
        whenever(it.attributeValue).thenReturn("123")
        whenever(it.name).thenReturn("max-pixels")
        whenever(it.namespace).thenReturn(ApplicationConstants.Namespaces.XML_OPENROSA_NAMESPACE)
    }
    private val imageCompressor = mock<ImageCompressor>()
    private val imageCompressionController = ImageCompressionController(imageCompressor)

    @Test
    fun `when 'max-pixels' is not specified on a form level and expected image size in setting is 'original_image_size', image compression should not be triggered`() {
        whenever(formEntryPrompt.bindAttributes).thenReturn(emptyList())

        imageCompressionController.execute("/path", questionWidget, context, "original_image_size")

        verifyNoInteractions(imageCompressor)
    }

    @Test
    fun `when 'max-pixels' is not specified on a form level and expected image size in setting is 'very_small', image compression should be triggered for 640px`() {
        whenever(formEntryPrompt.bindAttributes).thenReturn(emptyList())

        imageCompressionController.execute("/path", questionWidget, context, "very_small")

        verify(imageCompressor).execute("/path", 640)
    }

    @Test
    fun `when 'max-pixels' is not specified on a form level and expected image size in setting is 'small', image compression should be triggered for 1024px`() {
        whenever(formEntryPrompt.bindAttributes).thenReturn(emptyList())

        imageCompressionController.execute("/path", questionWidget, context, "small")

        verify(imageCompressor).execute("/path", 1024)
    }

    @Test
    fun `when 'max-pixels' is not specified on a form level and expected image size in setting is 'medium', image compression should be triggered for 2048px`() {
        whenever(formEntryPrompt.bindAttributes).thenReturn(emptyList())

        imageCompressionController.execute("/path", questionWidget, context, "medium")

        verify(imageCompressor).execute("/path", 2048)
    }

    @Test
    fun `when 'max-pixels' is not specified on a form level and expected image size in setting is 'large', image compression should be triggered for 3072px`() {
        whenever(formEntryPrompt.bindAttributes).thenReturn(emptyList())

        imageCompressionController.execute("/path", questionWidget, context, "large")

        verify(imageCompressor).execute("/path", 3072)
    }

    @Test
    fun `when 'max-pixels' is specified on a form level and expected image size in setting is 'original_image_size', image compression should be triggered for value stored in 'max-pixels'`() {
        whenever(formEntryPrompt.bindAttributes).thenReturn(listOf(treeElement))

        imageCompressionController.execute("/path", questionWidget, context, "original_image_size")

        verify(imageCompressor).execute("/path", 123)
    }

    @Test
    fun `when 'max-pixels' is specified on a form level and expected image size in setting is 'very_small', image compression should be triggered and the value stored in for value stored in 'max-pixels' should take precedence`() {
        whenever(formEntryPrompt.bindAttributes).thenReturn(listOf(treeElement))

        imageCompressionController.execute("/path", questionWidget, context, "very_small")

        verify(imageCompressor).execute("/path", 123)
    }

    @Test
    fun `when 'max-pixels' is specified on a form level and expected image size in setting is 'small', image compression should be triggered and the value stored in for value stored in 'max-pixels' should take precedence`() {
        whenever(formEntryPrompt.bindAttributes).thenReturn(listOf(treeElement))

        imageCompressionController.execute("/path", questionWidget, context, "small")

        verify(imageCompressor).execute("/path", 123)
    }

    @Test
    fun `when 'max-pixels' is specified on a form level and expected image size in setting is 'medium', image compression should be triggered and the value stored in for value stored in 'max-pixels' should take precedence`() {
        whenever(formEntryPrompt.bindAttributes).thenReturn(listOf(treeElement))

        imageCompressionController.execute("/path", questionWidget, context, "medium")

        verify(imageCompressor).execute("/path", 123)
    }

    @Test
    fun `when 'max-pixels' is specified on a form level and expected image size in setting is 'large', image compression should be triggered and the value stored in for value stored in 'max-pixels' should take precedence`() {
        whenever(formEntryPrompt.bindAttributes).thenReturn(listOf(treeElement))

        imageCompressionController.execute("/path", questionWidget, context, "large")

        verify(imageCompressor).execute("/path", 123)
    }

    @Test
    fun `when 'max-pixels' is specified on a form level but it is not a valid integer value and expected image size in setting is 'original_image_size', image compression should not be triggered`() {
        whenever(treeElement.attributeValue).thenReturn("123.5")
        whenever(formEntryPrompt.bindAttributes).thenReturn(listOf(treeElement))

        imageCompressionController.execute("/path", questionWidget, context, "original_image_size")

        verifyNoInteractions(imageCompressor)
    }

    @Test
    fun `when 'max-pixels' is specified on a form level but it is not a valid integer value and expected image size in setting is 'very_small', image compression should be triggered for 640px`() {
        whenever(treeElement.attributeValue).thenReturn("123.5")
        whenever(formEntryPrompt.bindAttributes).thenReturn(listOf(treeElement))

        imageCompressionController.execute("/path", questionWidget, context, "very_small")

        verify(imageCompressor).execute("/path", 640)
    }

    @Test
    fun `when 'max-pixels' is specified on a form level but it is not a valid integer value and expected image size in setting is 'small', image compression should be triggered for 1024px`() {
        whenever(treeElement.attributeValue).thenReturn("123.5")
        whenever(formEntryPrompt.bindAttributes).thenReturn(listOf(treeElement))

        imageCompressionController.execute("/path", questionWidget, context, "small")

        verify(imageCompressor).execute("/path", 1024)
    }

    @Test
    fun `when 'max-pixels' is specified on a form level but it is not a valid integer value and expected image size in setting is 'medium', image compression should be triggered for 2048px`() {
        whenever(treeElement.attributeValue).thenReturn("123.5")
        whenever(formEntryPrompt.bindAttributes).thenReturn(listOf(treeElement))

        imageCompressionController.execute("/path", questionWidget, context, "medium")

        verify(imageCompressor).execute("/path", 2048)
    }

    @Test
    fun `when 'max-pixels' is specified on a form level but it is not a valid integer value and expected image size in setting is 'large', image compression should be triggered for 3072px`() {
        whenever(treeElement.attributeValue).thenReturn("123.5")
        whenever(formEntryPrompt.bindAttributes).thenReturn(listOf(treeElement))

        imageCompressionController.execute("/path", questionWidget, context, "large")

        verify(imageCompressor).execute("/path", 3072)
    }
}
