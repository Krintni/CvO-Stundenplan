package cvo.stundenplan.app

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.remember
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import cvo.stundenplan.app.backend.manageDataExtraction
import cvo.stundenplan.app.backend.profilSetup
import cvo.stundenplan.app.data.DatabaseViewModel
import cvo.stundenplan.app.screen.AppNavigation
import cvo.stundenplan.app.screen.DateStateHolder
import cvo.stundenplan.app.screen.convertJsonToProfil
import cvo.stundenplan.app.ui.theme.CvOStundenplanAppTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.IOException

class MainActivity : ComponentActivity() {
    // Initialisierung des DatabaseViewModels
    // Verwaltet Methoden zur Nutzung von Datenbanken
    private val databaseViewModel: DatabaseViewModel by viewModels()

    private var fileUri: Uri? = null

    private val createFileLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                result.data?.data?.also { uri ->
                    fileUri = uri

                    saveJsonToFile(databaseViewModel.jsonContent, uri)
                }
            }
        }

    private val openFileLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode != RESULT_CANCELED){
                if (result.resultCode == RESULT_OK) {
                    result.data?.data?.also { uriFromPicker ->
                        fileUri = uriFromPicker
                        // Hier kannst du die Datei verarbeiten, z.B. den Inhalt lesen
                        readJsonFromFile(uriFromPicker)
                    }
                }
            }
        }

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "CoroutineCreationDuringComposition")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Aufruf zum Hinweisextraktor

        runBlocking {
            // Job 2: profilSetup auf einem Hintergrund-Thread starten
            val profilSetupJob = async(Dispatchers.IO) {
                Log.d("Startup", "Starte profilSetup...")
                profilSetup(databaseViewModel)
                Log.d("Startup", "profilSetup FERTIG.")
            }
            profilSetupJob.await()                 // Wartet, bis das Profil-Setup fertig ist
        }
        lifecycleScope.launch {
            // --- STARTPHASE: Alle drei Jobs werden parallel gestartet ---

            // Job 1: manageDataExtraction auf einem Hintergrund-Thread starten
            val dataExtractionJob = async(Dispatchers.IO) {
                Log.d("Startup", "Starte manageDataExtraction...")
                manageDataExtraction(databaseViewModel)
                Log.d("Startup", "manageDataExtraction FERTIG.")
            }

            dataExtractionJob.await()              // Wartet, bis die Datenextraktion fertig ist
            databaseViewModel.triggerHinweisUpdate()
        }

        enableEdgeToEdge()
        setContent {
            CvOStundenplanAppTheme {
                val navController = rememberNavController()
                databaseViewModel.callExportIntent = {createJsonFile()}
                databaseViewModel.callImportIntent = {openJsonFile()}
                // Objekt zum Speichern und Verwalten der anzuzeigenden Woche
                val dateStateHolder = remember { DateStateHolder }

                // Beginn des UI Codes
                AppNavigation(databaseViewModel, navController, dateStateHolder)
            }
        }
    }
    private fun createJsonFile() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/json"
            putExtra(Intent.EXTRA_TITLE, "ExportiertesProfil.json")
        }
        createFileLauncher.launch(intent)
    }

    private fun openJsonFile() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/json" // Setze den MIME-Typ auf JSON
        }
        openFileLauncher.launch(intent)
    }


    private fun saveJsonToFile(jsonString: String, uri: Uri) {
        try {
            contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(jsonString.toByteArray())
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
    private fun readJsonFromFile(uri: Uri) {
        Log.d("IMPORT_DEBUG", "URI inside readJsonFromFile: $uri")
        try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                val jsonString = inputStream.bufferedReader().use { it.readText() }
                // Json Inhalt in ViewModel speichern
                databaseViewModel.jsonContent = jsonString
                convertJsonToProfil(databaseViewModel)
            }
        } catch (e: SecurityException) {
            Log.e("IMPORT_DEBUG", "SecurityException for URI: $uri", e)
        }
        catch (e: IOException) {
            e.printStackTrace()
        }
    }

}