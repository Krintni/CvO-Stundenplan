package cvo.stundenplan.app.backend

import cvo.stundenplan.app.data.DatabaseViewModel
import cvo.stundenplan.app.data.Profil
import cvo.stundenplan.app.data.ProfilKursRelation
import kotlinx.coroutines.runBlocking

fun profilSetup(databaseViewModel: DatabaseViewModel) {
    runBlocking {
        val profile = databaseViewModel.getProfiles()
        if (profile.isEmpty()) {
            databaseViewModel.firstStart.value = true
            databaseViewModel.insertProfil(Profil(profilBezeichnung = "Standardprofil"))
            val profilId = databaseViewModel.getIdByProfilbezeichnung("Standardprofil")
            val kurseId = databaseViewModel.getIdFromKurse()
            kurseId.forEach { kursId ->
                val relation = ProfilKursRelation(profilId, kursId)
                databaseViewModel.insertProfilKursRelation(relation)
            }
        }
    }
}