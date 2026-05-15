package org.odk.collect.android.widgets

import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.provider.MediaStore
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.core.util.Pair
import net.bytebuddy.utility.RandomString
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.notNullValue
import org.hamcrest.Matchers.nullValue
import org.javarosa.core.model.data.StringData
import org.javarosa.core.reference.ReferenceManager
import org.junit.Assert.assertNull
import org.junit.Test
import org.mockito.kotlin.whenever
import org.odk.collect.android.R
import org.odk.collect.android.formentry.questions.QuestionDetails
import org.odk.collect.android.injection.config.AppDependencyModule
import org.odk.collect.android.support.CollectHelpers
import org.odk.collect.android.support.CollectHelpers.setupFakeReferenceManager
import org.odk.collect.android.support.MockFormEntryPromptBuilder
import org.odk.collect.android.widgets.base.FileWidgetTest
import org.odk.collect.android.widgets.support.FakeQuestionMediaManager
import org.odk.collect.android.widgets.support.FakeWaitingForDataRegistry
import org.odk.collect.android.widgets.support.SynchronousImageLoader
import org.odk.collect.android.widgets.utilities.FileAnswerDelegate
import org.odk.collect.imageloader.ImageLoader
import org.odk.collect.shared.TempFiles
import org.robolectric.Shadows.shadowOf
import java.io.File
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify

class ImageWidgetTest : FileWidgetTest<ImageWidget>() {
    private var currentFile: File? = null
    private lateinit var fileAnswerDelegate: FileAnswerDelegate

    override fun createWidget(): ImageWidget {
        val fakeQuestionMediaManager = object : FakeQuestionMediaManager() {
            override fun getAnswerFile(fileName: String): File? {
                return if (currentFile == null) {
                    super.getAnswerFile(fileName)
                } else {
                    if (fileName == DrawWidgetTest.USER_SPECIFIED_IMAGE_ANSWER) currentFile else null
                }
            }
        }

        fileAnswerDelegate = spy(FileAnswerDelegate(fakeQuestionMediaManager, formEntryPrompt))

        return ImageWidget(
            activity,
            QuestionDetails(formEntryPrompt, readOnlyOverride),
            fakeQuestionMediaManager,
            FakeWaitingForDataRegistry(),
            TempFiles.getPathInTempDir(),
            dependencies,
            fileAnswerDelegate
        )
    }

    @Test
    override fun settingANewAnswerShouldCallDeleteMediaToRemoveTheOldFile() {
        super.settingANewAnswerShouldRemoveTheOldAnswer()
        verify(fileAnswerDelegate).deleteFile()
    }

    @Test
    override fun callingClearAnswerShouldCallDeleteMediaAndRemoveTheExistingAnswer() {
        super.callingClearShouldRemoveTheExistingAnswer()
        verify(fileAnswerDelegate).deleteFile()
    }

    override fun getNextAnswer(): StringData {
        return StringData(RandomString.make())
    }

    @Test
    fun `buttons should launch correct intents when there is no custom package`() {
        stubAllRuntimePermissionsGranted(true)

        var intent = getIntentLaunchedByClick(R.id.capture_button)
        assertActionEquals(MediaStore.ACTION_IMAGE_CAPTURE, intent)
        assertThat(intent!!.getPackage(), equalTo(null))

        intent = getIntentLaunchedByClick(R.id.choose_button)
        assertActionEquals(Intent.ACTION_GET_CONTENT, intent)
        assertTypeEquals("image/*", intent)
    }

    @Test
    fun `buttons should launch correct intents when custom package is set`() {
        formEntryPrompt = MockFormEntryPromptBuilder()
            .withAdditionalAttribute("intent", "com.customcameraapp")
            .build()

        stubAllRuntimePermissionsGranted(true)

        var intent = getIntentLaunchedByClick(R.id.capture_button)
        assertActionEquals(MediaStore.ACTION_IMAGE_CAPTURE, intent)
        assertThat(intent!!.getPackage(), equalTo("com.customcameraapp"))

        intent = getIntentLaunchedByClick(R.id.choose_button)
        assertActionEquals(Intent.ACTION_GET_CONTENT, intent)
        assertTypeEquals("image/*", intent)
    }

