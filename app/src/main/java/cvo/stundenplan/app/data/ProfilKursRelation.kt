package cvo.stundenplan.app.data


import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ProfilKursRelation(
    val profilId: Int,
    val kursId: Int,
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0
)
