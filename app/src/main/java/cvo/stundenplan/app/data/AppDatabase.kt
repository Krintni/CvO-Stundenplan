package cvo.stundenplan.app.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

// Definiert die Struktur der Datenbank
@Database(entities = [Day::class, Hinweis::class, Kurs::class, Profil::class, ProfilKursRelation::class, KursNote::class], version = 3)
abstract class AppDatabase : RoomDatabase() {
    // Tabellen der Datenbank
    abstract fun DayDao(): DayDao
    abstract fun HinweisDao(): HinweisDao
    abstract fun KursDao(): KursDao
    abstract fun ProfilDao(): ProfilDao
    abstract fun ProfilKursRelationDao(): ProfilKursRelationDao
    abstract fun KursNoteDao(): KursNoteDao

}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Erstelle die Tabelle für Profil
        database.execSQL("""
            CREATE TABLE Profil (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                profilBezeichnung TEXT NOT NULL
            )
        """)

        // Erstelle die Tabelle für ProfilKursRelation
        database.execSQL("""
            CREATE TABLE ProfilKursRelation (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                profilId INTEGER NOT NULL,
                kursId INTEGER NOT NULL
            )
        """)
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Creating the new table for KursNote
        database.execSQL("""
            CREATE TABLE KursNote (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                dayId INTEGER NOT NULL,
                kurs TEXT NOT NULL,
                homework TEXT,
                homeworkCompleted INTEGER NOT NULL,
                comment TEXT,
                klausur INTEGER NOT NULL DEFAULT 0
            )
        """.trimIndent())
    }
}

