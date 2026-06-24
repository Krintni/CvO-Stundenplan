package cvo.stundenplan.app.data

import android.app.Application
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import cvo.stundenplan.app.CustomApplication
import cvo.stundenplan.app.backend.Updater
import cvo.stundenplan.app.backend.ableFilledCards
import cvo.stundenplan.app.backend.getFilledCardsEnabledFlow
import cvo.stundenplan.app.backend.getLastRefreshTimestampFlow
import cvo.stundenplan.app.backend.setRefreshTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DatabaseViewModel(application: Application) : AndroidViewModel(application) {

    // ---- DATENBANK RELEVANTE VARIABLEN ----
    // application as CustomApplication bedeutet man initalisiert die Datenbank
    private val dayDao = (application as CustomApplication).database.DayDao() // Zugriff auf das Dao
    private val dayRepository = DayRepository(dayDao)  // Repository erstellen

    private val hinweisDao = (application as CustomApplication).database.HinweisDao()
    private val hinweisRepository = HinweisRepository(hinweisDao)

    private val kursDao = (application as CustomApplication).database.KursDao()
    private val kursRepository = KursRepository(kursDao)

    private val profilDao = (application as CustomApplication).database.ProfilDao()

    private val profilKursRelationDao = (application as CustomApplication).database.ProfilKursRelationDao()

    private val kursNoteDao = (application as CustomApplication).database.KursNoteDao()

    // ---- FUNKTIONEN ZUR INTERAKTION MIT DATENBANK ----
    // -- Funktionen für DayDao --
    suspend fun insertDay(day: Day) {
        dayRepository.insertDay(day)  // Tag in die Datenbank einfügen

    }
    suspend fun getDistinctDates(): List<String> {
        return dayRepository.getDistinctDates()
    }
     suspend fun getIdByDate(date: String): Int {
        return dayRepository.getIdByDate(date)
    }

    // -- Funktionen für HinweisDao --
    fun insertHinweis(hinweis: Hinweis) {
        // Läuft asynchron, bei suspend functions geht es nur weiter bis der Ablauf fertig ist
        viewModelScope.launch {
            hinweisRepository.insertHinweis(hinweis)
        }
    }
    suspend fun getAllHinweis(): List<Hinweis> {
        return hinweisRepository.getAllHinweis()
    }



    // -- Funktionen für KursDao --
    suspend fun insertKurs(kurs: Kurs) {
        kursRepository.insertKurs(kurs)
    }
    suspend fun deleteKurs(kurs: Kurs) {
        kursRepository.deleteKurs(kurs)
    }
    suspend fun getNutzerKursListe(): List<Kurs> {
        return kursRepository.getNutzerKursListe()
    }
    suspend fun getIdFromKurse(): List<Int> {
        return kursRepository.getIdFromKurse()
    }
    suspend fun getKursById(id: Int): Kurs {
        return kursRepository.getKursById(id)
    }

    // -- Funktionen für ProfilDao --
    suspend fun insertProfil(profil: Profil) {
        profilDao.insertProfil(profil)
    }
    suspend fun deleteProfil(profil: Profil) {
        profilDao.deleteProfil(profil)
    }
    suspend fun getProfiles(): List<Profil> {
        return profilDao.getProfiles()
    }
    suspend fun getProfilBezeichnung(id: Int): String {
        return profilDao.getProfilBezeichnung(id)
    }
    suspend fun getIdByProfilbezeichnung(profilbezeichnung: String): Int {
        return profilDao.getIdByProfilbezeichnung(profilbezeichnung)
    }

    // -- Funktionen für ProfilKursRelation --
    suspend fun deleteProfilKursRelationByProfilId(profilId: Int) {
        profilKursRelationDao.deleteProfilKursRelationByProfilId(profilId)
    }
    suspend fun insertProfilKursRelation(profilKursRelation: ProfilKursRelation) {
        profilKursRelationDao.insertProfilKursRelation(profilKursRelation)
    }
    suspend fun deleteProfilKursRelation(profilKursRelation: ProfilKursRelation) {
        profilKursRelationDao.deleteProfilKursRelation(profilKursRelation)
    }
    suspend fun getProfilKursRelationId(profilId: Int, kursId: Int): Int {
        return profilKursRelationDao.getProfilKursRelationId(profilId, kursId)
    }

    suspend fun getKurseIdByProfilId(profilId: Int): List<Int> {
        return profilKursRelationDao.getKurseIdByProfilId(profilId)
    }
    suspend fun getKurseByProfil(profilId: Int): List<Kurs> {
        return profilKursRelationDao.getKurseByProfil(profilId)
    }

    suspend fun getKurseIdInProfiles(): List<Int> {
        return profilKursRelationDao.getKurseIdInProfiles()
    }

    // -- Funktionen für Notizen --
    suspend fun upsertKursNote(kursNote: KursNote) {
        kursNoteDao.upsertKursNote(kursNote)
    }
    suspend fun deleteKursNote(kursNote: KursNote) {
        kursNoteDao.deleteKursNote(kursNote)
    }

    suspend fun getKursNoteByDayIdAndKursName(dayId: Int, kursName: String): KursNote? {
        return kursNoteDao.getKursNoteByDayIdAndKursName(dayId, kursName)
    }
    // ---- PROFILE ----
    private val _currentProfilId = MutableStateFlow(1)
    private val currentProfilId: StateFlow<Int> = _currentProfilId

    // StateFlow für die Profile
    private val _profiles = MutableStateFlow<List<Profil>>(emptyList())
    val profiles: StateFlow<List<Profil>> get() = _profiles

    fun changeCurrentProfilId(newId: Int) {
        _currentProfilId.value = newId
    }

    fun getCurrentProfilId(): StateFlow<Int> {
        return currentProfilId
    }

    fun fetchProfiles() {
        viewModelScope.launch {
            val profileList = getProfiles() // Holt die Liste der Profile
            _profiles.value = profileList // Aktualisiert die MutableLiveData
        }
    }

    // ---- ANDERE FUNKTIONEN ----
    /**
     * Sucht EINMALIG nach einem spezifischen Hinweis für einen Kurs an einem Datum.
     * @return Das gefundene `Hinweis`-Objekt oder `null`.
     */
    suspend fun findSpecificHinweis(kursName: String, date: String): Hinweis? {
        // Führe die Logik auf einem Hintergrund-Thread aus.
        return withContext(Dispatchers.IO) {
            try {
                // Schritt 1: Hole die ID für das Datum.
                val dayId = getIdByDate(date) // Annahme: Diese Funktion existiert im ViewModel

                // Schritt 2: Hole die Liste aller Hinweise für diesen Tag (die "Once"-Version).
                val alleHinweiseAnDemTag = hinweisRepository.getHinweiseWithDayId(dayId)

                // Schritt 3: Finde den passenden Hinweis in der Liste und gib ihn zurück.
                alleHinweiseAnDemTag.findLast { hinweis ->
                    hinweis.kurs.equals(kursName, ignoreCase = true)
                }
            } catch (e: Exception) {
                Log.e("ViewModel", "Fehler bei der Suche nach spezifischem Hinweis", e)
                null // Bei jeglichem Fehler sicher null zurückgeben.
            }
        }
    }

    /**
     * Prüft für eine ganze Liste von Datums-Strings, wo Hinweise existieren.
     * @param dates Eine Liste von Daten im Format "dd.MM.yyyy".
     * @return Eine Map, z.B. {"24.12.2025"=true, "25.12.2025"=false}.
     */
    suspend fun checkHinweiseForDateRange(dates: List<String>): Map<String, Boolean> {
        return withContext(Dispatchers.IO) {
            // 1. Konvertiere alle Datums-Strings in Day-IDs.
            val dateToIdMap = dates.associateWith { date ->
                try { getIdByDate(date) } catch (e: Exception) { 0 }
            }
            val validDayIds = dateToIdMap.values.filter { it > 0 }

            // 2. Führe eine Datenbankabfrage für alle relevanten IDs aus.
            val hinweise = if (validDayIds.isNotEmpty()) {
                hinweisRepository.getHinweiseForDayIdsOnce(validDayIds)
            } else {
                emptyList()
            }

            // 3. Gruppiere die gefundenen Hinweise nach ihrer dayId.
            val hinweiseByDayId = hinweise.groupBy { it.dayId }

            // 4. Erstelle die finale Ergebnis-Map.
            dateToIdMap.mapValues { (_, dayId) ->
                // Ein Datum hat Hinweise, wenn seine ID in der Ergebnis-Map der DB vorkommt.
                hinweiseByDayId.containsKey(dayId)
            }
        }
    }

    private val _latestVersion = MutableStateFlow("")

    val latestVersion = _latestVersion.asStateFlow()

    init {
        // Starte die erste Update-Prüfung direkt beim Erstellen des ViewModels
        checkForUpdates()
    }

    fun checkForUpdates() {
        // Starte eine Coroutine, um die Netzwerkanfrage sicher im Hintergrund auszuführen
        viewModelScope.launch(Dispatchers.IO) {
            Log.d("Startup", "Starte Updater...")

            // Rufe deinen Updater auf. Wir simulieren das hier mit einem Result.
            // Ersetze das durch deinen echten Aufruf: Updater.getLatestVersionName()
            val versionResult = Updater.getLatestVersionName() // Dein existierender Code

            // Wechsle zurück zum Main-Thread, um den State sicher zu aktualisieren
            // (Obwohl .value bei StateFlow thread-sicher ist, ist es eine gute Praxis)
            launch(Dispatchers.Main) {
                versionResult.onSuccess { versionString ->
                    // 4. Aktualisiere den Wert des StateFlows
                    //    Jeder, der 'latestVersion' beobachtet, wird nun benachrichtigt!
                    _latestVersion.value = versionString
                    Log.d("Startup", "Neueste Version: $versionString")
                }.onFailure { error ->
                    Log.e("Startup", "Fehler beim Laden der Version", error)
                }
            }
        }
    }

    // Ein einfacher Zähler, der als Signal für eine manuelle Aktualisierung dient.
    private val _hinweisUpdateTrigger = MutableStateFlow(0)
    val hinweisUpdateTrigger = _hinweisUpdateTrigger.asStateFlow()
    fun triggerHinweisUpdate() {
        _hinweisUpdateTrigger.value++ // Erhöhe einfach den Zähler.
        Log.d("HINWEIS_TRIGGER", "Trigger wurde ausgelöst, neuer Wert: ${_hinweisUpdateTrigger.value}")
    }


    /**
     * Dies ist der EINE StateFlow, den Ihre UI beobachten wird.
     * Er holt den "kalten" Flow aus Ihrer `getFilledCardsEnabledFlow`-Funktion
     * und wandelt ihn in einen "heißen" StateFlow um, der den letzten Wert behält.
     */
    val areCardsFilled: StateFlow<Boolean> = getFilledCardsEnabledFlow(application)
        .stateIn(
            scope = viewModelScope, // Läuft so lange wie das ViewModel
            // Hält den Flow aktiv, solange die UI zuhört (plus 5s Puffer)
            started = SharingStarted.WhileSubscribed(5000L),
            // Wichtig: Der Startwert, der angezeigt wird, bevor der echte Wert geladen ist.
            // Sollte dem Standardwert aus Ihrer Lese-Funktion entsprechen.
            initialValue = false
        )

    /**
     * Diese Funktion wird von der UI aufgerufen, um die Einstellung zu ändern.
     * Sie startet sicher eine Coroutine, um die `suspend`-Schreibfunktion aufzurufen.
     */
    fun setCardsFilled(isEnabled: Boolean) {
        // Starte die Coroutine im Scope des ViewModels.
        viewModelScope.launch {
            // Ruft Ihre globale Schreib-Funktion `ableFilledCards` auf.
            ableFilledCards(application, isEnabled)
        }
    }

    private val _hinweisExtraktorFehler = MutableStateFlow(false)
    val hinweisExtraktorFehler = _hinweisExtraktorFehler.asStateFlow()

    fun setHinweisExtraktorFehler(isTrue: Boolean) {
        viewModelScope.launch {
            _hinweisExtraktorFehler.value = isTrue
        }
    }

    val lastRefreshTimestamp: StateFlow<Long> = getLastRefreshTimestampFlow(application)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = 0L
        )
    fun onDataExtractionComplete() {
        viewModelScope.launch {
            setRefreshTime(application)
        }
    }

    var firstStart = mutableStateOf(false)
    // Export / Import
    var overwriteDefaultProfil = false
    var callExportIntent: () -> Unit = {}
    var callImportIntent: () -> Unit = {}

    var jsonContent = ""
}

