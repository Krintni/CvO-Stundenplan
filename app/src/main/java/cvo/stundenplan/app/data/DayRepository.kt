package cvo.stundenplan.app.data


class DayRepository(private val dayDao: DayDao) {

    suspend fun insertDay(day: Day) {
        dayDao.insertDay(day)
    }
    suspend fun getDistinctDates(): List<String> {
        return dayDao.getDistinctDates()
    }
    suspend fun getIdByDate(date: String): Int {
        return dayDao.getIdByDate(date)
    }
}
