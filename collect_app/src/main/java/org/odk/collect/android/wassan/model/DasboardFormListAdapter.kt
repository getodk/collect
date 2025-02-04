package org.odk.collect.android.wassan.model

import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import org.odk.collect.android.R
import org.odk.collect.android.application.Collect
import org.odk.collect.android.formlists.blankformlist.BlankFormListItem
import org.odk.collect.android.formlists.blankformlist.OnFormItemClickListener
import org.odk.collect.android.utilities.ContentUriHelper.getIdFromUri
import org.odk.collect.android.utilities.InstancesRepositoryProvider
import org.odk.collect.android.wassan.app.Utils
import org.odk.collect.androidshared.ui.multiclicksafe.MultiClickGuard
import org.odk.collect.forms.instances.Instance

class DasboardFormListAdapter(val listener: OnFormItemClickListener) : RecyclerView.Adapter<DashboardFormListItemViewHolder>() {

    private var formItems = emptyList<BlankFormListItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DashboardFormListItemViewHolder {
        return DashboardFormListItemViewHolder(parent).also {
            it.setTrailingView(R.layout.map_button)
        }
    }

    override fun onBindViewHolder(holder: DashboardFormListItemViewHolder, position: Int) {
        val item = formItems[position]
        holder.dashboardFormListItem = item

        holder.itemView.setOnClickListener {
            if (MultiClickGuard.allowClick(javaClass.name)) {
                listener.onFormClick(item.contentUri)
            }
        }

        val cardView = holder.itemView.findViewById<CardView>(R.id.cardView)

        val draftButton = holder.itemView.findViewById<LinearLayout>(R.id.btnDraft)
        val draftCount = holder.itemView.findViewById<TextView>(R.id.draftCount)

        val readyButton = holder.itemView.findViewById<LinearLayout>(R.id.btnReady)
        val readyCount = holder.itemView.findViewById<TextView>(R.id.readyCount)

        val sentButton = holder.itemView.findViewById<LinearLayout>(R.id.btnSent)
        val sentCount = holder.itemView.findViewById<TextView>(R.id.sentCount)

        val mapButton = holder.itemView.findViewById<Button>(R.id.map_button)

        val formId = item.formId

        mapButton.visibility = if (item.geometryPath.isNotBlank()) {
            View.VISIBLE
        } else {
            View.GONE
        }
       /* val instancesRepository = InstancesRepositoryProvider(Collect.getInstance()).get()

        val editableInstances = instancesRepository.getCountByStatusAndFormId(
                formId,
                Instance.STATUS_INCOMPLETE,
                Instance.STATUS_INVALID,
                Instance.STATUS_VALID,

        )
        draftCount.setText(editableInstances.toString())

        val sendableInstances = instancesRepository.getCountByStatusAndFormId(
                formId,
                Instance.STATUS_COMPLETE,
                Instance.STATUS_SUBMISSION_FAILED
        )
        readyCount.setText(sendableInstances.toString())
        val sentInstances = instancesRepository.getCountByStatusAndFormId(
                formId,
                Instance.STATUS_SUBMITTED,
                Instance.STATUS_SUBMISSION_FAILED
        )
        sentCount.setText(sentInstances.toString())*/

        //cardView.setBackground(Utils.getRandomGradientDrawable())

        mapButton.setOnClickListener {
            if (MultiClickGuard.allowClick(javaClass.name)) {
                listener.onMapButtonClick(item.databaseId)
            }
        }

        draftButton.setOnClickListener {
            if (MultiClickGuard.allowClick(javaClass.name)) {

                //listener.onDraftButtonClick(item.formId)
            }
        }

        readyButton.setOnClickListener {
            if (MultiClickGuard.allowClick(javaClass.name)) {
               // listener.onReadyButtonClick(item.formId)
            }
        }

        sentButton.setOnClickListener {
            if (MultiClickGuard.allowClick(javaClass.name)) {
               // listener.onSentButtonClick(item.formId)
            }
        }
    }

    override fun getItemCount() = formItems.size

    fun setData(blankFormItems: List<BlankFormListItem>) {
        this.formItems = blankFormItems.toList()
        notifyDataSetChanged()
    }
}



