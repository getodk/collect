package org.odk.collect.android.database.itemsets

import org.odk.collect.android.fastexternalitemset.ItemsetDbAdapter
import org.odk.collect.android.itemsets.FastExternalItemsetsRepository

class DatabaseFastExternalItemsetsRepository : FastExternalItemsetsRepository {

    override fun deleteAllByCsvPath(path: String) {
        ItemsetDbAdapter().open().use {
            it.delete(path)
        }
    }
}
