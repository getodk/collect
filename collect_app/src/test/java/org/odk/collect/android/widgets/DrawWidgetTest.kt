package org.odk.collect.android.widgets

import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.view.View
import androidx.core.util.Pair
import androidx.test.ext.junit.runners.AndroidJUnit4
import net.bytebuddy.utility.RandomString
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.javarosa.core.model.data.StringData
import org.javarosa.core.reference.ReferenceManager
import org.junit.Test
import org.junit.runner.RunWith
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
import org.odk.collect.draw.DrawActivity
import org.odk.collect.imageloader.ImageLoader
import org.odk.collect.shared.TempFiles.getPathInTempDir
import org.robolectric.Shadows
import java.io.File

@RunWith(AndroidJUnit4::class)
class DrawWidgetTest : FileWidgetTest<DrawWidget>() {
    private var currentFile: File? = null

    override fun createWidget(): DrawWidget {
        val fakeQuestionMediaManager: QuestionMediaManager = object : FakeQuestionMediaManager() {
            override fun getAnswerFile(fileName: String): File? {
                val result = if (currentFile == null) {
                    super.getAnswerFile(fileName)
                } else {
                    if (fileName == USER_SPECIFIED_IMAGE_ANSWER) currentFile else null
                }
                return result
            }
        }
        return DrawWidget(
            activity,
            QuestionDetails(formEntryPrompt, readOnlyOverride),
            fakeQuestionMediaManager, FakeWaitingForDataRegistry(), getPathInTempDir()
        )
    }

    override fun getNextAnswer(): StringData {
        return StringData(RandomString.make())
    }

    @Test
    override fun usingReadOnlyOptionShouldMakeAllClickableElementsDisabled() {
        whenever(formEntryPrompt.isReadOnly).thenReturn(true)

        assertThat(spyWidget!!.binding.drawButton.visibility, equalTo(View.GONE))
    }

    @Test
    fun `When read only override option is used should all clickable elements be disabled`() {
        readOnlyOverride = true
        whenever(formEntryPrompt.isReadOnly).thenReturn(false)

        assertThat(spyWidget!!.binding.drawButton.visibility, equalTo(View.GONE))
    }

    @Test
    fun `When there is no answer hide image view and error message`() {
        val widget = createWidget()

        assertThat(widget.getImageView().visibility, equalTo(View.GONE))
        assertThat(widget.getImageView().drawable, nullValue())
        assertThat(widget.getErrorTextView().visibility, equalTo(View.GONE))
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
            .withAnswerDisplayText(USER_SPECIFIED_IMAGE_ANSWER)
            .build()

        val widget = createWidget()

        assertThat(widget.getImageView().visibility, equalTo(View.GONE))
        assertThat(widget.getImageView().drawable, nullValue())
        assertThat(widget.getErrorTextView().visibility, equalTo(View.VISIBLE))
    }

    @Test
    fun `When prompt has default answer shows in image view`() {
        val imagePath = File.createTempFile("default", ".bmp").absolutePath
        val referenceManager = CollectHelpers.setupFakeReferenceManager(
            listOf(
                Pair(DEFAULT_IMAGE_ANSWER, imagePath)
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
            .withAnswerDisplayText(DEFAULT_IMAGE_ANSWER)
            .build()

        val widget = createWidget()

        val imageView = widget.getImageView()
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
            .withAnswerDisplayText(USER_SPECIFIED_IMAGE_ANSWER)
            .build()

        val widget = createWidget()

        val imageView = widget.getImageView()
        assertThat(imageView.visibility, equalTo(View.VISIBLE))

        val drawable = imageView.drawable
        assertThat(drawable, notNullValue())

        val loadedPath = Shadows.shadowOf((drawable as BitmapDrawable).bitmap).createdFromPath
        assertThat(loadedPath, equalTo(imagePath))
    }

    @Test
    fun `When prompt has default answer pass uri to draw activity`() {
        val file = File.createTempFile("default", ".bmp")
        val imagePath = file.absolutePath

        val referenceManager = CollectHelpers.setupFakeReferenceManager(
            listOf(
                Pair(DEFAULT_IMAGE_ANSWER, imagePath)
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
            .withAnswerDisplayText(DEFAULT_IMAGE_ANSWER)
            .build()

        val intent = getIntentLaunchedByClick(R.id.draw_button)

        assertComponentEquals(activity, DrawActivity::class.java, intent)
        assertExtraEquals(DrawActivity.OPTION, DrawActivity.OPTION_DRAW, intent)
        assertExtraEquals(DrawActivity.REF_IMAGE, Uri.fromFile(file), intent)
    }

    @Test
    fun `When prompt has default answer that does not exist do not pass uri to draw activity`() {
        val referenceManager = CollectHelpers.setupFakeReferenceManager(
            listOf(
                Pair(DEFAULT_IMAGE_ANSWER, "/something")
            )
        )
        CollectHelpers.overrideAppDependencyModule(object : AppDependencyModule() {
            override fun providesReferenceManager(): ReferenceManager {
                return referenceManager
            }
        })

        formEntryPrompt = MockFormEntryPromptBuilder()
            .withAnswerDisplayText(DEFAULT_IMAGE_ANSWER)
            .build()

        val intent = getIntentLaunchedByClick(R.id.draw_button)

        assertComponentEquals(activity, DrawActivity::class.java, intent)
        assertExtraEquals(DrawActivity.OPTION, DrawActivity.OPTION_DRAW, intent)
        assertThat(intent.hasExtra(DrawActivity.REF_IMAGE), equalTo(false))
    }

    @Test
    fun `When there is no answer do not pass uri to draw activity`() {
        val intent = getIntentLaunchedByClick(R.id.draw_button)

        assertComponentEquals(activity, DrawActivity::class.java, intent)
        assertExtraEquals(DrawActivity.OPTION, DrawActivity.OPTION_DRAW, intent)
        assertThat(intent.hasExtra(DrawActivity.REF_IMAGE), equalTo(false))
    }

    companion object {
        const val DEFAULT_IMAGE_ANSWER: String = "jr://images/referenceURI"
        const val USER_SPECIFIED_IMAGE_ANSWER: String = "current.bmp"
    }
}
