package org.odk.collect.android.errors

import java.io.Serializable

data class ErrorItem(
    val title: String,
    val secondaryText: String,
    val supportingText: String
) : Serializable
