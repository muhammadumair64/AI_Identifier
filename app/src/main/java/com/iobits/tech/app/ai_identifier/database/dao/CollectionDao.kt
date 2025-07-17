package com.iobits.tech.app.ai_identifier.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CollectionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: com.iobits.tech.app.ai_identifier.database.dataClasses.Collection)

    @Query("SELECT * FROM collections")
    fun getAllItems(): Flow<List<com.iobits.tech.app.ai_identifier.database.dataClasses.Collection>>

    @Query("DELETE FROM collections WHERE id = :itemId")
    suspend fun deleteItem(itemId: Int)
}
