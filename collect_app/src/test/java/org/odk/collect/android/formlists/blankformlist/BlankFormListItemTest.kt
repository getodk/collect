package org.odk.collect.android.formlists.blankformlist

import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.forms.Form
import org.odk.collect.forms.instances.Instance
import org.odk.collect.formstest.InMemInstancesRepository
import org.odk.collect.projects.Project

@RunWith(AndroidJUnit4::class)
class BlankFormListItemTest {
    private val instancesRepository = InMemInstancesRepository()

    @Test
    fun `Form should be properly converted to BlankFormListItem`() {
        val form = Form.Builder()
            .dbId(1)
            .formId("1")
            .displayName("Sample form B")
            .version("1")
            .geometryXpath("blah")
            .date(1665742651522)
            .build()

        instancesRepository.save(
            Instance.Builder()
                .formId(form.formId)
                .formVersion(form.version)
                .lastStatusChangeDate(3)
                .build()
        )

        val blankFormListItem = form.toBlankFormListItem(Project.DEMO_PROJECT_ID, instancesRepository)

        assertThat(blankFormListItem.databaseId, `is`(form.dbId))
        assertThat(blankFormListItem.formId, `is`(form.formId))
        assertThat(blankFormListItem.formName, `is`(form.displayName))
        assertThat(blankFormListItem.formVersion, `is`(form.version))
        assertThat(blankFormListItem.geometryPath, `is`(form.geometryXpath))
        assertThat(blankFormListItem.dateOfCreation, `is`(form.date))
        assertThat(blankFormListItem.dateOfLastUsage, `is`(3L))
        assertThat(blankFormListItem.contentUri, `is`(Uri.parse("content://org.odk.collect.android.provider.odk.forms/forms/1?projectId=DEMO")))
    }

    @Test
    fun `When there are no saved instances dateOfLastUsage in BlankFormListItem should be set to 0`() {
        val form = Form.Builder()
            .dbId(1)
            .formId("1")
            .displayName("Sample form")
            .date(1665742651521)
            .build()

        val blankFormListItem = form.toBlankFormListItem(Project.DEMO_PROJECT_ID, instancesRepository)

        assertThat(blankFormListItem.dateOfLastUsage, `is`(0L))
    }

    @Test
    fun `When version in form is not set should be represented as an empty sting in BlankFormListItem`() {
        val form = Form.Builder()
            .dbId(1)
            .formId("1")
            .displayName("Sample form")
            .date(1665742651521)
            .build()

        val blankFormListItem = form.toBlankFormListItem(Project.DEMO_PROJECT_ID, instancesRepository)

        assertThat(blankFormListItem.formVersion, `is`(""))
    }

    @Test
    fun `When geometryXpath in form is not set should be represented as an empty sting in BlankFormListItem`() {
        val form = Form.Builder()
            .dbId(1)
            .formId("1")
            .displayName("Sample form")
            .date(1665742651521)
            .build()

        val blankFormListItem = form.toBlankFormListItem(Project.DEMO_PROJECT_ID, instancesRepository)

        assertThat(blankFormListItem.geometryPath, `is`(""))
    }

    @Test
    fun `dateOfLastUsage should be set taking into account forms saved not only for given formId but also formVersion`() {
        val formV1 = Form.Builder()
            .dbId(1)
            .formId("1")
            .displayName("Sample form v1")
            .version("1")
            .geometryXpath("blah")
            .date(1665742651521)
            .build()

        val formV2 = Form.Builder(formV1)
            .dbId(2)
            .displayName("Sample form v2")
            .version("2")
            .date(1665742651522)
            .build()

        instancesRepository.save(
            Instance.Builder()
                .formId(formV1.formId)
                .formVersion(formV1.version)
                .lastStatusChangeDate(5)
                .build()
        )

        instancesRepository.save(
            Instance.Builder()
                .formId(formV2.formId)
                .formVersion(formV2.version)
                .lastStatusChangeDate(3)
                .build()
        )

        instancesRepository.save(
            Instance.Builder()
                .formId(formV2.formId)
                .formVersion(formV2.version)
                .lastStatusChangeDate(4)
                .build()
        )

        val blankFormListItem = formV2.toBlankFormListItem(Project.DEMO_PROJECT_ID, instancesRepository)

        assertThat(blankFormListItem.dateOfLastUsage, `is`(4L))
    }
}
