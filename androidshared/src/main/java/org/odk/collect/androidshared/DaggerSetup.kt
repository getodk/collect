package org.odk.collect.androidshared

import dagger.Component
import dagger.Module
import javax.inject.Singleton

interface AndroidSharedDependencyComponentProvider {
    val androidSharedDependencyComponent: AndroidSharedDependencyComponent
}

@Component(modules = [AndroidSharedDependencyModule::class])
@Singleton
interface AndroidSharedDependencyComponent

@Module
open class AndroidSharedDependencyModule
