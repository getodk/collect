package org.odk.collect.maplibre

import android.content.Context

object MapboxAccessToken {
    fun get(context: Context): String? {
        val id = context.resources.getIdentifier("mapbox_access_token", "string", context.packageName)
        return if (id != 0) context.getString(id).ifBlank { null } else null
    }
}
