package org.odk.collect.android.formentry

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppStateFormSessionRepositoryTest : FormSessionRepositoryTest() {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private val appStateFormSessionRepository =
        AppStateFormSessionRepository(ApplicationProvider.getApplicationContext())
    override val formSessionRepository: FormSessionRepository
        get() = appStateFormSessionRepository
}
