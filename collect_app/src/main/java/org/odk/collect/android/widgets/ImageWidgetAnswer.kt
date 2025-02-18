package org.odk.collect.android.widgets
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import org.javarosa.core.model.data.IAnswerData
import org.javarosa.core.reference.ReferenceManager
import org.odk.collect.android.databinding.ImageWidgetAnswerBinding
import org.odk.collect.android.utilities.QuestionMediaManager
import org.odk.collect.imageloader.GlideImageLoader.ImageLoaderCallback
import org.odk.collect.imageloader.ImageLoader
import java.io.File

class ImageWidgetAnswer @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0
) : FrameLayout(context, attrs, defStyle) {
    private val binding = ImageWidgetAnswerBinding.inflate(LayoutInflater.from(context), this, true)
    private lateinit var imageLoader: ImageLoader
    private lateinit var questionMediaManager: QuestionMediaManager
    private lateinit var referenceManager: ReferenceManager
    private var defaultFilePath: String? = null

    fun setup(
        answer: IAnswerData?,
        imageLoader: ImageLoader,
        questionMediaManager: QuestionMediaManager,
        referenceManager: ReferenceManager,
        defaultFilePath: String?
    ) {
        this.imageLoader = imageLoader
        this.questionMediaManager = questionMediaManager
        this.referenceManager = referenceManager
        this.defaultFilePath = defaultFilePath
        setAnswer(answer?.displayText)
    }

    fun setAnswer(answer: String?) {
        binding.errorMessage.visibility = GONE
        binding.imageView.visibility = GONE

        if (answer == null) {
            binding.imageView.setImageDrawable(null)
            binding.imageView.visibility = GONE
        } else {
            val imageFile = getFile(answer)
            if (imageFile != null && imageFile.exists()) {
                binding.imageView.visibility = VISIBLE
                imageLoader.loadImage(
                    binding.imageView,
                    imageFile,
                    ImageView.ScaleType.FIT_CENTER,
                    object : ImageLoaderCallback {
                        override fun onLoadFailed() {
                            binding.errorMessage.visibility = VISIBLE
                            binding.imageView.visibility = GONE
                        }
                        override fun onLoadSucceeded() {
                        }
                    })
            }
        }
    }

    fun getImageView() = binding.imageView

    fun getErrorView() = binding.errorMessage

    fun hasError(): Boolean {
        return binding.errorMessage.visibility == VISIBLE
    }

    fun getFile(fileName: String?): File? {
        if (fileName == null) {
            return null
        }

        val file = questionMediaManager.getAnswerFile(fileName)
        if ((file == null || !file.exists())) {
            return defaultFilePath?.let { File(it) }
        }
        return file
    }
}
