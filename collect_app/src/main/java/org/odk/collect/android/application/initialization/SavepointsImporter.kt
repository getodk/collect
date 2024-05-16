package org.odk.collect.android.application.initialization

import org.odk.collect.android.projects.ProjectDependencyProviderFactory
import org.odk.collect.android.storage.StorageSubdirectory
import org.odk.collect.forms.FormsRepository
import org.odk.collect.forms.instances.InstancesRepository
import org.odk.collect.forms.savepoints.Savepoint
import org.odk.collect.forms.savepoints.SavepointsRepository
import org.odk.collect.projects.ProjectsRepository
import org.odk.collect.settings.keys.MetaKeys
import org.odk.collect.upgrade.Upgrade
import java.io.File

class SavepointsImporter(
    private val projectsRepository: ProjectsRepository,
    private val projectDependencyProviderFactory: ProjectDependencyProviderFactory
) : Upgrade {
    override fun key(): String {
        return MetaKeys.OLD_SAVEPOINTS_IMPORTED
    }

    override fun run() {
        projectsRepository.getAll().forEach { project ->
            val projectDependencyProvider = projectDependencyProviderFactory.create(project.uuid)

            val cacheDir =
                File(projectDependencyProvider.storagePathProvider.getOdkDirPath(StorageSubdirectory.CACHE, project.uuid))
            val instancesDir =
                File(projectDependencyProvider.storagePathProvider.getOdkDirPath(StorageSubdirectory.INSTANCES, project.uuid))

            importSavepointsThatBelongToSavedForms(projectDependencyProvider.instancesRepository, projectDependencyProvider.formsRepository, projectDependencyProvider.savepointsRepository, cacheDir)

            importSavepointsThatBelongToBlankForms(projectDependencyProvider.formsRepository, projectDependencyProvider.savepointsRepository, cacheDir, instancesDir)
        }
    }

    private fun importSavepointsThatBelongToSavedForms(instancesRepository: InstancesRepository, formsRepository: FormsRepository, savepointsRepository: SavepointsRepository, cacheDir: File) {
        instancesRepository.all.forEach { instance ->
            if (instance.deletedDate == null) {
                val savepointFile = File(cacheDir, File(instance.instanceFilePath).name.plus(".save"))
                if (savepointFile.exists() && savepointFile.lastModified() > instance.lastStatusChangeDate) {
                    val form = formsRepository.getAllByFormIdAndVersion(instance.formId, instance.formVersion).firstOrNull()
                    if (form != null && !form.isDeleted) {
                        savepointsRepository.save(Savepoint(form.dbId, instance.dbId, savepointFile.absolutePath, instance.instanceFilePath))
                    }
                }
            }
        }
    }

    private fun importSavepointsThatBelongToBlankForms(formsRepository: FormsRepository, savepointsRepository: SavepointsRepository, cacheDir: File, instancesDir: File) {
        formsRepository.all.forEach { form ->
            if (!form.isDeleted) {
                val formFileName = File(form.formFilePath).name.substringBeforeLast(".xml")

                cacheDir.listFiles { file ->
                    val match = """${formFileName}_(\d{4}-\d{2}-\d{2}_\d{2}-\d{2}-\d{2})(.xml.save)""".toRegex().matchEntire(file.name)
                    match != null
                }?.forEach { savepointFile ->
                    if (savepointFile.lastModified() > form.date) {
                        val instanceFileName = savepointFile.name.substringBefore(".xml.save")
                        savepointsRepository.save(Savepoint(form.dbId, null, savepointFile.absolutePath, File(instancesDir, "$instanceFileName/$instanceFileName.xml").absolutePath))
                    }
                }
            }
        }
    }
}
