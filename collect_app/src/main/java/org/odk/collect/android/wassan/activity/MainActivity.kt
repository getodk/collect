package org.odk.collect.android.wassan.activity

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.odk.collect.android.R
import org.odk.collect.android.activities.ActivityUtils
import org.odk.collect.android.activities.FormDownloadListActivity
import org.odk.collect.android.application.MapboxClassInstanceCreator
import org.odk.collect.android.formmanagement.FormFillingIntentFactory
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.mainmenu.CurrentProjectViewModel
import org.odk.collect.android.mainmenu.MainMenuActivity
import org.odk.collect.android.mainmenu.MainMenuViewModelFactory
import org.odk.collect.android.projects.ProjectIconView
import org.odk.collect.android.projects.ProjectSettingsDialog
import org.odk.collect.android.projects.ProjectsDataService
import org.odk.collect.android.wassan.fragments.AccountFragment
import org.odk.collect.android.wassan.fragments.CalculatorFragment
import org.odk.collect.android.wassan.fragments.CommunityFragment
import org.odk.collect.android.wassan.fragments.DashboardFragment
import org.odk.collect.android.wassan.fragments.OnFormSelectedListener
import org.odk.collect.android.wassan.model.User
import org.odk.collect.androidshared.system.IntentLauncher
import org.odk.collect.androidshared.ui.DialogFragmentUtils
import org.odk.collect.androidshared.ui.ToastUtils
import org.odk.collect.androidshared.ui.multiclicksafe.MultiClickGuard
import org.odk.collect.projects.Project
import org.odk.collect.projects.ProjectsRepository
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.settings.keys.MetaKeys
import org.odk.collect.settings.keys.ProjectKeys
import org.odk.collect.strings.localization.LocalizedActivity
import timber.log.Timber
import javax.inject.Inject

