package cvo.stundenplan.app.data


class HinweisRepository(private val hinweisDao: HinweisDao) {

    suspend fun insertHinweis(hinweis: Hinweis) {
        hinweisDao.insertHinweis(hinweis)
    }
    suspend fun getAllHinweis(): List<Hinweis> {
        return hinweisDao.getAllHinweis()
    }
    suspend fun getHinweiseWithDayId(dayId: Int): List<Hinweis> {
        return hinweisDao.getHinweiseWithDayId(dayId)
    }
    suspend fun getHinweiseForDayIdsOnce(dayIds: List<Int>): List<Hinweis> {
        return hinweisDao.getHinweiseForDayIdsOnce(dayIds)
    }

}
