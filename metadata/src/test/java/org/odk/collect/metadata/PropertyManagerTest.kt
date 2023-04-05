package org.odk.collect.metadata

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.odk.collect.settings.InMemSettingsProvider
import org.odk.collect.settings.keys.ProjectKeys

class PropertyManagerTest {
    private val installIDProvider = TestInstallIDProvider()
    private val settingsProvider = InMemSettingsProvider()

    private lateinit var propertyManager: PropertyManager

    @Before
    fun setup() {
        propertyManager = PropertyManager(installIDProvider, settingsProvider)
    }

    @Test
    fun `getSingularProperty should return empty string for undefined properties`() {
        assertThat(propertyManager.getSingularProperty("blah"), equalTo(""))
    }

    @Test
    fun `reload should populate properties`() {
        settingsProvider.getUnprotectedSettings().apply {
            save(ProjectKeys.KEY_METADATA_USERNAME, "John")
            save(ProjectKeys.KEY_METADATA_PHONENUMBER, "789")
            save(ProjectKeys.KEY_METADATA_EMAIL, "john@gmail.com")
        }

        propertyManager.reload()

        assertThat(propertyManager.getSingularProperty(PropertyManager.PROPMGR_USERNAME), equalTo("John"))
        assertThat(propertyManager.getSingularProperty(PropertyManager.PROPMGR_PHONE_NUMBER), equalTo("789"))
        assertThat(propertyManager.getSingularProperty(PropertyManager.PROPMGR_EMAIL), equalTo("john@gmail.com"))
        assertThat(propertyManager.getSingularProperty(PropertyManager.PROPMGR_DEVICE_ID), equalTo("123"))
    }

    @Test
    fun `reload should use server username if metadata username is not defined`() {
        settingsProvider.getUnprotectedSettings().save(ProjectKeys.KEY_USERNAME, "Mark")

        propertyManager.reload()

        assertThat(propertyManager.getSingularProperty(PropertyManager.PROPMGR_USERNAME), equalTo("Mark"))
    }

    @Test
    fun `reload should use metadata username if both metadata and server usernames are defined`() {
        settingsProvider.getUnprotectedSettings().apply {
            save(ProjectKeys.KEY_METADATA_USERNAME, "John")
            save(ProjectKeys.KEY_USERNAME, "Mark")
        }

        propertyManager.reload()

        assertThat(propertyManager.getSingularProperty(PropertyManager.PROPMGR_USERNAME), equalTo("John"))
    }

    private class TestInstallIDProvider : InstallIDProvider {
        override val installID: String
            get() = "123"
    }
}
