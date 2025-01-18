package org.odk.collect.android.wassan.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.odk.collect.android.R


/**
 * A simple [Fragment] subclass.
 * Use the [CommunityFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DashboardFragment : Fragment() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }


}