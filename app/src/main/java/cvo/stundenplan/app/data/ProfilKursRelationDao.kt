package cvo.stundenplan.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface ProfilKursRelationDao {
    @Upsert
    suspend fun insertProfilKursRelation(profilKursRelation: ProfilKursRelation)

    @Delete
    suspend fun deleteProfilKursRelation(profilKursRelation: ProfilKursRelation)

    @Query("DELETE FROM ProfilKursRelation WHERE profilId= :profilId")
    suspend fun deleteProfilKursRelationByProfilId(profilId: Int)

    @Query("SELECT id FROM ProfilKursRelation WHERE profilId = :profilId AND kursId = :kursId")
    suspend fun getProfilKursRelationId(profilId: Int, kursId: Int): Int

    @Query("SELECT kursId FROM ProfilKursRelation WHERE profilId = :profilId")
    suspend fun getKurseIdByProfilId(profilId: Int): List<Int>

    @Query("SELECT * FROM Kurse WHERE Kurse.id IN (SELECT kursId FROM ProfilKursRelation WHERE profilId = :profilId)")
    suspend fun getKurseByProfil(profilId: Int): List<Kurs>

    @Query("SELECT kursId FROM ProfilKursRelation")
    suspend fun getKurseIdInProfiles(): List<Int>
}
