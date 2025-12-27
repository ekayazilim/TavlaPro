package com.toplu.tavlauygulamasi.data.db

import androidx.room.*

@Entity(tableName = "match_history")
data class MatchResult(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val winner: String,
    val loser: String,
    val mode: String, // "AI" or "LOCAL_PVP"
    val date: Long = System.currentTimeMillis()
)

@Dao
interface MatchDao {
    @Insert
    suspend fun insertMatch(match: MatchResult)

    @Query("SELECT * FROM match_history ORDER BY date DESC")
    fun getAllMatches(): kotlinx.coroutines.flow.Flow<List<MatchResult>>
}

@Database(entities = [MatchResult::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun matchDao(): MatchDao
}
