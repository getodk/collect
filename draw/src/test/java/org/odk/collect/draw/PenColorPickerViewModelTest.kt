package org.odk.collect.draw

import android.graphics.Color
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.shared.settings.InMemSettings
import org.odk.collect.shared.settings.Settings

@RunWith(AndroidJUnit4::class)
class PenColorPickerViewModelTest {
    private lateinit var settings: Settings
    private lateinit var viewModel: PenColorPickerViewModel

    @Before
    fun setup() {
        settings = InMemSettings()
        viewModel = PenColorPickerViewModel(settings, "penColor")
    }

    @Test
    fun `default pen color should be black`() {
        assertThat(viewModel.penColor.value, `is`(Color.BLACK))
    }

    @Test
    fun `setPenColor sets penColor`() {
        viewModel.setPenColor(Color.RED)
        assertThat(viewModel.penColor.value, `is`(Color.RED))
    }

    @Test
    fun `color saved in meta settings should be used as default if exists`() {
        settings.save("penColor", Color.RED)
        viewModel = PenColorPickerViewModel(settings, "penColor")
        assertThat(viewModel.penColor.value, `is`(Color.RED))
    }
}
