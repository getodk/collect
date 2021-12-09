package org.odk.collect.android.application.initialization

import android.app.Application
import android.os.Handler
import androidx.appcompat.app.AppCompatDelegate
import androidx.startup.AppInitializer
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.MapsInitializer
import net.danlew.android.joda.JodaTimeInitializer
import org.javarosa.core.model.CoreModelModule
import org.javarosa.core.services.PrototypeManager
import org.javarosa.core.util.JavaRosaCoreModule
import org.javarosa.model.xform.XFormsModule
import org.javarosa.xform.parse.XFormParser
import org.odk.collect.analytics.Analytics
import org.odk.collect.android.BuildConfig
import org.odk.collect.android.application.Collect
import org.odk.collect.android.application.initialization.upgrade.UpgradeInitializer
import org.odk.collect.android.geo.MapboxUtils
import org.odk.collect.android.logic.PropertyManager
import org.odk.collect.android.logic.actions.setgeopoint.CollectSetGeopointActionHandler
import org.odk.collect.android.preferences.source.SettingsProvider
import org.odk.collect.projects.ProjectsRepository
import org.odk.collect.utilities.UserAgentProvider
import org.osmdroid.config.Configuration
import timber.log.Timber
import java.util.Locale

class ApplicationInitializer(
    private val context: Application,
    private val userAgentProvider: UserAgentProvider,
    private val propertyManager: PropertyManager,
    private val analytics: Analytics,
    private val upgradeInitializer: UpgradeInitializer,
    private val analyticsInitializer: AnalyticsInitializer,
    private val projectsRepository: ProjectsRepository,
    private val settingsProvider: SettingsProvider
) {
    fun initialize() {
        runInitializers()
        initializeFrameworks()
        initializeLocale()
    }

    private fun runInitializers() {
        upgradeInitializer.initialize()
        analyticsInitializer.initialize()
        UserPropertiesInitializer(
            analytics,
            projectsRepository,
            settingsProvider,
            context
        ).initialize()
    }

    private fun initializeFrameworks() {
        initializeLogging()
        AppInitializer.getInstance(context).initializeComponent(JodaTimeInitializer::class.java)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
        initializeMapFrameworks()
        initializeJavaRosa()
    }

    private fun initializeLocale() {
        Collect.defaultSysLanguage = Locale.getDefault().language
    }

    private fun initializeJavaRosa() {
        propertyManager.reload()
        org.javarosa.core.services.PropertyManager
            .setPropertyManager(propertyManager)

        // Register prototypes for classes that FormDef uses
        PrototypeManager.registerPrototypes(JavaRosaCoreModule.classNames)
        PrototypeManager.registerPrototypes(CoreModelModule.classNames)
        XFormsModule().registerModule()

        // When registering prototypes from Collect, a proguard exception also needs to be added
        PrototypeManager.registerPrototype("org.odk.collect.android.logic.actions.setgeopoint.CollectSetGeopointAction")
        XFormParser.registerActionHandler(
            CollectSetGeopointActionHandler.ELEMENT_NAME,
            CollectSetGeopointActionHandler()
        )
    }

    private fun initializeLogging() {
        if (BuildConfig.BUILD_TYPE == "odkCollectRelease") {
            Timber.plant(CrashReportingTree(analytics))
        } else {
            Timber.plant(Timber.DebugTree())
        }
    }

    private fun initializeMapFrameworks() {
        try {
            MapsInitializer.initialize(
                context,
                MapsInitializer.Renderer.LATEST
            ) { renderer: MapsInitializer.Renderer ->
                when (renderer) {
                    MapsInitializer.Renderer.LATEST -> Timber.d("The latest version of Google Maps renderer is used.")
                    MapsInitializer.Renderer.LEGACY -> Timber.d("The legacy version of Google Maps renderer is used.")
                }
            }
            val handler = Handler(context.mainLooper)
            handler.post {
                // This has to happen on the main thread but we might call `initialize` from tests
                MapView(context).onCreate(null)
            }
            Configuration.getInstance().userAgentValue = userAgentProvider.userAgent
            MapboxUtils.initMapbox()
        } catch (ignore: Exception) {
            // ignored
        } catch (ignore: Error) {
            // ignored
        }
    }
}
