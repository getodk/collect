package org.odk.collect.shared

import org.odk.collect.shared.Query.And
import org.odk.collect.shared.Query.Eq
import org.odk.collect.shared.Query.NotEq
import org.odk.collect.shared.Query.NumericEq
import org.odk.collect.shared.Query.NumericNotEq
import org.odk.collect.shared.Query.Or

sealed class Query {
    class Eq(val column: String, val value: String) : Query()
    class NotEq(val column: String, val value: String) : Query()
    class NumericEq(val column: String, val value: Double) : Query()
    class NumericNotEq(val column: String, val value: Double) : Query()
    class And(val queryA: Query, val queryB: Query) : Query()
    class Or(val queryA: Query, val queryB: Query) : Query()
}

fun Query.mapColumns(columnMapper: (String) -> String): Query {
    return when (this) {
        is Eq -> Eq(columnMapper(column), value)
        is NotEq -> NotEq(columnMapper(column), value)
        is NumericEq -> NumericEq(columnMapper(column), value)
        is NumericNotEq -> NumericNotEq(columnMapper(column), value)
        is And -> And(
            queryA.mapColumns(columnMapper),
            queryB.mapColumns(columnMapper)
        )
        is Or -> Or(
            queryA.mapColumns(columnMapper),
            queryB.mapColumns(columnMapper)
        )
    }
}
