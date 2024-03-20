package org.odk.collect.android.savepoints

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.android.external.FormsContract
import org.odk.collect.android.external.InstancesContract
import org.odk.collect.forms.Form
import org.odk.collect.forms.instances.Instance
import org.odk.collect.forms.savepoints.Savepoint
import org.odk.collect.formstest.FormUtils
import org.odk.collect.formstest.InMemFormsRepository
import org.odk.collect.formstest.InMemInstancesRepository
import org.odk.collect.formstest.InMemSavepointsRepository
import org.odk.collect.shared.TempFiles
import java.io.File

@RunWith(AndroidJUnit4::class)
class SavepointUseCasesTest {
    private var formV1: Form
    private var formV2: Form

    private var instance1: Instance
    private var instance2: Instance

    private val formsRepository = InMemFormsRepository().apply {
        formV1 = save(
            FormUtils.buildForm(
                "1",
                "1",
                TempFiles.createTempDir().absolutePath
            ).build()
        )

        Thread.sleep(100) // make sure the two forms have different creation dates

        formV2 = save(
            FormUtils.buildForm(
                "1",
                "2",
                TempFiles.createTempDir().absolutePath
            ).build()
        )
    }
    private val instancesRepository = InMemInstancesRepository().apply {
        instance1 = save(
            Instance.Builder()
                .formId("1")
                .formVersion("2")
                .instanceFilePath(TempFiles.createTempFile(TempFiles.createTempDir()).absolutePath)
                .status(Instance.STATUS_INCOMPLETE)
                .build()
        )

        Thread.sleep(100) // make sure the two instances have different creation dates

        instance2 = save(
            Instance.Builder()
                .formId("1")
                .formVersion("2")
                .instanceFilePath(TempFiles.createTempFile(TempFiles.createTempDir()).absolutePath)
                .status(Instance.STATUS_INCOMPLETE)
                .build()
        )
    }
    private val savepointsRepository = InMemSavepointsRepository()

    @Test
    fun `getSavepoint called with the old form version uri returns null if only the new form version has a savepoint`() {
        val savepointFile = createSavepointFile()
        val savepoint = Savepoint(formV2.dbId, null, savepointFile.absolutePath, "")
        savepointsRepository.save(savepoint)

        if (savepointsRepository.getAll().size > 1) {
            throw(Error("WTF?"))
        }

        assertThat(
            SavepointUseCases.getSavepoint(
                FormsContract.getUri("1", formV1.dbId),
                FormsContract.CONTENT_ITEM_TYPE,
                formsRepository,
                instancesRepository,
                savepointsRepository
            ),
            equalTo(null)
        )
    }

    @Test
    fun `getSavepoint called with the old form version uri returns savepoint if it has one`() {
        val savepointFile = createSavepointFile()
        val savepoint = Savepoint(formV1.dbId, null, savepointFile.absolutePath, "")
        savepointsRepository.save(savepoint)

        assertThat(
            SavepointUseCases.getSavepoint(
                FormsContract.getUri("1", formV1.dbId),
                FormsContract.CONTENT_ITEM_TYPE,
                formsRepository,
                instancesRepository,
                savepointsRepository
            ),
            equalTo(savepoint)
        )
    }

    @Test
    fun `getSavepoint called with the old form version uri returns savepoint that belongs to the old form if both form versions have ones`() {
        val savepointFile1 = createSavepointFile()
        val savepoint1 = Savepoint(formV1.dbId, null, savepointFile1.absolutePath, "")
        savepointsRepository.save(savepoint1)

        val savepointFile2 = createSavepointFile()
        val savepoint2 = Savepoint(formV2.dbId, null, savepointFile2.absolutePath, "")
        savepointsRepository.save(savepoint2)

        assertThat(
            SavepointUseCases.getSavepoint(
                FormsContract.getUri("1", formV1.dbId),
                FormsContract.CONTENT_ITEM_TYPE,
                formsRepository,
                instancesRepository,
                savepointsRepository
            ),
            equalTo(savepoint1)
        )
    }

    @Test
    fun `getSavepoint called with the new form version uri returns savepoint if it has one`() {
        val savepointFile = createSavepointFile()
        val savepoint = Savepoint(formV2.dbId, null, savepointFile.absolutePath, "")
        savepointsRepository.save(savepoint)

        assertThat(
            SavepointUseCases.getSavepoint(
                FormsContract.getUri("1", formV2.dbId),
                FormsContract.CONTENT_ITEM_TYPE,
                formsRepository,
                instancesRepository,
                savepointsRepository
            ),
            equalTo(savepoint)
        )
    }

    @Test
    fun `getSavepoint called with the new form version uri returns savepoint that belongs to the old form if only the old form has one`() {
        val savepointFile = createSavepointFile()
        val savepoint = Savepoint(formV1.dbId, null, savepointFile.absolutePath, "")
        savepointsRepository.save(savepoint)

        assertThat(
            SavepointUseCases.getSavepoint(
                FormsContract.getUri("1", formV2.dbId),
                FormsContract.CONTENT_ITEM_TYPE,
                formsRepository,
                instancesRepository,
                savepointsRepository
            ),
            equalTo(savepoint)
        )
    }

