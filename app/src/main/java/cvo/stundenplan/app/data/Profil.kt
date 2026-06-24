package cvo.stundenplan.app.data

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
@Keep
@Entity
data class Profil(
    var profilBezeichnung: String,
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0

)
