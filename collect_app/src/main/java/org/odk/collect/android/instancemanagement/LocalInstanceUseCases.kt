package org.odk.collect.android.instancemanagement

import org.odk.collect.forms.instances.InstancesRepository

object LocalInstanceUseCases {

    fun reset(instancesRepository: InstancesRepository) {
        instancesRepository.all.forEach {
            if (it.canDelete()) {
                instancesRepository.delete(it.dbId)
            }
        }
    }
}
