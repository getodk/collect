package org.odk.collect.android.widgets

import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.provider.MediaStore
import android.view.View
import androidx.core.util.Pair
import net.bytebuddy.utility.RandomString
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.javarosa.core.model.data.StringData
import org.javarosa.core.reference.ReferenceManager
import org.junit.Test
import org.mockito.kotlin.whenever
import org.odk.collect.android.R
import org.odk.collect.android.formentry.questions.QuestionDetails
import org.odk.collect.android.injection.config.AppDependencyModule
import org.odk.collect.android.support.CollectHelpers
import org.odk.collect.android.support.MockFormEntryPromptBuilder
import org.odk.collect.android.utilities.QuestionMediaManager
import org.odk.collect.android.widgets.base.FileWidgetTest
import org.odk.collect.android.widgets.support.FakeQuestionMediaManager
import org.odk.collect.android.widgets.support.FakeWaitingForDataRegistry
import org.odk.collect.android.widgets.support.SynchronousImageLoader
import org.odk.collect.imageloader.ImageLoader
import org.odk.collect.shared.TempFiles.getPathInTempDir
import org.robolectric.Shadows
import java.io.File

class ImageWidgetTest : FileWidgetTest<ImageWidget>() {
    private var currentFile: File? = null

    override fun createWidget(): ImageWidget {
        val fakeQuestionMediaManager: QuestionMediaManager = object : FakeQuestionMediaManager() {
            override fun getAnswerFile(fileName: String): File? {
                val result = if (currentFile == null) {
                    super.getAnswerFile(fileName)
                } else {
                    if (fileName == DrawWidgetTest.USER_SPECIFIED_IMAGE_ANSWER) currentFile else null
                }
                return result
            }
        }
        return ImageWidget(
            activity, QuestionDetails(formEntryPrompt, readOnlyOverride),
            fakeQuestionMediaManager, FakeWaitingForDataRegistry(), getPathInTempDir()
        )
    }

    override fun getNextAnswer(): StringData {
        return StringData(RandomString.make())
    }

    @Test
    fun `Buttons should launch correct intents when there is no custom package`() {
        stubAllRuntimePermissionsGranted(true)

        var intent = getIntentLaunchedByClick(R.id.capture_button)
        assertActionEquals(MediaStore.ACTION_IMAGE_CAPTURE, intent)
        assertThat(intent.getPackage(), equalTo(null))

        intent = getIntentLaunchedByClick(R.id.choose_button)
        assertActionEquals(Intent.ACTION_GET_CONTENT, intent)
        assertTypeEquals("image/*", intent)
    }

    @Test
    fun `Buttons should launch correct intents when custom package is set`() {
        formEntryPrompt = MockFormEntryPromptBuilder()
            .withAdditionalAttribute("intent", "com.customcameraapp")
            .build()

        stubAllRuntimePermissionsGranted(true)

        var intent = getIntentLaunchedByClick(R.id.capture_button)
        assertActionEquals(MediaStore.ACTION_IMAGE_CAPTURE, intent)
        assertThat(intent.getPackage(), equalTo("com.customcameraapp"))

        intent = getIntentLaunchedByClick(R.id.choose_button)
        assertActionEquals(Intent.ACTION_GET_CONTENT, intent)
        assertTypeEquals("image/*", intent)
    }

    @Test
    fun `Buttons should not launch intents when permissions denied`() {
        stubAllRuntimePermissionsGranted(false)

        assertThat(getIntentLaunchedByClick(R.id.capture_button), equalTo(null))
    }

    @Test
    override fun usingReadOnlyOptionShouldMakeAllClickableElementsDisabled() {
        whenever(formEntryPrompt.isReadOnly).thenReturn(true)

        assertThat(spyWidget!!.binding.captureButton.visibility, equalTo(View.GONE))
        assertThat(spyWidget!!.binding.chooseButton.visibility, equalTo(View.GONE))
    }

    @Test
    fun `When read only override option is used should all clickable elements be disabled`() {
        readOnlyOverride = true
        whenever(formEntryPrompt.isReadOnly).thenReturn(false)

        assertThat(spyWidget!!.binding.captureButton.visibility, equalTo(View.GONE))
        assertThat(spyWidget!!.binding.chooseButton.visibility, equalTo(View.GONE))
    }

    @Test
    fun `When there is no answer hide image view and error message`() {
        val widget = createWidget()

        assertThat(widget.answerView.getImageView().visibility, equalTo(View.GONE))
        assertThat(widget.answerView.getImageView().drawable, nullValue())
        assertThat(widget.answerView.getErrorView().visibility, equalTo(View.GONE))
    }

    @Test
    fun `When the answer image can not be loaded hide image view and show error message`() {
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

        assertThat(widget.answerView.getImageView().visibility, equalTo(View.GONE))
        assertThat(widget.answerView.getImageView().drawable, nullValue())
        assertThat(widget.answerView.getErrorView().visibility, equalTo(View.VISIBLE))
    }

    @Test
    fun `When prompt has default answer do not show it`() {
        val imagePath = File.createTempFile("default", ".bmp").absolutePath
        val referenceManager = CollectHelpers.setupFakeReferenceManager(
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
        val imageView = widget.answerView.getImageView()

        assertThat(imageView.visibility, equalTo(View.GONE))
    }

    @Test
    fun `When prompt has current answer shows in image view`() {
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

        val imageView = widget.answerView.getImageView()
        assertThat(imageView.visibility, equalTo(View.VISIBLE))

        val drawable = imageView.drawable
        assertThat(drawable, notNullValue())

        val loadedPath = Shadows.shadowOf((drawable as BitmapDrawable).bitmap).createdFromPath
        assertThat(loadedPath, equalTo(imagePath))
    }
}
