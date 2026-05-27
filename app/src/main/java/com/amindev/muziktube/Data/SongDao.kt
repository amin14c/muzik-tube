package com.amindev.muziktube.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {
    @Query("SELECT * FROM songs ORDER BY addedAt DESC")
    fun getAll(): Flow<List<Song>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(song: Song)

    @Delete
    suspend fun delete(song: Song)

    @Query("SELECT EXISTS(SELECT 1 FROM songs WHERE videoId = :id)")
    suspend fun isFavorite(id: String): Boolean
}
