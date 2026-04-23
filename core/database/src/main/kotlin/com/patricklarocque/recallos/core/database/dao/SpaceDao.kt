package com.patricklarocque.recallos.core.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.patricklarocque.recallos.core.database.entity.SpaceEntity

@Dao
interface SpaceDao {
    @Upsert
    suspend fun upsert(space: SpaceEntity)

    @Query("SELECT * FROM spaces WHERE id = :id")
    suspend fun getById(id: String): SpaceEntity?

    @Query("SELECT * FROM spaces ORDER BY name ASC")
    suspend fun getAll(): List<SpaceEntity>
}