class MainActivity : LocalizedActivity(), NavigationView.OnNavigationItemSelectedListener,
    OnFormSelectedListener {
    @Inject
    lateinit var settingsProvider: SettingsProvider

    @Inject
    lateinit var projectsRepository: ProjectsRepository

    @Inject
    lateinit var projectsDataService: ProjectsDataService

    @Inject
    lateinit var viewModelFactory: MainMenuViewModelFactory

    @Inject
    lateinit var intentLauncher: IntentLauncher

    private lateinit var currentProjectViewModel: CurrentProjectViewModel

    private val GOOGLE_PLAY_URL = "https://play.google.com/store/apps/details?id="

    private val formLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            setResult(RESULT_OK, it.data)
            //finish()
        }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        DaggerUtils.getComponent(this).inject(this)

        setContentView(R.layout.activity_main)

        val viewModelProvider = ViewModelProvider(this, viewModelFactory)
        currentProjectViewModel = viewModelProvider[CurrentProjectViewModel::class.java]

        initLogin()
        initMapbox()
        initToolbar()
        initBottomToolbar()

        val syncForm: FloatingActionButton = findViewById(R.id.newform)
        syncForm.setOnClickListener {
            startActivityForResult(Intent(this, FormDownloadListActivity::class.java),1001)
        }
    }

    private fun initLogin() {
        val isLogged = settingsProvider.getMetaSettings().getBoolean(MetaKeys.IS_LOGIN)

        if (!isLogged) {
            ActivityUtils.startActivityAndCloseAllOthers(this, LoginActivity::class.java)
            return
        }else{
            initProject()
        }
    }

    private fun initProject() {
        val gson = Gson()
        val jsonUser = settingsProvider.getMetaSettings().getString(MetaKeys.KEY_USER)
        val user: User = gson.fromJson(jsonUser, User::class.java)
        val parser = JsonParser()
        val jsonObject: JsonObject = parser.parse(jsonUser).asJsonObject
        val projectsJsonString = jsonObject.getAsJsonPrimitive("projects").asString
        projectsRepository.deleteAll()
        // Parse the JSON string representing projects into a JsonArray
        val projectsArray: JsonArray = parser.parse(projectsJsonString).asJsonArray
        for (projectElement in projectsArray) {
            val projectObject = projectElement.asJsonObject
            val projectId = projectObject.get("central_project_id").asString
            val projectName = projectObject.get("project_name").asString
            val projectIcon = projectObject.get("icon").asString
            val projectColor = projectObject.get("color").asString
            val serverAddress = projectObject.get("server_url").asString
            val centralUserToken = projectObject.get("central_user_token").asString
            val serverUrl=serverAddress+"/key/"+centralUserToken+"/projects/"+projectId

            projectsRepository.save(
                Project.Saved(
                    projectId,
                    projectName,
                    projectIcon,
                    projectColor
                )
            )

            val generalSettings = settingsProvider.getUnprotectedSettings(projectId)
            generalSettings.save(ProjectKeys.KEY_METADATA_USERNAME, user.username)
            generalSettings.save(ProjectKeys.KEY_USERNAME, user.username)
            generalSettings.save(ProjectKeys.KEY_METADATA_PHONENUMBER, user.phone)
            generalSettings.save(ProjectKeys.KEY_METADATA_EMAIL, user.email)
            generalSettings.save(ProjectKeys.KEY_SERVER_URL, serverUrl)

        }
        val currrentProject = settingsProvider.getMetaSettings().getString(MetaKeys.CURRENT_PROJECT_ID)
        if (currrentProject == null) {
            val uuid = user.projectId
            val projectName=user.projectName
            val projectIcon = user.projectIcon
            val projectColor = user.projectColor
            projectsRepository.save(
                Project.Saved(
                    uuid,
                    projectName,
                    projectIcon,
                    projectColor
                )
            )
            projectsDataService.setCurrentProject(uuid)
        }
    }
    private fun initMapbox() {
        if (MapboxClassInstanceCreator.isMapboxAvailable()) {
            supportFragmentManager
                .beginTransaction()
                .add(
                    R.id.map_box_initialization_fragment,
                    MapboxClassInstanceCreator.createMapBoxInitializationFragment()
                )
                .commit()
        }
    }

    private fun initToolbar() {
        val toolbar = findViewById<Toolbar>(org.odk.collect.androidshared.R.id.toolbar)
        setSupportActionBar(toolbar)


        currentProjectViewModel = ViewModelProvider(this).get(CurrentProjectViewModel::class.java)

        // Observe the current project
        currentProjectViewModel.currentProject.observe(this) { project ->
            val (_, name) = project
            // Set the title directly in the Toolbar
            toolbar.subtitle = name
        }

        val drawerLayout = findViewById<DrawerLayout>(R.id.main)
        val navigationView = findViewById<NavigationView>(R.id.nav_sidebar)
        navigationView.setNavigationItemSelectedListener(this)

        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.open_nav,
            R.string.close_nav
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        val header = navigationView.getHeaderView(0)
        val textfullname = header.findViewById<TextView>(R.id.fullName)
        val textRole = header.findViewById<TextView>(R.id.role)

        val photo = header.findViewById<ImageView>(R.id.imageView)

        val gson = Gson()
        try {
            val jsonuser = settingsProvider.getMetaSettings().getString(MetaKeys.KEY_USER)

            val user = gson.fromJson(jsonuser, User::class.java)
            var fullname = user.username
            var position: String? = "Employee"
            if (user.fullname != null && user.fullname.isNotEmpty() && !user.fullname.equals("null")) {
                fullname = user.fullname
            }
            if (user.position != null && user.position.isNotEmpty() && !user.position.equals("null")) {
                position = user.position
            }

            textfullname.text = fullname
            textRole.text = position

            Glide.with(this)
                .load(user.photo)
                .apply(RequestOptions.circleCropTransform().placeholder(R.drawable.profile))
                .into(photo)

        } catch (e: Exception) {
            // Handle the exception (e.g., log the error or show a message to the user)
            e.printStackTrace()
            Timber.tag("UserRetrieval").e(e, "Error retrieving or parsing user information")

            // Optionally, set default values or show an error message in the UI
            // textfullname.text = "Unknown"
            // textRole.text = "Unknown"
        }

    }

    private fun initBottomToolbar() {

        loadFragment(DashboardFragment(),"Dashboard")
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNav.background = null
        bottomNav.menu.getItem(2).isEnabled = false
        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.dashboard -> {
                    loadFragment(DashboardFragment(),"Dashboard")
                    true
                }

                R.id.community -> {
                    loadFragment(CommunityFragment(),"Community")
                    true
                }
                R.id.crop -> {
                    loadFragment(CalculatorFragment(),"Crop Calculate")
                    true
                }
                R.id.account -> {
                    loadFragment(AccountFragment(),"Account")
                    true
                }

                else -> {
                    false
                }
            }
        }
    }

    override fun onFormSelected(formUri: Uri) {
        if (Intent.ACTION_PICK == intent.action) {
            // Caller is waiting on a picked form
            setResult(Activity.RESULT_OK, Intent().setData(formUri))
            finish()
        } else {
            // Caller wants to view/edit a form, so launch FormFillingActivity
            formLauncher.launch(FormFillingIntentFactory.newInstanceIntent(this, formUri))
        }
    }
    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.nav_logout -> {
                settingsProvider.getMetaSettings().save(MetaKeys.IS_LOGIN, false)
                ActivityUtils.startActivityAndCloseAllOthers(this, LoginActivity::class.java)
            }
            R.id.nav_form -> {
                startActivity(Intent(this, MainMenuActivity::class.java))
                //finish()
            }
            R.id.nav_share -> {
                shareApp()
            }
            R.id.nav_about -> {
                aboutApp()
            }
            R.id.nav_rate -> {
                addReview()
            }

        }
        val drawerLayout = findViewById<DrawerLayout>(R.id.main)
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun shareApp() {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(
                Intent.EXTRA_TEXT,
                getString(org.odk.collect.strings.R.string.tell_your_friends_msg) + " " + GOOGLE_PLAY_URL + packageName
            )
        }
        startActivity(
            Intent.createChooser(
                shareIntent,
                getString(org.odk.collect.strings.R.string.tell_your_friends)
            )
        )
    }

    private fun addReview() {
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("market://details?id=$packageName")
        )
        intentLauncher.launch(this, intent) {
            // Show a list of all available browsers if user doesn't have a default browser
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(GOOGLE_PLAY_URL + packageName)
                )
            )
        }
    }

    private fun aboutApp(){
        val aboutServerURL = getString(org.odk.collect.strings.R.string.web_server_url)
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(aboutServerURL))
        startActivity(browserIntent)
    }

    private fun loadFragment(fragment: Fragment, title: String) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.framecontainer, fragment)
        transaction.commit()
        setTitle(title) // Set the title after committing the transaction
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (!MultiClickGuard.allowClick(javaClass.name)) {
            return true
        }
        if (item.itemId == R.id.projects) {
            val dialog = ProjectSettingsDialog(viewModelFactory)  // Now you can pass the viewModelFactory here
            dialog.show(supportFragmentManager, ProjectSettingsDialog::class.java.simpleName)
            dialog.onProjectSwitchListener = { project ->
                switchProject(project)
            }
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun switchProject(project: Project.Saved) {
        // Switch project logic
        currentProjectViewModel.setCurrentProject(project)

        ActivityUtils.startActivityAndCloseAllOthers(this, MainActivity::class.java)
        ToastUtils.showLongToast(
            this,
            getString(org.odk.collect.strings.R.string.switched_project, project.name)
        )
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        val projectsMenuItem = menu?.findItem(R.id.projects)
        (projectsMenuItem?.actionView as ProjectIconView).apply {
            project = currentProjectViewModel.currentProject.value
            setOnClickListener { onOptionsItemSelected(projectsMenuItem) }
            contentDescription = getString(org.odk.collect.strings.R.string.projects)
        }
        return super.onPrepareOptionsMenu(menu)
    }


}