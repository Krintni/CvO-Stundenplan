package cvo.stundenplan.app.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface DayDao {
    @Upsert
    suspend fun insertDay(day: Day)

    // Distinct bedeutet einzigeartig
    @Query("SELECT DISTINCT date FROM Day")
    suspend fun getDistinctDates(): List<String>

    @Query("SELECT id FROM Day WHERE date = :date")
    suspend fun getIdByDate(date: String): Int
}
