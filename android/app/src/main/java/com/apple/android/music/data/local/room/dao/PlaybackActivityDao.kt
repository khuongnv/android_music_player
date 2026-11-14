package com.apple.android.music.data.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.apple.android.music.data.local.room.entity.PlaybackActivityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlaybackActivityDao {

    @Query("SELECT * FROM playback_activity WHERE endTime >= :sinceTimestamp ORDER BY startTime ASC")
    fun getActivitiesSinceFlow(sinceTimestamp: Long): Flow<List<PlaybackActivityEntity>>

    @Query("SELECT * FROM playback_activity WHERE endTime >= :sinceTimestamp ORDER BY startTime ASC")
    suspend fun getActivitiesSince(sinceTimestamp: Long): List<PlaybackActivityEntity>

    @Query("SELECT * FROM playback_activity ORDER BY id DESC LIMIT 1")
    suspend fun getLatestActivity(): PlaybackActivityEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(activity: PlaybackActivityEntity): Long

    @Update
    suspend fun update(activity: PlaybackActivityEntity)

    @Query("DELETE FROM playback_activity WHERE endTime < :beforeTimestamp")
    suspend fun deleteOldActivities(beforeTimestamp: Long)

    @Query("SELECT COUNT(*) FROM playback_activity")
    suspend fun getActivityCount(): Int
}