    @Test
    fun `getSavepoint called with the new form version uri returns savepoint that belongs to the new form if both form versions have ones`() {
        val savepointFile1 = createSavepointFile()
        val savepoint1 = Savepoint(formV1.dbId, null, savepointFile1.absolutePath, "")
        savepointsRepository.save(savepoint1)

        val savepointFile2 = createSavepointFile()
        val savepoint2 = Savepoint(formV2.dbId, null, savepointFile2.absolutePath, "")
        savepointsRepository.save(savepoint2)

        assertThat(
            SavepointUseCases.getSavepoint(
                FormsContract.getUri("1", formV2.dbId),
                FormsContract.CONTENT_ITEM_TYPE,
                formsRepository,
                instancesRepository,
                savepointsRepository
            ),
            equalTo(savepoint2)
        )
    }

    @Test
    fun `getSavepoint returns null if savepoint exists in the database but the file does not`() {
        val savepointFile = createSavepointFile()
        val savepoint = Savepoint(formV2.dbId, null, savepointFile.absolutePath, "")
        savepointsRepository.save(savepoint)
        savepointFile.delete()

        assertThat(
            SavepointUseCases.getSavepoint(
                FormsContract.getUri("1", formV2.dbId),
                FormsContract.CONTENT_ITEM_TYPE,
                formsRepository,
                instancesRepository,
                savepointsRepository
            ),
            equalTo(null)
        )
    }

    @Test
    fun `getSavepoint called for a saved form uri returns null if only its blank form has a savepoint`() {
        val savepointFile = createSavepointFile()
        val savepoint = Savepoint(formV2.dbId, null, savepointFile.absolutePath, instance1.instanceFilePath)
        savepointsRepository.save(savepoint)

        assertThat(
            SavepointUseCases.getSavepoint(
                InstancesContract.getUri("1", instance1.dbId),
                InstancesContract.CONTENT_ITEM_TYPE,
                formsRepository,
                instancesRepository,
                savepointsRepository
            ),
            equalTo(null)
        )
    }

    @Test
    fun `getSavepoint called for a saved form uri returns savepoint if it has one`() {
        val savepointFile = createSavepointFile()
        val savepoint = Savepoint(formV2.dbId, instance1.dbId, savepointFile.absolutePath, instance1.instanceFilePath)
        savepointsRepository.save(savepoint)

        assertThat(
            SavepointUseCases.getSavepoint(
                InstancesContract.getUri("1", instance1.dbId),
                InstancesContract.CONTENT_ITEM_TYPE,
                formsRepository,
                instancesRepository,
                savepointsRepository
            ),
            equalTo(savepoint)
        )
    }

    @Test
    fun `getSavepoint called for a saved form uri returns null if savepoint exists in the database but the file does not`() {
        val savepointFile = createSavepointFile()
        val savepoint = Savepoint(formV2.dbId, instance1.dbId, savepointFile.absolutePath, instance1.instanceFilePath)
        savepointsRepository.save(savepoint)
        savepointFile.delete()

        assertThat(
            SavepointUseCases.getSavepoint(
                InstancesContract.getUri("1", instance1.dbId),
                InstancesContract.CONTENT_ITEM_TYPE,
                formsRepository,
                instancesRepository,
                savepointsRepository
            ),
            equalTo(null)
        )
    }

    @Test
    fun `getSavepoint called for a saved form uri returns null if it has one but the instance file has been modified later`() {
        val savepointFile = createSavepointFile()
        val savepoint = Savepoint(formV2.dbId, instance1.dbId, savepointFile.absolutePath, instance1.instanceFilePath)
        savepointsRepository.save(savepoint)

        instancesRepository.save(instance1)

        assertThat(
            SavepointUseCases.getSavepoint(
                InstancesContract.getUri("1", instance1.dbId),
                InstancesContract.CONTENT_ITEM_TYPE,
                formsRepository,
                instancesRepository,
                savepointsRepository
            ),
            equalTo(null)
        )
    }

    @Test
    fun `getSavepoint called for a saved form uri returns savepoint that belongs to that saved form if there are more savepoints in the database created for saved forms`() {
        val savepointFile1 = createSavepointFile()
        val savepoint1 = Savepoint(formV2.dbId, instance1.dbId, savepointFile1.absolutePath, instance1.instanceFilePath)
        savepointsRepository.save(savepoint1)

        val savepointFile2 = createSavepointFile()
        val savepoint2 = Savepoint(formV2.dbId, instance2.dbId, savepointFile2.absolutePath, instance2.instanceFilePath)
        savepointsRepository.save(savepoint2)

        assertThat(
            SavepointUseCases.getSavepoint(
                InstancesContract.getUri("1", instance1.dbId),
                InstancesContract.CONTENT_ITEM_TYPE,
                formsRepository,
                instancesRepository,
                savepointsRepository
            ),
            equalTo(savepoint1)
        )
    }

    /**
     * Tests seem to run fast enough so that sometimes two files created in succession end up with
     * the same creation date. This isn't ideal as we depend on distinguishing the older files.
     */
    private fun createSavepointFile(): File {
        Thread.sleep(100)
        val savepointFile = TempFiles.createTempFile()
        Thread.sleep(100)
        return savepointFile
    }
}
