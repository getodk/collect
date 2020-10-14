package org.odk.collect.audiorecorder.recording

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_audio_recorder.*
import org.odk.collect.audiorecorder.R
import org.odk.collect.audiorecorder.getComponent
import javax.inject.Inject

class AudioRecorderActivity : AppCompatActivity() {

    @Inject
    lateinit var recorder: Recorder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getComponent().inject(this)

        setTheme(intent.getIntExtra(ARGS.THEME, R.style.Theme_MaterialComponents_Light_NoActionBar))
        setContentView(R.layout.activity_audio_recorder)

        recorder.start()

        done.setOnClickListener {
            val recording = recorder.stop()
            setResult(Activity.RESULT_OK, Intent().also {
                it.data = Uri.parse(recording.absolutePath)
            })

            finish()
        }
    }

    object ARGS {
        const val THEME = "theme"
    }
}