package org.odk.collect.db.sqlite

import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test

class QueryTest {
    @Test
    fun `Eq query generates correct selection and arguments`() {
        val query = Query.Eq("name", "John")

        assertThat(query.selection, equalTo("name = ?"))
        assertThat(query.selectionArgs, equalTo(arrayOf("John")))
    }

    @Test
    fun `NotEq query generates correct selection and arguments`() {
        val query = Query.NotEq("age", "30")

        assertThat(query.selection, equalTo("age != ?"))
        assertThat(query.selectionArgs, equalTo(arrayOf("30")))
    }

    @Test
    fun `And query generates correct selection and arguments`() {
        val queryA = Query.Eq("name", "John")
        val queryB = Query.NotEq("age", "30")
        val combinedQuery = Query.And(queryA, queryB)

        assertThat(combinedQuery.selection, equalTo("(name = ? AND age != ?)"))
        assertThat(combinedQuery.selectionArgs, equalTo(arrayOf("John", "30")))
    }

    @Test
    fun `Or query generates correct selection and arguments`() {
        val queryA = Query.Eq("city", "New York")
        val queryB = Query.NotEq("country", "Canada")
        val combinedQuery = Query.Or(queryA, queryB)

        assertThat(combinedQuery.selection, equalTo("(city = ? OR country != ?)"))
        assertThat(combinedQuery.selectionArgs, equalTo(arrayOf("New York", "Canada")))
    }

    @Test
    fun `nested And and Or queries generates correct selection and arguments`() {
        val queryA = Query.Eq("status", "active")
        val queryB = Query.NotEq("role", "admin")
        val queryC = Query.Eq("team", "engineering")

        val combinedQuery = Query.And(Query.Or(queryA, queryB), queryC)

        assertThat(combinedQuery.selection, equalTo("((status = ? OR role != ?) AND team = ?)"))
        assertThat(combinedQuery.selectionArgs, equalTo(arrayOf("active", "admin", "engineering")))
    }
}
