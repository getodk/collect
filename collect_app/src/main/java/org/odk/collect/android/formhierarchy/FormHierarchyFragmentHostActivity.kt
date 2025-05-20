package org.odk.collect.android.formhierarchy

import android.os.Bundle
import org.odk.collect.analytics.Analytics
import org.odk.collect.android.R
import org.odk.collect.android.activities.FormEntryViewModelFactory
import org.odk.collect.android.entities.EntitiesRepositoryProvider
import org.odk.collect.android.formentry.FormOpeningMode
import org.odk.collect.android.formentry.FormSessionRepository
import org.odk.collect.android.formentry.repeats.DeleteRepeatDialogFragment
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.instancemanagement.InstancesDataService
import org.odk.collect.android.instancemanagement.autosend.AutoSendSettingsProvider
import org.odk.collect.android.projects.ProjectsDataService
import org.odk.collect.android.utilities.ChangeLockProvider
import org.odk.collect.android.utilities.FormsRepositoryProvider
import org.odk.collect.android.utilities.InstancesRepositoryProvider
import org.odk.collect.android.utilities.MediaUtils
import org.odk.collect.android.utilities.SavepointsRepositoryProvider
import org.odk.collect.androidshared.ui.FragmentFactoryBuilder
import org.odk.collect.androidshared.ui.SnackbarUtils.showLongSnackbar
import org.odk.collect.async.Scheduler
import org.odk.collect.audiorecorder.recording.AudioRecorder
import org.odk.collect.location.LocationClient
import org.odk.collect.permissions.PermissionsChecker
import org.odk.collect.permissions.PermissionsProvider
import org.odk.collect.printer.HtmlPrinter
import org.odk.collect.qrcode.zxing.QRCodeCreatorImpl
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.strings.localization.LocalizedActivity
import javax.inject.Inject

class FormHierarchyFragmentHostActivity : LocalizedActivity() {

    @Inject
    lateinit var scheduler: Scheduler

    @Inject
    lateinit var formSessionRepository: FormSessionRepository

    @Inject
    lateinit var mediaUtils: MediaUtils

    @Inject
    lateinit var analytics: Analytics

    @Inject
    lateinit var audioRecorder: AudioRecorder

    @Inject
    lateinit var projectsDataService: ProjectsDataService

    @Inject
    lateinit var entitiesRepositoryProvider: EntitiesRepositoryProvider

    @Inject
    lateinit var permissionsChecker: PermissionsChecker

    @Inject
    lateinit var fusedLocationClient: LocationClient

    @Inject
    lateinit var settingsProvider: SettingsProvider

    @Inject
    lateinit var permissionsProvider: PermissionsProvider

    @Inject
    lateinit var autoSendSettingsProvider: AutoSendSettingsProvider

    @Inject
    lateinit var instancesRepositoryProvider: InstancesRepositoryProvider

    @Inject
    lateinit var formsRepositoryProvider: FormsRepositoryProvider

    @Inject
    lateinit var savepointsRepositoryProvider: SavepointsRepositoryProvider

    @Inject
    lateinit var instancesDataService: InstancesDataService

    @Inject
    lateinit var changeLockProvider: ChangeLockProvider

    private val sessionId by lazy { intent.getStringExtra(EXTRA_SESSION_ID)!! }
    private val viewModelFactory by lazy {
        FormEntryViewModelFactory(
            this,
            FormOpeningMode.EDIT_SAVED,
            sessionId,
            scheduler,
            formSessionRepository,
            mediaUtils,
            audioRecorder,
            projectsDataService,
            entitiesRepositoryProvider,
            settingsProvider,
            permissionsChecker,
            fusedLocationClient,
            permissionsProvider,
            autoSendSettingsProvider,
            formsRepositoryProvider,
            instancesRepositoryProvider,
            savepointsRepositoryProvider,
            QRCodeCreatorImpl(),
            HtmlPrinter(),
            instancesDataService,
            changeLockProvider
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        DaggerUtils.getComponent(this).inject(this)

        val viewOnly = intent.getBooleanExtra(EXTRA_VIEW_ONLY, false)
        supportFragmentManager.fragmentFactory = FragmentFactoryBuilder()
            .forClass(FormHierarchyFragment::class) {
                FormHierarchyFragment(
                    viewOnly,
                    viewModelFactory,
                    this,
                    scheduler,
                    instancesDataService,
                    projectsDataService.getCurrentProject().value!!.uuid
                )
            }
            .forClass(DeleteRepeatDialogFragment::class) {
                DeleteRepeatDialogFragment(viewModelFactory)
            }
            .build()

        if (formSessionRepository.get(sessionId).value == null) {
            super.onCreate(null)
            finish()
            return
        } else {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.hierarchy_host_layout)
            setSupportActionBar(findViewById(org.odk.collect.androidshared.R.id.toolbar))
        }

        val shouldShowNewEditMessage = intent.getBooleanExtra(SHOW_NEW_EDIT_MESSAGE, false)
        if (shouldShowNewEditMessage) {
            showLongSnackbar(
                findViewById(R.id.fragment_container),
                getString(org.odk.collect.strings.R.string.finalized_form_edit_started),
                null,
                null,
                true
            )
        }
    }

    companion object {
        const val EXTRA_SESSION_ID = "session_id"
        const val EXTRA_VIEW_ONLY = "view_only"
        const val SHOW_NEW_EDIT_MESSAGE = "show_new_edit_message"
    }
}
