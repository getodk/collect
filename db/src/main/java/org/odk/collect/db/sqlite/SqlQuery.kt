package org.odk.collect.db.sqlite

import org.odk.collect.shared.Query

data class SqlQuery(
    val selection: String,
    val selectionArgs: Array<String>
)

fun Query.toSql(): SqlQuery {
    return when (this) {
        is Query.Eq -> {
            if (value.toDoubleOrNull() != null) {
                SqlQuery("CAST($column AS REAL) = CAST(? AS REAL)", arrayOf(value))
            } else {
                SqlQuery("$column = ?", arrayOf(value))
            }
        }
        is Query.NotEq -> {
            if (value.toDoubleOrNull() != null) {
                SqlQuery("CAST($column AS REAL) != CAST(? AS REAL)", arrayOf(value))
            } else {
                SqlQuery("$column != ?", arrayOf(value))
            }
        }
        is Query.And -> {
            val sqlA = queryA.toSql()
            val sqlB = queryB.toSql()
            SqlQuery(
                "(${sqlA.selection} AND ${sqlB.selection})",
                sqlA.selectionArgs + sqlB.selectionArgs
            )
        }
        is Query.Or -> {
            val sqlA = queryA.toSql()
            val sqlB = queryB.toSql()
            SqlQuery(
                "(${sqlA.selection} OR ${sqlB.selection})",
                sqlA.selectionArgs + sqlB.selectionArgs
            )
        }
    }
}
