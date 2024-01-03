import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.LiveData
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import org.odk.collect.androidshared.livedata.NonNullLiveData
import org.odk.collect.android.geo.MapLayerSelectionData
import org.odk.collect.android.geo.OfflineMapLayerItem
import org.odk.collect.android.geo.OfflineMapLayerSelectionFragment

class OfflineMapLayersListBottomSheetDialog : BottomSheetDialogFragment() {

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(org.odk.collect.android.R.layout.bottom_sheet_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val selectionMapData = object : MapLayerSelectionData {

            override fun isLoading(): NonNullLiveData<Boolean> {
                TODO("Not yet implemented")
            }

            override fun getMapTitle(): LiveData<String?> {
                TODO("Not yet implemented")
            }

            override fun getItemType(): String {
                TODO("Not yet implemented")
            }

            override fun getItemCount(): NonNullLiveData<Int> {
                TODO("Not yet implemented")
            }

            override fun getItems(): LiveData<List<OfflineMapLayerItem>>? {
                TODO("Not yet implemented")
            }

            override fun onItemSelectionChanged(item: OfflineMapLayerItem) {
                TODO("Not yet implemented")
            }
        }

        val itemList = listOf(
                OfflineMapLayerItem("Map 1", "/path/to/map1", false),
                OfflineMapLayerItem("Map 2", "/path/to/map2", false),
                // Add more OfflineMapLayerItem instances as needed
        )

        val offlineMapLayerSelectionFragment = OfflineMapLayerSelectionFragment(selectionMapData, itemList)

        childFragmentManager.beginTransaction()
                .replace(org.odk.collect.android.R.id.bottom_sheet_fragment_container, offlineMapLayerSelectionFragment)
                .commit()
    }

    companion object {
        fun showBottomSheet(fragmentManager: FragmentManager) {
            val bottomSheetFragment = OfflineMapLayersListBottomSheetDialog()
            bottomSheetFragment.show(fragmentManager, bottomSheetFragment.tag)
        }
    }
}
