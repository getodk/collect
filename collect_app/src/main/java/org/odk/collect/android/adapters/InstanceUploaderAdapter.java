package org.odk.collect.android.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;

import org.odk.collect.android.database.DatabaseObjectMapper;
import org.odk.collect.android.formlists.savedformlist.SavedForListItemViewHolder;
import org.odk.collect.android.storage.StoragePathProvider;
import org.odk.collect.android.storage.StorageSubdirectory;
import org.odk.collect.forms.instances.Instance;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

public class InstanceUploaderAdapter extends CursorAdapter {
    private final Consumer<Long> onItemCheckboxClickListener;
    private Set<Long> selected = new HashSet<>();

    public InstanceUploaderAdapter(Context context, Cursor cursor, Consumer<Long> onItemCheckboxClickListener) {
        super(context, cursor);
        this.onItemCheckboxClickListener = onItemCheckboxClickListener;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        SavedForListItemViewHolder viewHolder = new SavedForListItemViewHolder(parent);
        viewHolder.itemView.setTag(viewHolder);
        return viewHolder.itemView;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        SavedForListItemViewHolder viewHolder = (SavedForListItemViewHolder) view.getTag();
        Instance instance = DatabaseObjectMapper.getInstanceFromCurrentCursorPosition(cursor, new StoragePathProvider().getOdkDirPath(StorageSubdirectory.INSTANCES));
        viewHolder.setItem(instance);

        long dbId = instance.getDbId();
        viewHolder.getCheckbox().setChecked(selected.contains(dbId));
        viewHolder.getSelectArea().setOnClickListener(v -> onItemCheckboxClickListener.accept(dbId));
    }

    public void setSelected(Set<Long> ids) {
        this.selected = ids;
        notifyDataSetChanged();
    }
}
