package cvo.stundenplan.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface KursNoteDao {
    @Upsert
    suspend fun upsertKursNote(note: KursNote)

    @Delete
    suspend fun deleteKursNote(note: KursNote)

    @Query("SELECT * FROM KursNote WHERE dayId = :dayId AND kurs = :kursName")
    suspend fun getKursNoteByDayIdAndKursName(dayId: Int, kursName: String): KursNote?

}

