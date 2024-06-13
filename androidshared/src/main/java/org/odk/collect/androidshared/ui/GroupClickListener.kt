package org.odk.collect.androidshared.ui

import android.view.View
import androidx.constraintlayout.widget.Group

// https://stackoverflow.com/questions/59020818/group-multiple-views-in-constraint-layout-to-set-only-one-click-listener
fun Group.addOnClickListener(listener: (view: View) -> Unit) {
    referencedIds.forEach { id ->
        rootView.findViewById<View>(id).setOnClickListener(listener)
    }
}

fun List<View>.addOnClickListener(listener: (view: View) -> Unit) {
    forEach {
        it.setOnClickListener(listener)
    }
}
