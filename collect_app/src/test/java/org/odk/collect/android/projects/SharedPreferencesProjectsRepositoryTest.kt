package org.odk.collect.android.projects

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.gson.Gson
import org.junit.runner.RunWith
import org.odk.collect.android.preferences.keys.MetaKeys
import org.odk.collect.android.preferences.source.SettingsProvider
import org.odk.collect.android.utilities.UUIDGenerator

@RunWith(AndroidJUnit4::class)
class SharedPreferencesProjectsRepositoryTest : ProjectsRepositoryTest() {
    override fun buildSubject(): ProjectsRepository {
        SettingsProvider(ApplicationProvider.getApplicationContext()).getMetaSettings().remove(MetaKeys.KEY_PROJECTS)
        return SharedPreferencesProjectsRepository(UUIDGenerator(), Gson(), SettingsProvider(ApplicationProvider.getApplicationContext()).getMetaSettings())
    }
}
