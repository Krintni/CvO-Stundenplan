package cvo.stundenplan.app

import android.app.Application
import androidx.room.Room
import cvo.stundenplan.app.data.AppDatabase
import cvo.stundenplan.app.data.MIGRATION_1_2
import cvo.stundenplan.app.data.MIGRATION_2_3

// CustomApplication ist eine Subklasse von Application, die globale Ressourcen für die gesamte App verwaltet.
class CustomApplication : Application() {
    // Später initalisierte Datenbank
    lateinit var database: AppDatabase

    override fun onCreate() {
        super.onCreate()
        // Initialisiere globale Ressourcen, hier also Datenbank
        database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "Application Database"
        )
            .addMigrations(MIGRATION_1_2) // Füge die Migration hier hinzu
            .addMigrations(MIGRATION_2_3)
            .build()
    }
}
