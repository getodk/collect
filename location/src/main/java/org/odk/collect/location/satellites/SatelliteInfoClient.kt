package org.odk.collect.location.satellites

import org.odk.collect.androidshared.livedata.NonNullLiveData

interface SatelliteInfoClient {

    val satellitesUsedInLastFix: NonNullLiveData<Int>
}
