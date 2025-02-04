package org.odk.collect.android.wassan.activity

import android.app.ProgressDialog
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import org.json.JSONObject
import org.odk.collect.android.R
import org.odk.collect.android.activities.ActivityUtils
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.mainmenu.MainMenuActivity
import org.odk.collect.android.wassan.model.User
import org.odk.collect.settings.SettingsProvider
import org.odk.collect.settings.keys.MetaKeys
import org.odk.collect.settings.keys.ProjectKeys
import org.odk.collect.strings.localization.LocalizedActivity
import timber.log.Timber
import javax.inject.Inject

class LoginActivity : LocalizedActivity() {
    @Inject
    lateinit var settingsProvider: SettingsProvider
    lateinit var pd: ProgressDialog;

    lateinit var webServerURL: String
    lateinit var username: EditText;
    lateinit var password:EditText;


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        DaggerUtils.getComponent(this).inject(this)
        setContentView(R.layout.activity_login)

        // Adjust insets for keyboard and system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { view, insets ->
            val imeVisible = insets.isVisible(WindowInsetsCompat.Type.ime())
            val systemBarsInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val imeInsets = insets.getInsets(WindowInsetsCompat.Type.ime())

            view.setPadding(
                systemBarsInsets.left,
                systemBarsInsets.top,
                systemBarsInsets.right,
                if (imeVisible) imeInsets.bottom else systemBarsInsets.bottom
            )
            insets
        }

        val isLogged = settingsProvider.getMetaSettings().getBoolean(MetaKeys.IS_LOGIN)

        if (isLogged) {
            launchDashboard()
            finish()
        }

        username = findViewById<EditText>(R.id.editTextUsername)
        password = findViewById<EditText>(R.id.editTextPassword)
        val loginButton = findViewById<Button>(R.id.loginButton)
        pd = ProgressDialog(this)
        pd.setCanceledOnTouchOutside(false)
        loginButton.setOnClickListener(View.OnClickListener { loginRequest() })
    }

    private fun loginRequest() {
        val username1 = findViewById<EditText>(R.id.editTextUsername).text.toString()
        val password1 = findViewById<EditText>(R.id.editTextPassword).text.toString()
        pd.setMessage("Signing In . . .")
        pd.show()
        webServerURL = getString(org.odk.collect.strings.R.string.api_server_url)

        //using volley request

        val request = object : StringRequest(
            Method.POST, webServerURL+"login",
            Response.Listener { response ->
                // Handle successful login response
                try {
                    //converting response to json object
                    val obj: JSONObject = JSONObject(response)
                    if (obj.getBoolean("status")) {
                        /*Toast.makeText(
                            applicationContext,
                            obj.getString("message"),
                            Toast.LENGTH_SHORT
                        ).show()*/

                        //getting the user from the response
                        val userJson = obj.getJSONObject("user")
                        val defaultProjectJson=userJson.getJSONObject("default_project");
                        if (defaultProjectJson.length() == 0) {
                            Toast.makeText(applicationContext, "No default project found", Toast.LENGTH_SHORT).show()
                            pd.dismiss()
                            return@Listener
                        }
                        val serverUrl=defaultProjectJson.getString("server_url")+"/key/"+defaultProjectJson.getString("central_user_token")+"/projects/"+defaultProjectJson.getString("central_project_id");
                        Timber.tag("Tag").d("user: %s", userJson.toString(4));

                        //creating a new user object
                        val user = User(
                            userJson.getString("id"),
                            userJson.getString("username"),
                            password1,
                            defaultProjectJson.getString("central_user_token"),
                            defaultProjectJson.getString("central_project_id"),
                            defaultProjectJson.getString("project_name"),
                            defaultProjectJson.getString("color"),
                            defaultProjectJson.getString("icon"),
                            userJson.getString("email"),
                            userJson.getString("fullname"),
                            userJson.getString("phone"),
                            userJson.getString("position"),
                            userJson.getString("district_id"),
                            userJson.getString("block_id"),
                            userJson.getString("gp_id"),
                            userJson.getString("user_group_id"),
                            userJson.getString("image"),
                            userJson.getString("user_project"),
                        )

                        //storing the user in shared preferences
                        val gson = Gson()
                        val jsonuser = gson.toJson(user)
                        val generalSettings = settingsProvider.getUnprotectedSettings(defaultProjectJson.getString("central_project_id"))
                        settingsProvider.getMetaSettings().save(MetaKeys.KEY_USER, jsonuser)
                        generalSettings.save(ProjectKeys.KEY_METADATA_USERNAME, username1)
                        generalSettings.save(ProjectKeys.KEY_USERNAME, username1)
                        generalSettings.save(ProjectKeys.KEY_METADATA_PHONENUMBER, userJson.getString("phone"))
                        generalSettings.save(ProjectKeys.KEY_METADATA_EMAIL, userJson.getString("email"))
                        generalSettings.save(ProjectKeys.KEY_SERVER_URL, serverUrl)
                       // val generalSettingss = settingsProvider.getUnprotectedSettings();
                        launchDashboard()


                    }else{
                        if (obj.has("errors")) {
                            val errors = obj.getJSONObject("errors")
                            if (errors.has("username")) {
                                username.requestFocus()
                                username.setError(errors.getString("username"))
                            }
                            if (errors.has("password")) {
                                //password.requestFocus();
                                password.setError(errors.getString("password"))
                            }
                        }

                        // error
                        pd.dismiss()
                        showSnackbar(obj.getString("message"))
                    }

                }catch (e: Exception) {
                    e.printStackTrace()
                }
            },
            Response.ErrorListener { error ->

                // error
                pd.dismiss()
                // Handle error
                if (error.networkResponse != null && error.networkResponse.data != null) {
                    val error1 = VolleyError(String(error.networkResponse.data))
                    //Log.d("error", error1.toString());
                    Toast.makeText(applicationContext, error.message, Toast.LENGTH_SHORT).show()
                }
            }) {
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params["username"] = username1
                params["password"] = password1
                return params
            }
        }

        // Add the request to the RequestQueue
        Volley.newRequestQueue(this).add(request)
    }

    private fun launchDashboard() {
        finish()
        settingsProvider.getMetaSettings().save(MetaKeys.IS_LOGIN, true)
        ActivityUtils.startActivityAndCloseAllOthers(this, MainActivity::class.java)
    }

    private fun showSnackbar(stringSnackbar: String?) {
        Snackbar.make(
            findViewById(android.R.id.content),
            stringSnackbar!!, Snackbar.LENGTH_SHORT
        )
            .setActionTextColor(getResources().getColor(R.color.colorError))
            .show()
    }
}