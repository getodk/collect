package org.odk.collect.android.formentry

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import org.junit.Rule
import org.odk.collect.android.formentry.support.InMemFormSessionRepository

class InMemoryFormSessionRepositoryTest : FormSessionRepositoryTest() {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val inMemFormSessionRepository = InMemFormSessionRepository()
    override val formSessionRepository: FormSessionRepository
        get() = inMemFormSessionRepository
}
