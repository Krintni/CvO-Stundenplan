package cvo.stundenplan.app.data


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Hinweis(
    val kurs: String? = null, // Kurs ist der Kursname z.B 2eng1a
    val hinweis: String,
    val hinweisTyp: String,
    val dayId: Int,
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0

)