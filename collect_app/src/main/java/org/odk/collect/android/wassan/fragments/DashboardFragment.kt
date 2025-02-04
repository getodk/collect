package org.odk.collect.android.wassan.fragments

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.activity.viewModels
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.odk.collect.android.R
import org.odk.collect.android.formlists.blankformlist.BlankFormListAdapter
import org.odk.collect.android.formlists.blankformlist.BlankFormListViewModel
import org.odk.collect.android.formlists.blankformlist.OnFormItemClickListener
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.preferences.dialogs.ServerAuthDialogFragment
import org.odk.collect.android.wassan.model.DasboardFormListAdapter
import org.odk.collect.androidshared.ui.DialogFragmentUtils
import org.odk.collect.androidshared.ui.ObviousProgressBar
import org.odk.collect.androidshared.ui.SnackbarUtils
import org.odk.collect.lists.EmptyListView
import org.odk.collect.lists.RecyclerViewUtils
import javax.inject.Inject


/**
 * A simple [Fragment] subclass.
 * Use the [CommunityFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DashboardFragment : Fragment(), OnFormItemClickListener {

    @Inject
    lateinit var viewModelFactory: BlankFormListViewModel.Factory

    private val viewModel: BlankFormListViewModel by viewModels { viewModelFactory }
    private val adapter: DasboardFormListAdapter = DasboardFormListAdapter(this)

    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Dependency Injection (similar to Activity)
        DaggerUtils.getComponent(requireContext()).inject(this)

        // Initialize your ViewModel, network state provider, or any other setup here
        //viewModel = ViewModelProvider(this).get(BlankFormListViewModel::class.java)
        //networkStateProvider = NetworkStateProvider(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(view)
    }

    private fun initView(view: View) {
       // view.findViewById<RecyclerView>(R.id.dashboard_form_list).adapter = adapter

        val list = view.findViewById<RecyclerView>(R.id.dashboard_form_list)
        list.layoutManager = LinearLayoutManager(requireContext())
        list.addItemDecoration(RecyclerViewUtils.verticalLineDivider(requireContext()))
        list.adapter = adapter

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // Access the progress bar and show/hide based on isLoading state
            view.findViewById<ObviousProgressBar>(org.odk.collect.androidshared.R.id.progressBar)?.apply {
                if (isLoading) show() else hide()
            }
        }

        viewModel.syncResult.observe(viewLifecycleOwner) { result ->
            if (result != null) {
                SnackbarUtils.showShortSnackbar(requireView(), result)
            }
        }

        viewModel.formsToDisplay.observe(viewLifecycleOwner) { forms ->
            view.findViewById<RecyclerView>(R.id.form_list)?.visibility =
                if (forms.isEmpty()) View.GONE else View.VISIBLE

            view.findViewById<EmptyListView>(R.id.empty_list_message)?.visibility =
                if (forms.isEmpty()) View.VISIBLE else View.GONE

            adapter.setData(forms)
        }

        viewModel.isAuthenticationRequired().observe(viewLifecycleOwner) { authenticationRequired ->
            if (authenticationRequired) {
                DialogFragmentUtils.showIfNotShowing(
                    ServerAuthDialogFragment::class.java,
                    childFragmentManager
                )
            } else {
                DialogFragmentUtils.dismissDialog(
                    ServerAuthDialogFragment::class.java,
                    childFragmentManager
                )
            }
        }
    }


    override fun onFormClick(formUri: Uri) {
        TODO("Not yet implemented")
    }

    override fun onMapButtonClick(id: Long) {
        TODO("Not yet implemented")
    }

}