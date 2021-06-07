package org.odk.collect.projects

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.gson.Gson
import org.junit.runner.RunWith
import org.odk.collect.shared.strings.UUIDGenerator
import org.odk.collect.testshared.InMemSettings

@RunWith(AndroidJUnit4::class)
class SharedPreferencesProjectsRepositoryTest : ProjectsRepositoryTest() {
    override fun buildSubject(): ProjectsRepository {
        return SharedPreferencesProjectsRepository(UUIDGenerator(), Gson(), InMemSettings(), "test")
    }
}
