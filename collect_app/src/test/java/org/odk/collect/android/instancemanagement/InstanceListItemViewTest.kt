package org.odk.collect.android.instancemanagement

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.android.R
import org.odk.collect.android.databinding.FormChooserListItemBinding
import org.odk.collect.forms.instances.Instance
import org.odk.collect.formstest.InstanceFixtures
import org.odk.collect.strings.R.string

@RunWith(AndroidJUnit4::class)
class InstanceListItemViewTest {

    private val context = ApplicationProvider.getApplicationContext<Context>()
    private val layoutInflater = LayoutInflater.from(context)

    @Before
    fun setup() {
        context.setTheme(R.style.Theme_Collect)
    }

    @Test
    fun whenInstanceIsInvalid_showsIncompleteChip() {
        val binding = FormChooserListItemBinding.inflate(layoutInflater)
        val instance = InstanceFixtures.instance(status = Instance.STATUS_INVALID)

        InstanceListItemView.setInstance(binding.root, instance, false)

        assertThat(binding.chip.visibility, equalTo(View.VISIBLE))
        assertThat(binding.chip.text, equalTo(context.getString(string.incomplete)))
    }

    @Test
    fun whenInstanceIsValid_showsCompleteChip() {
        val binding = FormChooserListItemBinding.inflate(layoutInflater)
        val instance = InstanceFixtures.instance(status = Instance.STATUS_VALID)

        InstanceListItemView.setInstance(binding.root, instance, false)

        assertThat(binding.chip.visibility, equalTo(View.VISIBLE))
        assertThat(binding.chip.text, equalTo(context.getString(string.complete)))
    }

    @Test
    fun whenInstanceIsIncomplete_showsIncompleteChip() {
        val binding = FormChooserListItemBinding.inflate(layoutInflater)
        val instance = InstanceFixtures.instance(status = Instance.STATUS_INCOMPLETE)

        InstanceListItemView.setInstance(binding.root, instance, false)

        assertThat(binding.chip.visibility, equalTo(View.VISIBLE))
        assertThat(binding.chip.text, equalTo(context.getString(string.incomplete)))
    }

    @Test
    fun whenInstanceIsComplete_doesNotShowIncompleteChip() {
        val binding = FormChooserListItemBinding.inflate(layoutInflater)
        val instance = InstanceFixtures.instance(status = Instance.STATUS_COMPLETE)

        InstanceListItemView.setInstance(binding.root, instance, false)

        assertThat(binding.chip.visibility, equalTo(View.GONE))
    }

    @Test
    fun chipCanBeRecycled() {
        val binding = FormChooserListItemBinding.inflate(layoutInflater)
        val valid = InstanceFixtures.instance(status = Instance.STATUS_VALID)
        InstanceListItemView.setInstance(binding.root, valid, false)

        assertThat(binding.chip.visibility, equalTo(View.VISIBLE))
        assertThat(binding.chip.text, equalTo(context.getString(string.complete)))

        val invalid = InstanceFixtures.instance(status = Instance.STATUS_INVALID)
        InstanceListItemView.setInstance(binding.root, invalid, false)

        assertThat(binding.chip.visibility, equalTo(View.VISIBLE))
        assertThat(binding.chip.text, equalTo(context.getString(string.incomplete)))
    }
}
