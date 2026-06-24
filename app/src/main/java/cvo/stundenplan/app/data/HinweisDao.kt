package cvo.stundenplan.app.data

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface HinweisDao {
    @Upsert
    suspend fun insertHinweis(hinweis: Hinweis)

    @Query("SELECT * FROM Hinweis")
    suspend fun getAllHinweis(): List<Hinweis>

    @Query("SELECT * FROM Hinweis WHERE dayId = :dayId")
    suspend fun getHinweiseWithDayId(dayId: Int): List<Hinweis>

    @Query("SELECT * FROM Hinweis WHERE dayId IN (:dayIds)")
    suspend fun getHinweiseForDayIdsOnce(dayIds: List<Int>): List<Hinweis>
}
