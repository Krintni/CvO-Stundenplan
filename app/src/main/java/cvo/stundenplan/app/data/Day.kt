package cvo.stundenplan.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Day(
    val date: String,
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0

)
