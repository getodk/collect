package org.odk.collect.entities.browser

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import org.odk.collect.androidshared.ui.ComposeThemeProvider.Companion.setContextThemedContent

class EntitiesFragment(private val viewModelFactory: ViewModelProvider.Factory) : Fragment() {

    private val entitiesViewModel by viewModels<EntitiesViewModel> { viewModelFactory }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val composeView = view as ComposeView
        val list = EntitiesFragmentArgs.fromBundle(requireArguments()).list
        composeView.setContextThemedContent {
            Surface {
                val entities by entitiesViewModel.getEntities(list).observeAsState(emptyList())
                LazyColumn {
                    entities.forEach {
                        item {
                            EntityItem(entity = it)
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}
