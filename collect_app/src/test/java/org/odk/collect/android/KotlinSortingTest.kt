package org.odk.collect.android

import org.junit.Test

class KotlinSortingTest {

    @Test
    fun `test sorting objects by descending with long`() {
        val adam = Person2("Adam", 1)
        val mark = Person2("Mark", 2)

        val items = listOf(adam, mark)

        for (i in 1..100) {
            val sortedItems = items.sortedByDescending {
                it.age
            }

            if (sortedItems[0] != mark) {
                throw Exception("Sorting failed in loop number $i")
            }
        }
    }

    @Test
    fun `test sorting objects by descending`() {
        val adam = Person("Adam", 1)
        val mark = Person("Mark", 2)

        val items = listOf(adam, mark)

        for (i in 1..100) {
            val sortedItems = items.sortedByDescending {
                it.age
            }

            if (sortedItems[0] != mark) {
                throw Exception("Sorting failed in loop number $i")
            }
        }
    }

    @Test
    fun `test sorting objects by ascending`() {
        val adam = Person("Adam", 1)
        val mark = Person("Mark", 2)

        val items = listOf(mark, adam)

        for (i in 1..100) {
            val sortedItems = items.sortedBy {
                it.age
            }

            if (sortedItems[0] != adam) {
                throw Exception("Sorting failed in loop number $i")
            }
        }
    }

    data class Person(
        val name: String,
        val age: Int
    )

    data class Person2(
        val name: String,
        val age: Long
    )
}
