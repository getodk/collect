package org.odk.collect.android.audio

import android.widget.SeekBar
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.Mockito.verifyNoMoreInteractions
import org.odk.collect.android.R
import org.odk.collect.android.support.CollectHelpers
import org.odk.collect.android.support.SwipableParentActivity
import org.robolectric.Shadows
import org.robolectric.shadows.ShadowView

@RunWith(AndroidJUnit4::class)
class AudioControllerViewTest {
    private lateinit var activity: SwipableParentActivity
    private lateinit var view: AudioControllerView

    @Before
    fun setup() {
        activity = CollectHelpers.buildThemedActivity(
            SwipableParentActivity::class.java
        ).get()
        view = AudioControllerView(activity)
    }

    @Test
    fun `setDuration() shows duration in minutes and seconds`() {
        view.setDuration(52000)

        assertThat(
            view.binding.totalDuration.text.toString(),
            equalTo("00:52")
        )
    }

    @Test
    fun `setPosition() shows position in minutes and seconds`() {
        view.setDuration(65000)
        view.setPosition(64000)

        assertThat(
            view.binding.currentDuration.text.toString(),
            equalTo("01:04")
        )
    }

    @Test
    fun `setPosition() changes seekBar position`() {
        view.setDuration(65000)

        assertThat(view.binding.seekBar.progress, `is`(0))
        assertThat(view.binding.seekBar.max, `is`(65000))

        view.setPosition(8000)

        assertThat(view.binding.seekBar.progress, Matchers.`is`(8000))
        assertThat(view.binding.seekBar.max, Matchers.`is`(65000))
    }

    @Test
    fun `swiping seekBar when paused skips to position once stopped`() {
        val listener = Mockito.mock(
            AudioControllerView.Listener::class.java
        )

        view.setListener(listener)
        view.setDuration(12000)

        val seekBar = view.binding.seekBar

        Shadows.shadowOf(seekBar).onSeekBarChangeListener.onStartTrackingTouch(seekBar)
        Shadows.shadowOf(seekBar).onSeekBarChangeListener.onProgressChanged(seekBar, 7000, true)

        assertThat(
            ShadowView.innerText(view.findViewById(R.id.currentDuration)),
            equalTo("00:07")
        )

        verifyNoInteractions(listener) // We don't change position yet

        Shadows.shadowOf(seekBar).onSeekBarChangeListener.onProgressChanged(seekBar, 5000, true)

        assertThat(
            ShadowView.innerText(view.findViewById(R.id.currentDuration)),
            equalTo("00:05")
        )
        verifyNoInteractions(listener) // We don't change position yet

        Shadows.shadowOf(seekBar).onSeekBarChangeListener.onStopTrackingTouch(seekBar)

        assertThat(
            ShadowView.innerText(view.findViewById(R.id.currentDuration)),
            equalTo("00:05")
        )

        verify(listener).onPositionChanged(5000)
    }

    @Test
    fun `swiping seekBar when playing pauses and then skips to position and plays once stopped`() {
        val listener = Mockito.mock(
            AudioControllerView.Listener::class.java
        )
        view.setListener(listener)
        view.setDuration(12000)
        view.setPlaying(true)

        val seekBar = view.binding.seekBar

        Shadows.shadowOf(seekBar).onSeekBarChangeListener.onStartTrackingTouch(seekBar)

        verify(listener).onPauseClicked()

        Shadows.shadowOf(seekBar).onSeekBarChangeListener.onProgressChanged(seekBar, 7000, true)

        assertThat(
            ShadowView.innerText(view.findViewById(R.id.currentDuration)),
            equalTo("00:07")
        )

        verifyNoMoreInteractions(listener) // We don't change position yet

        Shadows.shadowOf(seekBar).onSeekBarChangeListener.onProgressChanged(seekBar, 5000, true)

        assertThat(
            ShadowView.innerText(view.findViewById(R.id.currentDuration)),
            equalTo("00:05")
        )

        verifyNoMoreInteractions(listener) // We don't change position yet

        Shadows.shadowOf(seekBar).onSeekBarChangeListener.onStopTrackingTouch(seekBar)

        assertThat(
            ShadowView.innerText(view.findViewById(R.id.currentDuration)),
            equalTo("00:05")
        )

        verify(listener).onPositionChanged(5000)
        verify(listener).onPlayClicked()
    }

    @Test
    fun `when swiping notifies swipeable parent`() {
        val seekBar = view.findViewById<SeekBar>(R.id.seekBar)
        Shadows.shadowOf(seekBar).onSeekBarChangeListener.onStartTrackingTouch(seekBar)

        assertThat(activity.isSwipingAllowed, equalTo(false))

        Shadows.shadowOf(seekBar).onSeekBarChangeListener.onStopTrackingTouch(seekBar)

        assertThat(activity.isSwipingAllowed, equalTo(true))
    }
}
