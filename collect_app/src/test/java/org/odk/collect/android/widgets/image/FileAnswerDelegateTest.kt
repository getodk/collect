package org.odk.collect.android.widgets.image

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.nullValue
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.odk.collect.android.support.MockFormEntryPromptBuilder
import org.odk.collect.android.widgets.QuestionWidget
import org.odk.collect.android.widgets.support.FakeQuestionMediaManager
import java.io.File

@RunWith(AndroidJUnit4::class)
class FileAnswerDelegateTest {
    private val questionMediaManager = FakeQuestionMediaManager()
    private val prompt = MockFormEntryPromptBuilder().build()
    private val widget = mock<QuestionWidget>()
    private val delegate = FileAnswerDelegate(widget, questionMediaManager, prompt)

    @Test
    fun `getAnswer() returns StringData with binary name when answer is present`() {
        val promptWithAnswer = MockFormEntryPromptBuilder()
            .withAnswerDisplayText("image.jpg")
            .build()
        val delegate = FileAnswerDelegate(widget, questionMediaManager, promptWithAnswer)

        assertThat(delegate.getAnswer()?.value, equalTo("image.jpg" as Any))
    }

    @Test
    fun `getAnswer() returns null when binary name is null`() {
        assertThat(delegate.getAnswer(), nullValue())
    }

    @Test
    fun `deleteFile() clears binary name, calls questionMediaManager and notifies the widget`() {
        val promptWithAnswer = MockFormEntryPromptBuilder()
            .withAnswerDisplayText("image.jpg")
            .withIndex("1")
            .build()
        val delegate = FileAnswerDelegate(widget, questionMediaManager, promptWithAnswer)

        delegate.deleteFile()

        assertThat(delegate.binaryName, nullValue())
        assertThat(questionMediaManager.originalFiles["1"], equalTo("image.jpg"))
        verify(widget).widgetValueChanged()
    }

    @Test
    fun `setData() with valid file updates binary name and notifies the widget`() {
        val file = File.createTempFile("new_image", ".jpg")

        delegate.setData(file)

        assertThat(delegate.binaryName, equalTo(file.name))
        assertThat(questionMediaManager.recentFiles[prompt.index.toString()], equalTo(file.absolutePath))
        verify(widget).widgetValueChanged()
    }

    @Test
    fun `setData() deletes old file if it exists`() {
        val promptWithAnswer = MockFormEntryPromptBuilder()
            .withAnswerDisplayText("old_image.jpg")
            .withIndex("1")
            .build()
        val delegate = FileAnswerDelegate(widget, questionMediaManager, promptWithAnswer)

        val file = File.createTempFile("new_image", ".jpg")
        delegate.setData(file)

        assertThat(questionMediaManager.originalFiles["1"], equalTo("old_image.jpg"))
    }

    @Test
    fun `setData() does nothing and does not notify the widget when file does not exist`() {
        val file = File("non_existent_file")

        delegate.setData(file)

        assertThat(delegate.binaryName, nullValue())
        verify(widget, never()).widgetValueChanged()
    }

    @Test
    fun `setData() does nothing and does not notify the widget when data is not a file`() {
        delegate.setData("not a file")

        assertThat(delegate.binaryName, nullValue())
        verify(widget, never()).widgetValueChanged()
    }
}
