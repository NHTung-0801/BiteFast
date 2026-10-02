package com.bitefast.core.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bitefast.core.database.entity.FavoriteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {

    @Query("SELECT * FROM favorite_items WHERE type = :type ORDER BY createdAt DESC")
    fun getFavoritesByType(type: String): Flow<List<FavoriteEntity>>

    @Query("SELECT * FROM favorite_items ORDER BY createdAt DESC")
    fun getAllFavorites(): Flow<List<FavoriteEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorite_items WHERE targetId = :targetId)")
    fun isFavorite(targetId: String): Flow<Boolean>

    @Query("SELECT * FROM favorite_items WHERE targetId = :targetId LIMIT 1")
    suspend fun getFavoriteByTargetId(targetId: String): FavoriteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(entity: FavoriteEntity)

    @Query("DELETE FROM favorite_items WHERE targetId = :targetId")
    suspend fun deleteFavoriteByTargetId(targetId: String)

    @Query("DELETE FROM favorite_items WHERE id = :id")
    suspend fun deleteFavoriteById(id: String)

    @Query("SELECT COUNT(*) FROM favorite_items")
    fun getFavoriteCount(): Flow<Int>
}
