package cvo.stundenplan.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class KursNote(
    var dayId: Int,
    val kurs: String, // Kurs ist der Kursname z.B 2eng1a
    var homework: String?,
    var homeworkCompleted: Boolean,
    var comment: String?,
    var klausur: Boolean = false,
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0

)