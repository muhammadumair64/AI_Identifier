package com.iobits.tech.app.ai_identifier.ui.repositories

import com.iobits.tech.app.ai_identifier.database.dao.CollectionDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CollectionRepository @Inject constructor(private val collectionDao: CollectionDao) {

    val allItems: Flow<List<com.iobits.tech.app.ai_identifier.database.dataClasses.Collection>> = collectionDao.getAllItems()

    suspend fun insertItem(item: com.iobits.tech.app.ai_identifier.database.dataClasses.Collection) {
        collectionDao.insertItem(item)
    }

    suspend fun deleteItem(itemId: Int) {
        collectionDao.deleteItem(itemId)
    }
}