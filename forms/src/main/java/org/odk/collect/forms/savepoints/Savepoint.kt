package org.odk.collect.forms.savepoints

data class Savepoint(
    val formDbId: Long,
    val instanceDbId: Long?,
    val savepointFilePath: String,
    val instanceFilePath: String
)
