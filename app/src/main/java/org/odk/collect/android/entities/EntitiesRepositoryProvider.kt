package org.odk.collect.android.entities

import android.app.Application
import org.odk.collect.androidshared.data.getState
import org.odk.collect.entities.EntitiesRepository

class EntitiesRepositoryProvider(application: Application) {

    private val repositories =
        application.getState().get(MAP_KEY, mutableMapOf<String, EntitiesRepository>())

    fun get(projectId: String): EntitiesRepository {
        return repositories.getOrPut(projectId) {
            InMemEntitiesRepository()
        }
    }

    companion object {
        private const val MAP_KEY = "entities_repository_map"
    }
}
