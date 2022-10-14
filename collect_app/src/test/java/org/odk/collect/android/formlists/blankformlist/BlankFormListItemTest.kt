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
        instancesRepository.save(
            Instance.Builder()
                .formId("0")
                .lastStatusChangeDate(5)
                .build()
        )

        instancesRepository.save(
            Instance.Builder()
                .formId("1")
                .lastStatusChangeDate(3)
                .build()
        )

        instancesRepository.save(
            Instance.Builder()
                .formId("1")
                .lastStatusChangeDate(4)
                .build()
        )

        val form = Form.Builder()
            .dbId(1)
            .formId("1")
            .displayName("Sample form")
            .version("1")
            .geometryXpath("blah")
            .date(1665742651521)
            .build()

        val blankFormListItem = formToBlankFormListItem(form, Project.DEMO_PROJECT_ID, instancesRepository)

        assertThat(blankFormListItem.databaseId, `is`(form.dbId))
        assertThat(blankFormListItem.formId, `is`(form.formId))
        assertThat(blankFormListItem.formName, `is`(form.displayName))
        assertThat(blankFormListItem.formVersion, `is`(form.version))
        assertThat(blankFormListItem.geometryPath, `is`(form.geometryXpath))
        assertThat(blankFormListItem.dateOfCreation, `is`(form.date))
        assertThat(blankFormListItem.dateOfLastUsage, `is`(4L))
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

        val blankFormListItem = formToBlankFormListItem(form, Project.DEMO_PROJECT_ID, instancesRepository)

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

        val blankFormListItem = formToBlankFormListItem(form, Project.DEMO_PROJECT_ID, instancesRepository)

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

        val blankFormListItem = formToBlankFormListItem(form, Project.DEMO_PROJECT_ID, instancesRepository)

        assertThat(blankFormListItem.geometryPath, `is`(""))
    }
}
