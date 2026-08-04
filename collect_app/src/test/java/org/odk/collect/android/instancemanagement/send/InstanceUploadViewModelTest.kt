package org.odk.collect.android.instancemanagement.send

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.odk.collect.android.instancemanagement.InstancesDataService
import org.odk.collect.androidtest.TestDispatcherProvider
import org.odk.collect.formstest.InMemInstancesRepository

@RunWith(AndroidJUnit4::class)
class InstanceUploadViewModelTest {
    private val dispatcherProvider = TestDispatcherProvider()
    private val instancesDataService = mock<InstancesDataService>()

    private val viewModel = InstanceUploadViewModel(
        dispatcherProvider,
        mock(),
        InMemInstancesRepository(),
        instancesDataService,
        "projectId",
        "",
        null,
        null,
        null,
        null,
        "Success",
        "Waiting"
    )

    @Test
    fun `isCancelled passed to sendInstances returns true when upload is canceled`() {
        var isCancelledBeforeCancel: Boolean? = null
        var isCancelledAfterCancel: Boolean? = null

        whenever(
            instancesDataService.sendInstances(
                any(), any(), any(), anyOrNull(), anyOrNull(), any(), any(), any()
            )
        ) doAnswer {
            val isCancelled: () -> Boolean = it.getArgument(6)

            isCancelledBeforeCancel = isCancelled()
            viewModel.cancel()
            isCancelledAfterCancel = isCancelled()

            emptyList()
        }

        viewModel.upload(emptyList())
        dispatcherProvider.flush()

        assertThat(isCancelledBeforeCancel, equalTo(false))
        assertThat(isCancelledAfterCancel, equalTo(true))
    }
}
