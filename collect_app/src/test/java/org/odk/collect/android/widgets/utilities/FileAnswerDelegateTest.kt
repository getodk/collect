package org.odk.collect.android.widgets.utilities

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.nullValue
import org.junit.Test
import org.odk.collect.android.support.MockFormEntryPromptBuilder
import org.odk.collect.android.widgets.support.FakeQuestionMediaManager
import java.io.File

class FileAnswerDelegateTest {
    private val questionMediaManager = FakeQuestionMediaManager()
    private val prompt = MockFormEntryPromptBuilder().build()
    private val delegate = FileAnswerDelegate(questionMediaManager, prompt)

    @Test
    fun `getAnswer() returns StringData with binary name when answer is present`() {
        val promptWithAnswer = MockFormEntryPromptBuilder()
            .withAnswerDisplayText("image.jpg")
            .build()
        val delegate = FileAnswerDelegate(questionMediaManager, promptWithAnswer)

        assertThat(delegate.getAnswer()?.value, equalTo("image.jpg" as Any))
    }

    @Test
    fun `getAnswer() returns null when binary name is null`() {
        assertThat(delegate.getAnswer(), nullValue())
    }

    @Test
    fun `deleteFile() clears binary name and calls questionMediaManager`() {
        val promptWithAnswer = MockFormEntryPromptBuilder()
            .withAnswerDisplayText("image.jpg")
            .withIndex("1")
            .build()
        val delegate = FileAnswerDelegate(questionMediaManager, promptWithAnswer)

        delegate.deleteFile()

        assertThat(delegate.binaryName, nullValue())
        assertThat(questionMediaManager.originalFiles["1"], equalTo("image.jpg"))
    }

    @Test
    fun `setData() with valid file updates binary name and calls questionMediaManager`() {
        val file = File.createTempFile("new_image", ".jpg")
        var answerChangedName: String? = null

        delegate.setData(file) { answerChangedName = it }

        assertThat(delegate.binaryName, equalTo(file.name))
        assertThat(answerChangedName, equalTo(file.name))
        assertThat(questionMediaManager.recentFiles[prompt.index.toString()], equalTo(file.absolutePath))
    }

    @Test
    fun `setData() deletes old file if it exists`() {
        val promptWithAnswer = MockFormEntryPromptBuilder()
            .withAnswerDisplayText("old_image.jpg")
            .withIndex("1")
            .build()
        val delegate = FileAnswerDelegate(questionMediaManager, promptWithAnswer)

        val file = File.createTempFile("new_image", ".jpg")
        delegate.setData(file) {}

        assertThat(questionMediaManager.originalFiles["1"], equalTo("old_image.jpg"))
    }

    @Test
    fun `setData() does nothing and logs error when file does not exist`() {
        val file = File("non_existent_file")
        var answerChanged = false

        delegate.setData(file) { answerChanged = true }

        assertThat(delegate.binaryName, nullValue())
        assertThat(answerChanged, equalTo(false))
    }

    @Test
    fun `setData() does nothing when data is not a file`() {
        var answerChanged = false

        delegate.setData("not a file") { answerChanged = true }

        assertThat(delegate.binaryName, nullValue())
        assertThat(answerChanged, equalTo(false))
    }
}
