package org.odk.collect.android.widgets

import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.provider.MediaStore
import android.view.View
import androidx.core.util.Pair
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
import org.odk.collect.android.widgets.base.FileWidgetTest
import org.odk.collect.android.widgets.support.FakeQuestionMediaManager
import org.odk.collect.android.widgets.support.FakeWaitingForDataRegistry
import org.odk.collect.android.widgets.support.SynchronousImageLoader
import org.odk.collect.draw.DrawActivity
import org.odk.collect.imageloader.ImageLoader
import org.odk.collect.shared.TempFiles.createTempFile
import org.odk.collect.shared.TempFiles.getPathInTempDir
import org.robolectric.Shadows
import org.robolectric.shadows.ShadowToast
import java.io.File

class AnnotateWidgetTest : FileWidgetTest<AnnotateWidget>() {
    private lateinit var questionMediaManager: FakeQuestionMediaManager
    private var currentFile: File? = null
    private val file = createTempFile("sample", ".jpg")

    override fun createWidget(): AnnotateWidget {
        questionMediaManager = object : FakeQuestionMediaManager() {
            override fun getAnswerFile(fileName: String): File? {
                val result = if (currentFile == null) {
                    super.getAnswerFile(fileName)
                } else {
                    if (fileName == DrawWidgetTest.USER_SPECIFIED_IMAGE_ANSWER) currentFile else null
                }
                return result
            }
        }
        return AnnotateWidget(
            activity,
            QuestionDetails(formEntryPrompt, readOnlyOverride),
            questionMediaManager,
            FakeWaitingForDataRegistry(),
            getPathInTempDir()
        )
    }

    override fun getNextAnswer(): StringData {
        return StringData(file.name)
    }

    override fun createBinaryData(answerData: StringData): Any {
        return file
    }

    @Test
    fun `Buttons should launch correct intents when there is no custom package`() {
        stubAllRuntimePermissionsGranted(true)

        var intent = getIntentLaunchedByClick(R.id.capture_button)
        assertActionEquals(MediaStore.ACTION_IMAGE_CAPTURE, intent)
        assertThat(intent.getPackage(), equalTo(null))

        intent = getIntentLaunchedByClick(R.id.choose_button)
        assertActionEquals(Intent.ACTION_GET_CONTENT, intent)

        intent = getIntentLaunchedByClick(R.id.annotate_button)
        assertComponentEquals(activity, DrawActivity::class.java, intent)
        assertExtraEquals(DrawActivity.OPTION, DrawActivity.OPTION_ANNOTATE, intent)
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
        assertThat(spyWidget!!.binding.annotateButton.visibility, equalTo(View.GONE))
    }

    @Test
    fun `When read only override option is used should all clickable elements be disabled`() {
        readOnlyOverride = true
        whenever(formEntryPrompt.isReadOnly).thenReturn(false)

        assertThat(spyWidget!!.binding.captureButton.visibility, equalTo(View.GONE))
        assertThat(spyWidget!!.binding.chooseButton.visibility, equalTo(View.GONE))
        assertThat(spyWidget!!.binding.annotateButton.visibility, equalTo(View.GONE))
    }

    @Test
    fun `When there is no answer hide image view and error message`() {
        val widget = createWidget()

        assertThat(widget.answerView.getImageView().visibility, equalTo(View.GONE))
        assertThat(widget.answerView.getImageView().drawable, nullValue())
        assertThat(widget.answerView.getErrorView().visibility, equalTo(View.GONE))
    }

    @Test
    fun `When gif file selected do not attach it and display a message`() {
        val widget = createWidget()

        val file = createTempFile("sample", ".gif")
        questionMediaManager.addAnswerFile(file)
        widget.setData(file)

        assertThat(widget.answerView.getImageView().visibility, equalTo(View.GONE))
        assertThat(widget.answerView.getImageView().drawable, nullValue())
        assertThat(ShadowToast.getTextOfLatestToast(), equalTo("Gif files are not supported"))
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
    fun `When prompt has default answer shows in image view`() {
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
        assertThat(imageView.visibility, equalTo(View.VISIBLE))

        val drawable = imageView.drawable
        assertThat(drawable, notNullValue())

        val loadedPath = Shadows.shadowOf((drawable as BitmapDrawable).bitmap).createdFromPath
        assertThat(loadedPath, equalTo(imagePath))
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

    @Test
    fun `Markup button should be disabled if image absent`() {
        val badPath = "bad_path"
        CollectHelpers.overrideReferenceManager(
            CollectHelpers.setupFakeReferenceManager(
                listOf(
                    Pair(DrawWidgetTest.DEFAULT_IMAGE_ANSWER, badPath)
                )
            )
        )

        formEntryPrompt = MockFormEntryPromptBuilder()
            .withAnswerDisplayText(DrawWidgetTest.DEFAULT_IMAGE_ANSWER)
            .build()

        assertThat(widget!!.binding.annotateButton.isEnabled, equalTo(false))

        formEntryPrompt = MockFormEntryPromptBuilder()
            .withAnswerDisplayText(DrawWidgetTest.USER_SPECIFIED_IMAGE_ANSWER)
            .build()

        assertThat(widget!!.binding.annotateButton.isEnabled, equalTo(false))
    }

    @Test
    fun `When prompt has default answer pass uri to draw activity`() {
        val file = File.createTempFile("default", ".bmp")
        val imagePath = file.absolutePath

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

        val intent = getIntentLaunchedByClick(R.id.annotate_button)

        assertComponentEquals(activity, DrawActivity::class.java, intent)
        assertExtraEquals(DrawActivity.OPTION, DrawActivity.OPTION_ANNOTATE, intent)
        assertExtraEquals(DrawActivity.REF_IMAGE, Uri.fromFile(file), intent)
    }

    @Test
    fun `When prompt has default answer that does not exist do not pass uri to draw activity`() {
        val referenceManager = CollectHelpers.setupFakeReferenceManager(
            listOf(
                Pair(DrawWidgetTest.DEFAULT_IMAGE_ANSWER, "/something")
            )
        )
        CollectHelpers.overrideAppDependencyModule(object : AppDependencyModule() {
            override fun providesReferenceManager(): ReferenceManager {
                return referenceManager
            }
        })

        formEntryPrompt = MockFormEntryPromptBuilder()
            .withAnswerDisplayText(DrawWidgetTest.DEFAULT_IMAGE_ANSWER)
            .build()

        val intent = getIntentLaunchedByClick(R.id.annotate_button)

        assertComponentEquals(activity, DrawActivity::class.java, intent)
        assertExtraEquals(DrawActivity.OPTION, DrawActivity.OPTION_ANNOTATE, intent)
        assertThat(intent.hasExtra(DrawActivity.REF_IMAGE), equalTo(false))
    }

    @Test
    fun `When there is no answer do not pass uri to draw activity`() {
        val intent = getIntentLaunchedByClick(R.id.annotate_button)

        assertComponentEquals(activity, DrawActivity::class.java, intent)
        assertExtraEquals(DrawActivity.OPTION, DrawActivity.OPTION_ANNOTATE, intent)
        assertThat(intent.hasExtra(DrawActivity.REF_IMAGE), equalTo(false))
    }
}
