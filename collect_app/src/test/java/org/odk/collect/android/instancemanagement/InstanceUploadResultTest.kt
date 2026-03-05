package org.odk.collect.android.instancemanagement

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.matcher.ViewMatchers.assertThat
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.forms.instances.Instance
import org.odk.collect.formstest.InMemInstancesRepository

@RunWith(AndroidJUnit4::class)
class InstanceUploadResultTest {
    companion object {
        /**
         * 1000 instances is a big number that would product a very long sql query that would cause
         * SQLiteException: Expression tree is too large if we didn't split it into parts.
         */
        private const val NUMBER_OF_INSTANCES_TO_SEND = 1000
    }

    @Test
    fun toMessage_containsEditNumbers() {
        val instancesRepository = InMemInstancesRepository()
        val resources = ApplicationProvider.getApplicationContext<Context>().resources

        val originalInstance = Instance.Builder()
            .dbId(1L)
            .displayName("InstanceTest")
            .formId("instanceTest")
            .status(Instance.STATUS_COMPLETE)
            .build()

        val editedInstance = Instance.Builder(originalInstance)
            .dbId(originalInstance.dbId + 1)
            .editOf(originalInstance.dbId)
            .editNumber(1L)
            .build()

        instancesRepository.save(originalInstance)
        instancesRepository.save(editedInstance)

        val uploadResults = listOf(
            InstanceUploadResult.Success(originalInstance, "Success"),
            InstanceUploadResult.Success(editedInstance, "Success")
        )

        val actualMessage = uploadResults.toMessage(resources)

        val expectedMessage = "InstanceTest - Success\n\nInstanceTest (Edit 1) - Success"

        assertThat(actualMessage.trim(), equalTo(expectedMessage))
    }

    @Test
    fun toMessage_ContainsAllInstances() {
        val instancesRepository = InMemInstancesRepository()
        val resources = ApplicationProvider.getApplicationContext<Context>().resources

        val uploadResults = (1..NUMBER_OF_INSTANCES_TO_SEND).map { i ->
            val instance = Instance.Builder()
                .dbId(i.toLong())
                .displayName("InstanceTest")
                .formId("instanceTest")
                .status(Instance.STATUS_COMPLETE)
                .lastStatusChangeDate(System.currentTimeMillis())
                .build()
            instancesRepository.save(instance)

            InstanceUploadResult.Success(instance, "Success")
        }

        val actualMessage = uploadResults.toMessage(resources)

        val expectedMessage = buildString {
            repeat(NUMBER_OF_INSTANCES_TO_SEND) {
                append("InstanceTest - Success\n\n")
            }
        }.trim()

        assertThat(actualMessage.trim(), equalTo(expectedMessage))
    }
}
