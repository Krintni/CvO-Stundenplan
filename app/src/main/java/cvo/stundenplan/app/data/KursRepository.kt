package cvo.stundenplan.app.data

class KursRepository(private val kursDao: KursDao) {

    suspend fun insertKurs(kurs: Kurs) {
        kursDao.insertKurs(kurs)
    }
    suspend fun deleteKurs(kurs: Kurs) {
        kursDao.deleteKurs(kurs)
    }
    suspend fun getNutzerKursListe(): List<Kurs> {
        return kursDao.getNutzerKursListe()
    }
    suspend fun getIdFromKurse(): List<Int> {
        return kursDao.getIdFromKurse()
    }
    suspend fun getKursById(id: Int): Kurs {
        return kursDao.getKursById(id)
    }
}