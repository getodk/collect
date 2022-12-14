package org.odk.collect.selfiecamera

import dagger.Component
import dagger.Module
import dagger.Provides
import org.odk.collect.permissions.PermissionsChecker
import javax.inject.Singleton

interface SelfieCameraDependencyComponentProvider {
    val selfieCameraDependencyComponent: SelfieCameraDependencyComponent
}

@Component(modules = [SelfieCameraDependencyModule::class])
@Singleton
interface SelfieCameraDependencyComponent {
    fun inject(captureSelfieActivity: CaptureSelfieActivity)
}

@Module
open class SelfieCameraDependencyModule {

    @Provides
    open fun providesPermissionChecker(): PermissionsChecker {
        throw UnsupportedOperationException("This should be overridden by dependent application")
    }

    @Provides
    internal open fun providesStillCamera(): StillCamera {
        return CameraXStillCamera()
    }

    @Provides
    internal open fun providesVideoCamera(): VideoCamera {
        return CameraXVideoCamera()
    }
}
