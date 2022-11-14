package org.odk.collect.android.audio

import android.app.Activity
import androidx.fragment.app.FragmentActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.android.R
import org.odk.collect.testshared.RobolectricHelpers.createThemedActivity
import org.odk.collect.testshared.RobolectricHelpers.getCreatedFromResId

@RunWith(AndroidJUnit4::class)
class AudioButtonTest {
    private lateinit var button: AudioButton

    @Before
    fun setup() {
        val activity: Activity = createThemedActivity(
            FragmentActivity::class.java, R.style.Theme_MaterialComponents
        )
        button = AudioButton(activity)
    }

    @Test
    fun `isPlaying returns false and shows play icon`() {
        assertThat(
            button.isPlaying,
            equalTo(false)
        )

        assertThat(
            getCreatedFromResId(button.icon),
            equalTo(R.drawable.ic_volume_up_black_24dp)
        )
    }

    @Test
    fun `when playing is true shows playing icon`() {
        button.isPlaying = true

        assertThat(
            getCreatedFromResId(button.icon),
            equalTo(R.drawable.ic_stop_black_24dp)
        )
    }

    @Test
    fun `when playing is false shows play icon`() {
        button.isPlaying = false

        assertThat(
            getCreatedFromResId(button.icon),
            equalTo(R.drawable.ic_volume_up_black_24dp)
        )
    }
}
