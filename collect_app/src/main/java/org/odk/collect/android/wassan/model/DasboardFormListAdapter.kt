package org.odk.collect.android.wassan.model

import android.annotation.SuppressLint
import android.database.Cursor
import android.database.DatabaseUtils
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import org.odk.collect.android.R
import org.odk.collect.android.database.instances.DatabaseInstanceColumns
import org.odk.collect.android.database.instances.DatabaseInstancesRepository
import org.odk.collect.android.formlists.blankformlist.BlankFormListItem
import org.odk.collect.android.formlists.blankformlist.OnFormItemClickListener
import org.odk.collect.android.projects.ProjectsDataService
import org.odk.collect.android.utilities.FormsRepositoryProvider
import org.odk.collect.android.utilities.InstancesRepositoryProvider
import org.odk.collect.android.wassan.app.InstanceCountHelper
import org.odk.collect.android.wassan.app.Utils
import org.odk.collect.android.wassan.listeners.FormActionListener
import org.odk.collect.androidshared.ui.multiclicksafe.MultiClickGuard
import org.odk.collect.forms.FormsRepository
import org.odk.collect.forms.instances.Instance
import java.util.Locale


class DasboardFormListAdapter(
    val listener: OnFormItemClickListener,
    val formActionListener: FormActionListener,
    private val instancesRepositoryProvider: InstancesRepositoryProvider,
    private val projectsDataService: ProjectsDataService
) : RecyclerView.Adapter<DashboardFormListItemViewHolder>() {

    lateinit var c: Cursor
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
        val currentProject = projectsDataService.getCurrentProject()

        draftCount.text = getFormattedCount(
            InstanceCountHelper.getInstanceCount(
                instancesRepositoryProvider,
                currentProject.uuid,
                "${DatabaseInstanceColumns.JR_FORM_ID} = ? AND ${DatabaseInstanceColumns.STATUS} IN (?, ?, ?)",
                arrayOf(formId, Instance.STATUS_INCOMPLETE, Instance.STATUS_INVALID, Instance.STATUS_VALID)
            )
        )

        readyCount.text = getFormattedCount(
            InstanceCountHelper.getInstanceCount(
                instancesRepositoryProvider,
                currentProject.uuid,
                "${DatabaseInstanceColumns.JR_FORM_ID} = ? AND ${DatabaseInstanceColumns.STATUS} IN (?, ?)",
                arrayOf(formId, Instance.STATUS_COMPLETE, Instance.STATUS_SUBMISSION_FAILED)
            )
        )

        sentCount.text = getFormattedCount(
            InstanceCountHelper.getInstanceCount(
                instancesRepositoryProvider,
                currentProject.uuid,
                "${DatabaseInstanceColumns.JR_FORM_ID} = ? AND ${DatabaseInstanceColumns.STATUS} = ?",
                arrayOf(formId, Instance.STATUS_SUBMITTED)
            )
        )

        cardView.background = Utils.getRandomGradientDrawable()

        mapButton.setOnClickListener {
            if (MultiClickGuard.allowClick(javaClass.name)) {
                listener.onMapButtonClick(item.databaseId)
            }
        }

        draftButton.setOnClickListener {
            if (MultiClickGuard.allowClick(javaClass.name)) {
                formActionListener.onDraftClick(item.formId)
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

    private fun getFormattedCount(count: Int): String {
        return String.format(Locale.getDefault(), "%d", count)
    }

    override fun getItemCount() = formItems.size

    @SuppressLint("NotifyDataSetChanged")
    fun setData(blankFormItems: List<BlankFormListItem>) {
        this.formItems = blankFormItems.toList()
        notifyDataSetChanged()
    }



    private fun dbQuery(
        projectId: String,
        selection: String,
        selectionArgs: Array<String>
    ): Int {
        val instancesRepository = instancesRepositoryProvider.create(projectId)

        if (instancesRepository is DatabaseInstancesRepository) {
            val cursor = instancesRepository.rawQuery(
                arrayOf("COUNT(*)"),  // Select only the count
                selection,
                selectionArgs,
                null, // No sorting
                null  // No grouping
            )

            cursor.use {
                return if (cursor.moveToFirst()) cursor.getInt(0) else 0 // Extract the count
            }
        }

        return 0
    }

}





