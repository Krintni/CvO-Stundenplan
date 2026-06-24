package cvo.stundenplan.app.data

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
@Keep
@Entity(tableName = "Kurse")
data class Kurs(
    val kursName: String,
    val raum: String? = null,
    val lehrerkuerzel: String? = null,
    val kursColor: Long, // Hex code
    // Regularitätstyp: 0, 1, 2 = jede, ungerade, gerade Woche
    var mondayRegularity: Int? = null,
    var mondayBlock: Int? = null,
    var tuesdayRegularity: Int? = null,
    var tuesdayBlock: Int? = null,
    var wednesdayRegularity: Int? = null,
    var wednesdayBlock: Int? = null,
    var thursdayRegularity: Int? = null,
    var thursdayBlock: Int? = null,
    var fridayRegularity: Int? = null,
    var fridayBlock: Int? = null,
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0

)