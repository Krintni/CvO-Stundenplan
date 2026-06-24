package cvo.stundenplan.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface KursDao {
    @Upsert
    suspend fun insertKurs(kurs: Kurs)

    @Delete
    suspend fun deleteKurs(kurs: Kurs)

    @Query("SELECT * FROM Kurse")
    suspend fun getNutzerKursListe(): List<Kurs>

    @Query("SELECT id FROM Kurse")
    suspend fun getIdFromKurse(): List<Int>

    @Query("SELECT * FROM Kurse WHERE id = :id")
    suspend fun getKursById(id: Int): Kurs
}
