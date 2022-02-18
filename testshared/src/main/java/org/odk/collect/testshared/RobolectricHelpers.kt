package org.odk.collect.testshared

import android.app.Application
import android.app.Service
import android.graphics.drawable.Drawable
import android.media.MediaMetadataRetriever
import android.os.Environment
import android.os.Looper
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ApplicationProvider
import org.odk.collect.servicetest.ServiceScenario
import org.odk.collect.servicetest.ServiceScenario.Companion.launch
import org.robolectric.Robolectric
import org.robolectric.Shadows
import org.robolectric.shadows.ShadowEnvironment
import org.robolectric.shadows.ShadowMediaMetadataRetriever
import org.robolectric.shadows.ShadowMediaPlayer
import org.robolectric.shadows.ShadowMediaPlayer.MediaInfo
import org.robolectric.shadows.util.DataSource

object RobolectricHelpers {

    var services: MutableMap<Class<*>, ServiceScenario<*>> = HashMap()

    @JvmStatic
    fun <T : FragmentActivity> createThemedActivity(clazz: Class<T>, theme: Int): T {
        val activity = Robolectric.buildActivity(clazz)
        activity.get()!!.setTheme(theme)
        return activity.setup().get()
    }

    @JvmStatic
    fun getCreatedFromResId(drawable: Drawable): Int {
        return Shadows.shadowOf(drawable).createdFromResId
    }

    @JvmStatic
    @JvmOverloads
    fun setupMediaPlayerDataSource(testFile: String, duration: Int = 322450): DataSource {
        val dataSource = DataSource.toDataSource(testFile)
        ShadowMediaMetadataRetriever.addMetadata(dataSource, MediaMetadataRetriever.METADATA_KEY_DURATION, duration.toString())
        ShadowMediaPlayer.addMediaInfo(dataSource, MediaInfo(duration, 0))
        return dataSource
    }

    fun mountExternalStorage() {
        ShadowEnvironment.setExternalStorageState(Environment.MEDIA_MOUNTED)
    }

    @JvmStatic
    fun <T : ViewGroup> populateRecyclerView(view: T): T {
        for (i in 0 until view.childCount) {
            val child = view.getChildAt(i)
            if (child is RecyclerView) {
                child.measure(0, 0)
                child.layout(0, 0, 100, 10000)
                break
            } else if (child is ViewGroup) {
                populateRecyclerView(child)
            }
        }
        return view
    }

    @JvmStatic
    @Suppress("UNCHECKED_CAST")
    fun <F : Fragment?> getFragmentByClass(fragmentManager: FragmentManager, fragmentClass: Class<F>): F? {
        val fragments = fragmentManager.fragments
        for (fragment in fragments) {
            if (fragment.javaClass.isAssignableFrom(fragmentClass)) {
                return fragment as F
            }
        }
        return null
    }

    @JvmStatic
    fun runLooper() {
        Shadows.shadowOf(Looper.getMainLooper()).idle()
    }

    fun clearServices() {
        services.clear()
    }

    @Suppress("UNCHECKED_CAST")
    fun runServices(keepServices: Boolean) {
        val application = ApplicationProvider.getApplicationContext<Application>()

        // Run pending start commands
        while (Shadows.shadowOf(application).peekNextStartedService() != null) {
            val intent = Shadows.shadowOf(application).nextStartedService
            val serviceClass: Class<*> = try {
                Class.forName(intent.component!!.className)
            } catch (e: ClassNotFoundException) {
                throw RuntimeException(e)
            }
            if (keepServices) {
                if (services.containsKey(serviceClass)) {
                    services[serviceClass]!!.startWithNewIntent(intent)
                } else {
                    val serviceController: ServiceScenario<*> = launch(serviceClass as Class<Service>, intent)
                    services[serviceClass] = serviceController
                }
            } else {
                launch(serviceClass as Class<Service>, intent)
            }
        }

        // Run pending stops - only need to stop previously started services
        if (keepServices) {
            while (true) {
                val intent = Shadows.shadowOf(application).nextStoppedService ?: break
                val serviceClass: Class<*> = try {
                    Class.forName(intent.component!!.className)
                } catch (e: ClassNotFoundException) {
                    throw RuntimeException(e)
                }
                if (services.containsKey(serviceClass)) {
                    services[serviceClass]!!.moveToState(Lifecycle.State.DESTROYED)
                    services.remove(serviceClass)
                }
            }
        }
    }
}
