package org.odk.collect.selfiecamera.support

import android.app.Application
import org.odk.collect.shared.injection.ObjectProvider
import org.odk.collect.shared.injection.ObjectProviderHost
import org.odk.collect.shared.injection.SupplierObjectProvider

class RobolectricApplication : Application(), ObjectProviderHost {

    val testObjectProvider = SupplierObjectProvider()

    override fun getObjectProvider(): ObjectProvider {
        return testObjectProvider
    }
}
