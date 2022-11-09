package org.odk.collect.android.entities

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.odk.collect.entities.EntitiesRepository
import org.odk.collect.entities.Entity

abstract class EntitiesRepositoryTest {

    abstract fun buildSubject(): EntitiesRepository

    @Test
    fun `getEntities returns entities for dataset`() {
        val repository = buildSubject()

        val wine = Entity("wines", emptyList())
        val whisky = Entity("whiskys", emptyList())
        repository.save(wine)
        repository.save(whisky)

        val wines = repository.getEntities("wines")
        assertThat(wines.size, equalTo(1))
        assertThat(wines[0], equalTo(wine))

        val whiskys = repository.getEntities("whiskys")
        assertThat(whiskys.size, equalTo(1))
        assertThat(whiskys[0], equalTo(whisky))
    }
}
