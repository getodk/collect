package org.odk.collect.location

import org.odk.collect.androidshared.livedata.NonNullLiveData

interface SatelliteInfoClient {

    val satellitesUsedInLastFix: NonNullLiveData<Int>
}