    @Test
    fun `capture button should not launch any intent when permissions denied`() {
        stubAllRuntimePermissionsGranted(false)

        assertNull(getIntentLaunchedByClick(R.id.capture_button))
    }

    @Test
    override fun usingReadOnlyOptionShouldMakeAllClickableElementsDisabled() {
        whenever(formEntryPrompt.isReadOnly).thenReturn(true)

        assertThat(spyWidget.binding.captureButton.visibility, equalTo(GONE))
        assertThat(spyWidget.binding.chooseButton.visibility, equalTo(GONE))
    }

    @Test
    fun `when read-only override option is used should all clickable elements be disabled`() {
        readOnlyOverride = true
        whenever(formEntryPrompt.isReadOnly).thenReturn(false)

        assertThat(spyWidget.binding.captureButton.visibility, equalTo(GONE))
        assertThat(spyWidget.binding.chooseButton.visibility, equalTo(GONE))
    }

    @Test
    fun `when there is no answer hide image view and error message`() {
        val widget = createWidget()

        assertThat(widget.binding.image.visibility, equalTo(GONE))
        assertThat(widget.binding.image.drawable, nullValue())

        assertThat(widget.binding.errorMessage.visibility, equalTo(GONE))
    }

    @Test
    fun `when the answer image cannot be loaded hide image view and show error message`() {
        CollectHelpers.overrideAppDependencyModule(object : AppDependencyModule() {
            override fun providesImageLoader(): ImageLoader {
                return SynchronousImageLoader(true)
            }
        })

        val imagePath = File.createTempFile("current", ".bmp").absolutePath
        currentFile = File(imagePath)

        formEntryPrompt = MockFormEntryPromptBuilder()
            .withAnswerDisplayText(DrawWidgetTest.USER_SPECIFIED_IMAGE_ANSWER)
            .build()

        val widget = createWidget()

        assertThat(widget.binding.image.visibility, equalTo(GONE))
        assertThat(widget.binding.image.drawable, nullValue())

        assertThat(widget.binding.errorMessage.visibility, equalTo(VISIBLE))
    }

    @Test
    fun `when prompt has default answer does not show`() {
        val imagePath = File.createTempFile("default", ".bmp").absolutePath
        val referenceManager = setupFakeReferenceManager(
            listOf(
                Pair(DrawWidgetTest.DEFAULT_IMAGE_ANSWER, imagePath)
            )
        )
        CollectHelpers.overrideAppDependencyModule(object : AppDependencyModule() {
            override fun providesReferenceManager(): ReferenceManager {
                return referenceManager
            }

            override fun providesImageLoader(): ImageLoader {
                return SynchronousImageLoader()
            }
        })

        formEntryPrompt = MockFormEntryPromptBuilder()
            .withAnswerDisplayText(DrawWidgetTest.DEFAULT_IMAGE_ANSWER)
            .build()

        val widget = createWidget()
        val imageView = widget.binding.image
        assertThat(imageView.visibility, equalTo(GONE))
    }

    @Test
    fun `when prompt has current answer shows in image view`() {
        CollectHelpers.overrideAppDependencyModule(object : AppDependencyModule() {
            override fun providesImageLoader(): ImageLoader {
                return SynchronousImageLoader()
            }
        })

        val imagePath = File.createTempFile("current", ".bmp").absolutePath
        currentFile = File(imagePath)

        formEntryPrompt = MockFormEntryPromptBuilder()
            .withAnswerDisplayText(DrawWidgetTest.USER_SPECIFIED_IMAGE_ANSWER)
            .build()

        val widget = createWidget()
        val imageView = widget.binding.image
        assertThat(imageView.visibility, equalTo(VISIBLE))
        val drawable = imageView.drawable
        assertThat(drawable, notNullValue())

        val loadedPath = shadowOf((drawable as BitmapDrawable).bitmap).createdFromPath
        assertThat(loadedPath, equalTo(imagePath))
    }
}
