package cvo.stundenplan.app.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert

@Dao
interface ProfilDao {
    @Upsert
    suspend fun insertProfil(profil: Profil)

    @Delete
    suspend fun deleteProfil(profil: Profil)

    @Query("SELECT * FROM Profil")
    suspend fun getProfiles(): List<Profil>

    @Query("SELECT profilBezeichnung FROM Profil WHERE id = :id")
    suspend fun getProfilBezeichnung(id: Int): String

    // Nur bei ProfilSetup zu nutzen, sonst kann es zum Crash führen
    @Query("SELECT id FROM Profil WHERE profilBezeichnung = :profilbezeichnung")
    suspend fun getIdByProfilbezeichnung(profilbezeichnung: String): Int
}
