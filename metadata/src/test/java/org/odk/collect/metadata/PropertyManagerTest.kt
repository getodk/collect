package org.odk.collect.metadata

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.odk.collect.permissions.PermissionsProvider
import org.odk.collect.settings.InMemSettingsProvider
import org.odk.collect.settings.keys.ProjectKeys

class PropertyManagerTest {
    private val permissionsProvider = mock<PermissionsProvider>()
    private val deviceDetailsProvider = TestDeviceDetailsProvider()
    private val settingsProvider = InMemSettingsProvider()

    private lateinit var propertyManager: PropertyManager

    @Before
    fun setup() {
        propertyManager = PropertyManager(permissionsProvider, deviceDetailsProvider, settingsProvider)
    }

    @Test
    fun `getSingularProperty should return empty string for undefined properties`() {
        assertThat(propertyManager.getSingularProperty("blah"), equalTo(""))
    }

    @Test
    fun `getSingularProperty should require phone state permission for phone number property only`() {
        propertyManager.reload()

        propertyManager.getSingularProperty(PropertyManager.PROPMGR_DEVICE_ID)
        propertyManager.getSingularProperty(PropertyManager.PROPMGR_USERNAME)
        propertyManager.getSingularProperty(PropertyManager.PROPMGR_EMAIL)

        assertThat(propertyManager.isPhoneStateRequired, equalTo(false))

        propertyManager.getSingularProperty(PropertyManager.PROPMGR_PHONE_NUMBER)

        assertThat(propertyManager.isPhoneStateRequired, equalTo(true))
    }

    @Test
    fun `reload should populate properties`() {
        settingsProvider.getUnprotectedSettings().save(ProjectKeys.KEY_METADATA_USERNAME, "John")
        settingsProvider.getUnprotectedSettings().save(ProjectKeys.KEY_METADATA_EMAIL, "john@gmail.com")

        propertyManager.reload()

        assertThat(propertyManager.getSingularProperty(PropertyManager.PROPMGR_DEVICE_ID), equalTo("123"))
        assertThat(propertyManager.getSingularProperty(PropertyManager.PROPMGR_PHONE_NUMBER), equalTo("789"))
        assertThat(propertyManager.getSingularProperty(PropertyManager.PROPMGR_USERNAME), equalTo("John"))
        assertThat(propertyManager.getSingularProperty(PropertyManager.PROPMGR_EMAIL), equalTo("john@gmail.com"))
    }

    @Test
    fun `reload should use phone number stored in settings instead of the device one if it is set`() {
        settingsProvider.getUnprotectedSettings().save(ProjectKeys.KEY_METADATA_PHONENUMBER, "456")

        propertyManager.reload()

        assertThat(propertyManager.getSingularProperty(PropertyManager.PROPMGR_PHONE_NUMBER), equalTo("456"))
    }

    @Test
    fun `reload should use device phone number if the one stored in settings does not exist or is blank`() {
        // does not exist
        propertyManager.reload()

        assertThat(propertyManager.getSingularProperty(PropertyManager.PROPMGR_PHONE_NUMBER), equalTo("789"))

        // empty
        settingsProvider.getUnprotectedSettings().save(ProjectKeys.KEY_METADATA_PHONENUMBER, "")

        propertyManager.reload()

        assertThat(propertyManager.getSingularProperty(PropertyManager.PROPMGR_PHONE_NUMBER), equalTo("789"))

        // blank
        settingsProvider.getUnprotectedSettings().save(ProjectKeys.KEY_METADATA_PHONENUMBER, " ")

        propertyManager.reload()

        assertThat(propertyManager.getSingularProperty(PropertyManager.PROPMGR_PHONE_NUMBER), equalTo("789"))
    }

    @Test
    fun `reload should use server username if metadata username is not defined`() {
        settingsProvider.getUnprotectedSettings().save(ProjectKeys.KEY_USERNAME, "Mark")

        propertyManager.reload()

        assertThat(propertyManager.getSingularProperty(PropertyManager.PROPMGR_USERNAME), equalTo("Mark"))
    }

    @Test
    fun `reload should use metadata username if both metadata and server usernames are defined`() {
        settingsProvider.getUnprotectedSettings().save(ProjectKeys.KEY_METADATA_USERNAME, "John")
        settingsProvider.getUnprotectedSettings().save(ProjectKeys.KEY_USERNAME, "Mark")

        propertyManager.reload()

        assertThat(propertyManager.getSingularProperty(PropertyManager.PROPMGR_USERNAME), equalTo("John"))
    }

    private class TestDeviceDetailsProvider : DeviceDetailsProvider {
        override val deviceId: String
            get() = "123"

        override val line1Number: String
            get() = "789"
    }
}
