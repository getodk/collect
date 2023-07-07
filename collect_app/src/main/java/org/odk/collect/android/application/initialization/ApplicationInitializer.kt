package org.odk.collect.android.application.initialization

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import androidx.startup.AppInitializer
import net.danlew.android.joda.JodaTimeInitializer
import org.javarosa.core.model.CoreModelModule
import org.javarosa.core.model.DataBinding
import org.javarosa.core.model.FormDef
import org.javarosa.core.model.QuestionDef
import org.javarosa.core.services.PrototypeManager
import org.javarosa.core.util.JavaRosaCoreModule
import org.javarosa.core.util.externalizable.ExtUtil
import org.javarosa.core.util.externalizable.Externalizable
import org.javarosa.core.util.externalizable.PrototypeFactory
import org.javarosa.entities.EntityXFormParserFactory
import org.javarosa.model.xform.XFormsModule
import org.javarosa.xform.parse.XFormParser
import org.javarosa.xform.parse.XFormParser.BindAttributeProcessor
import org.javarosa.xform.parse.XFormParser.FormDefProcessor
import org.javarosa.xform.parse.XFormParser.QuestionProcessor
import org.javarosa.xform.parse.XFormParserFactory
import org.javarosa.xform.util.XFormUtils
import org.kxml2.kdom.Document
import org.odk.collect.analytics.Analytics
import org.odk.collect.android.BuildConfig
import org.odk.collect.android.application.Collect
import org.odk.collect.android.application.initialization.upgrade.UpgradeInitializer
import org.odk.collect.android.dynamicpreload.ExternalDataUtil
import org.odk.collect.android.dynamicpreload.handler.ExternalDataHandlerPull
import org.odk.collect.android.logic.actions.setgeopoint.CollectSetGeopointActionHandler
import org.odk.collect.metadata.PropertyManager
import org.odk.collect.projects.ProjectsRepository
import org.odk.collect.settings.SettingsProvider
import timber.log.Timber
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.Reader
import java.util.Locale

class ApplicationInitializer(
    private val context: Application,
    private val propertyManager: PropertyManager,
    private val analytics: Analytics,
    private val upgradeInitializer: UpgradeInitializer,
    private val analyticsInitializer: AnalyticsInitializer,
    private val mapsInitializer: MapsInitializer,
    private val projectsRepository: ProjectsRepository,
    private val settingsProvider: SettingsProvider
) {
    fun initialize() {
        initializeLocale()
        runInitializers()
        initializeFrameworks()
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
        mapsInitializer.initialize()
    }

    private fun initializeFrameworks() {
        initializeLogging()
        AppInitializer.getInstance(context).initializeComponent(JodaTimeInitializer::class.java)
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true)
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

        // Configure default parser factory
        val entityXFormParserFactory = EntityXFormParserFactory(XFormParserFactory())
        val pullDataDetectorXFormParserFactory = object : XFormParserFactory() {
            override fun getXFormParser(reader: Reader?): XFormParser {
                return configureDetector(entityXFormParserFactory.getXFormParser(reader))
            }

            override fun getXFormParser(doc: Document?): XFormParser {
                return configureDetector(entityXFormParserFactory.getXFormParser(doc))
            }

            override fun getXFormParser(form: Reader?, instance: Reader?): XFormParser {
                return configureDetector(entityXFormParserFactory.getXFormParser(form, instance))
            }

            override fun getXFormParser(form: Document?, instance: Document?): XFormParser {
                return configureDetector(entityXFormParserFactory.getXFormParser(form, instance))
            }

            private fun configureDetector(xFormParser: XFormParser): XFormParser {
                xFormParser.addProcessor(object : BindAttributeProcessor, QuestionProcessor, FormDefProcessor {

                    private var containsSearchOrPullData = false

                    override fun getBindAttributes(): Set<Pair<String, String>> {
                        return setOf(Pair("", "calculate"))
                    }

                    override fun processBindAttribute(
                        name: String,
                        value: String,
                        binding: DataBinding
                    ) {
                        val bindingCalculate = binding.calculate
                        if (
                            bindingCalculate != null &&
                            bindingCalculate.expr.expr.containsFunc(ExternalDataHandlerPull.HANDLER_NAME)
                        ) {
                            containsSearchOrPullData = true
                        }
                    }

                    override fun processQuestion(question: QuestionDef) {
                        if (ExternalDataUtil.getSearchXPathExpression(question.appearanceAttr) != null) {
                            containsSearchOrPullData = true
                        }
                    }

                    override fun processFormDef(formDef: FormDef) {
                        formDef.extras.put(ContainsSearchOrPullDataExtra(containsSearchOrPullData))
                    }
                })

                return xFormParser
            }
        }

        XFormUtils.setXFormParserFactory(pullDataDetectorXFormParserFactory)
    }

    private fun initializeLogging() {
        if (BuildConfig.BUILD_TYPE == "odkCollectRelease") {
            Timber.plant(CrashReportingTree(analytics))
        } else {
            Timber.plant(Timber.DebugTree())
        }
    }
}

class ContainsSearchOrPullDataExtra(containsPullData: Boolean) : Externalizable {

    constructor() : this(false)

    var containsSearchOrPullData = containsPullData
        private set

    override fun readExternal(`in`: DataInputStream?, pf: PrototypeFactory?) {
        containsSearchOrPullData = ExtUtil.read(`in`, Boolean::class.java) as Boolean
    }

    override fun writeExternal(out: DataOutputStream?) {
        ExtUtil.write(out, containsSearchOrPullData)
    }
}
