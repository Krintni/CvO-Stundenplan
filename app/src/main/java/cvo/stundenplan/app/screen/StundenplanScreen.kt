package cvo.stundenplan.app.screen

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.NewReleases
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import com.google.gson.Gson
import cvo.stundenplan.app.BuildConfig
import cvo.stundenplan.app.backend.convertFromBlocksCodeToBlocksId
import cvo.stundenplan.app.backend.manageDataExtraction
import cvo.stundenplan.app.data.DatabaseViewModel
import cvo.stundenplan.app.data.Hinweis
import cvo.stundenplan.app.data.Kurs
import cvo.stundenplan.app.data.KursNote
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.WeekFields
import java.util.Date
import java.util.Locale

object DateStateHolder {
    // Variablen zum Speichern des Zustands des Datums
    private val currentDate = mutableStateOf(LocalDate.now())
    private val state: MutableState<LocalDate> = currentDate

    // Funktionen zum Ändern des Datums
    fun determineDate(weekInt: Int) {
        state.value = state.value.plusWeeks(weekInt.toLong())
    }
    // Methode zum Fetchen des Datums
    fun getDate(): LocalDate {
        return state.value
    }
}

@Composable
// Haupt-Composable, welche die TopBar und Stundenplan aufruft
fun HomeScreen(
    databaseViewModel: DatabaseViewModel,
    navController: NavHostController,
    dateStateHolder: DateStateHolder,
) {
    Scaffold(
        topBar = {
            TopBar(navController, dateStateHolder, databaseViewModel)
        }
    ){ innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize() // Breitet den Layout-Container auf ganzen Bildschirm aus
        ){
            Stundenplan(
                databaseViewModel = databaseViewModel,
                navController = navController,
                dateStateHolder = dateStateHolder,
            )
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class) // Aktiviert experimentelle APIs von Material3
@Composable
// Diese Funktion erstellt eine TopBar mit Titel, Wochenwechselbuttons und Kurs Hinzufügen Button
fun TopBar(
    navController: NavHostController,
    dateStateHolder: DateStateHolder,
    databaseViewModel: DatabaseViewModel
) {
    // Erstellt eine zentrierte TopAppBar mit einem Titel und Aktionsschaltflächen
    CenterAlignedTopAppBar(
        title = {
            // Zeigt den Titel und die Aktionsschaltflächen in einer horizontalen Reihe an
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Spalte für den Titel der App
                Column(modifier = Modifier
                    .padding(start = 5.dp) // Fügt links einen Abstand hinzu
                    .weight(1f) // Lässt die Spalte den verfügbaren Platz einnehmen
                ) {
                    // Wenn UI sich aktualisieren musst, dann muss so:
                    val currentProfilId by databaseViewModel.getCurrentProfilId().collectAsState()
                    var currentProfilBezeichnung: String
                    runBlocking {
                        currentProfilBezeichnung = databaseViewModel.getProfilBezeichnung(currentProfilId)
                    }
                    // Text für den App-Namen

                    Text(
                        text = currentProfilBezeichnung, // Ruft den App-Namen aus den Ressourcen ab
                    )
                }
                // Horizontale Zeile für die Navigationsbuttons
                Row {
                    val coroutineScope = rememberCoroutineScope()
                    // Button für die vorherige Woche
                    IconButton(onClick = {
                        coroutineScope.launch {
                            manageDataExtraction(databaseViewModel)
                            databaseViewModel.triggerHinweisUpdate()
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Outlined.Refresh, // Symbol für die vorherige Woche
                            contentDescription = "Hinweise aktualisieren" // Beschreibung für Barrierefreiheit
                        )
                    }
                    IconButton(onClick = { dateStateHolder.determineDate(-1) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft, // Symbol für die vorherige Woche
                            contentDescription = "Vorherige Woche" // Beschreibung für Barrierefreiheit
                        )
                    }
                    // Button für die nächste Woche
                    IconButton(onClick = { dateStateHolder.determineDate(1) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, // Symbol für die nächste Woche
                            contentDescription = "Nächste Woche" // Beschreibung für Barrierefreiheit
                        )
                    }
                }
                DropdownMenu(navController, databaseViewModel)
            }
        }
    )
}

@Composable
fun DropdownMenu(navController: NavController, databaseViewModel: DatabaseViewModel) {
    val context = LocalContext.current
    var expanded by remember { mutableStateOf(false) }

    val versionName = BuildConfig.VERSION_NAME
    val latestVersion by databaseViewModel.latestVersion.collectAsState()
    val updateAvailable = versionName != latestVersion && latestVersion != ""
    Box {
        IconButton(onClick = { expanded = !expanded }) {
            BadgedBox(
                badge = {
                    if (updateAvailable) {
                        Badge()
                    }
                }
            ) {
                Icon(Icons.Default.MoreVert, contentDescription = "Mehr Optionen",
                        modifier = Modifier.size(20.dp))
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {

            DropdownMenuItem(
                leadingIcon = { Icon(Icons.Outlined.Add, contentDescription = null) },
                text = { Text("Kurs hinzufügen") },
                onClick = {expanded = false
                    navController.navigate("kursHinzufuegen/${null}")}
            )

            DropdownMenuItem(
                leadingIcon = { Icon(Icons.Outlined.Settings, contentDescription = null) },
                text = { Text("Einstellungen") },
                onClick = {navController.navigate("Einstellungen")}
            )

            if (updateAvailable) {
                DropdownMenuItem(
                    leadingIcon = {
                        BadgedBox(
                            badge = {
                                Badge()
                            }
                        ) {
                        Icon(Icons.Outlined.NewReleases, contentDescription = null)
                        }
                    },
                    text = { Text("Neues Update verfügbar!") },
                    onClick = {
                        val fileName = "CvO_Stundenplan.apk"
                        val intent = Intent(
                            Intent.ACTION_VIEW,
                            "https://github.com/Krintni/CvO-Stundenplan/releases/latest/download/$fileName".toUri())
                        // Optional: Intent chooser
                        val chooser = Intent.createChooser(intent, "Open with")
                        context.startActivity(chooser) })
            }
            HorizontalDivider()



            // Profile
            databaseViewModel.fetchProfiles()
            val profileState = databaseViewModel.profiles.collectAsState()
            val profile = profileState.value
            profile.forEach { profil ->
                DropdownMenuItem(
                    leadingIcon = { Icon(Icons.Outlined.AccountCircle, contentDescription = null)},
                    text = { Text(profil.profilBezeichnung) },
                    onClick = {expanded = false
                        databaseViewModel.changeCurrentProfilId(profil.id) }
                )
            }
            HorizontalDivider()
            DropdownMenuItem(
                leadingIcon = { Icon(Icons.Outlined.Edit, contentDescription = null)},
                text = { Text("Profile bearbeiten") },
                onClick = {expanded = false
                    navigateToProfilAnsicht(navController) }
            )
        }
    }
}


@Composable
// Composable für die Anzeige des Stundenplans, einschließlich Zeitleiste, Datumsleiste und Blöcke
fun Stundenplan(
    databaseViewModel: DatabaseViewModel,
    navController: NavHostController,
    dateStateHolder: DateStateHolder,
){
    val date = dateStateHolder.getDate()

    // Ermittelt Woche basierend auf Systemsprache
    val weekFields = WeekFields.of(Locale.getDefault())
    // Ermittelt Kalenderwoche basierend auf dem Datum
    val kalenderwoche = date.get(weekFields.weekOfWeekBasedYear())

    val aktuelleWoche: Boolean = kalenderwoche == LocalDate.now().get(weekFields.weekOfWeekBasedYear())
    // Erstellt eine Liste von Daten (Singular: Datum) für die Wochentage (Montag bis Freitag)
    val datumListe = listOf(
        date.with(DayOfWeek.MONDAY),
        date.with(DayOfWeek.TUESDAY),
        date.with(DayOfWeek.WEDNESDAY),
        date.with(DayOfWeek.THURSDAY),
        date.with(DayOfWeek.FRIDAY)
    )
    // Horizontale Zeile für die Zeitleiste, DatumLeiste und Blöcke
    val hinweisExtraktorFehler by databaseViewModel.hinweisExtraktorFehler.collectAsState()
    if (hinweisExtraktorFehler) {
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.error) // Hier wird die Hintergrundfarbe gesetzt


        ) {
            AutoResizedText(
                text = "Fehler beim Laden neuer Hinweise. Bitte versuche es erneut.",
                color = MaterialTheme.colorScheme.onError,
                style = MaterialTheme.typography.bodyMedium, // Textstil anpassen
            )
        }
    }
    Row(
        modifier = Modifier
            .fillMaxWidth() // Füllt die gesamte Breite aus
            .padding(top = 2.dp, end = 5.dp)
    ){
        val profilId by databaseViewModel.getCurrentProfilId().collectAsState()

        var kurse: List<Kurs>
        runBlocking {
            kurse = databaseViewModel.getKurseByProfil(profilId) // Holt die Liste der Kurse
        }

        val blocksAnzahl = determineBlockCount(kurse)
        Zeitleiste(kalenderwoche, aktuelleWoche, blocksAnzahl)

        Column {
            // Zeigt die Datumsleiste für die Wochentage an
            DatumLeiste(datumListe, databaseViewModel)
            // Zeichnet die Blöcke für die Kurse basierend auf den Daten und der Kalenderwoche
            SwipeNavigationArea(
                onSwipeLeft = {
                    // Deine Funktion für "Nächster Tag"
                    dateStateHolder.determineDate(1)    },
                onSwipeRight = {
                    // Deine Funktion für "Vorheriger Tag"
                    dateStateHolder.determineDate(-1)
                }
            ) {
                Box {
                    DrawBloecke(databaseViewModel, kalenderwoche, datumListe, navController, kurse, blocksAnzahl)
                    Box(
                        modifier = Modifier
                            .padding(start = 5.dp)
                            .fillMaxSize(),
                        contentAlignment = Alignment.BottomStart
                    ) {
                        var secSinceLastRefresh by remember { mutableLongStateOf(0L) }
                        val timestamp by databaseViewModel.lastRefreshTimestamp.collectAsState()

                        LaunchedEffect(timestamp) {
                            while (true) {
                                // Berechne die Differenz zwischen "jetzt" und dem gespeicherten Zeitstempel.
                                val currentTimestamp = System.currentTimeMillis()
                                secSinceLastRefresh = if (timestamp > 0L) {
                                    (currentTimestamp - timestamp) / 1000L
                                } else {
                                    0L
                                }
                                delay(1000L)
                            }
                        }

                        val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

                        val formattedDate = when {
                            secSinceLastRefresh > 259200 -> "${sdf.format(Date(timestamp))}"
                            secSinceLastRefresh in 172800..<259200 -> "Vorgestern"
                            secSinceLastRefresh in 86401..<172800 -> "Gestern"
                            secSinceLastRefresh > 3600 -> "Vor ${secSinceLastRefresh / 60 / 60} Stunden"
                            secSinceLastRefresh in 61..3600 -> "Vor ${secSinceLastRefresh / 60} Minuten"
                            secSinceLastRefresh < 60 -> "gerade eben"
                            else -> {"Unbekannt"}
                        }

                        AutoResizedText(
                            text = "Zuletzt aktualisiert: $formattedDate",
                        )
                    }
                }
            }

        }
    }
}

fun determineBlockCount(kurse: List<Kurs>): Int {
    var blocksAnzahl = 3
    val kurseWithPossibleFourthBlock = kurse.filter {
        (it.mondayBlock ?: 0)  > blocksAnzahl ||
        (it.tuesdayBlock ?: 0) > blocksAnzahl ||
        (it.wednesdayBlock ?: 0) > blocksAnzahl ||
        (it.thursdayBlock ?: 0) > blocksAnzahl ||
        (it.fridayBlock ?: 0) > blocksAnzahl
    }
    for (kurs in kurseWithPossibleFourthBlock) {
        val blocks = listOf(
            kurs.mondayBlock,
            kurs.tuesdayBlock,
            kurs.wednesdayBlock,
            kurs.thursdayBlock,
            kurs.fridayBlock
        )
        for (block in blocks) {
            if (block != null && blocksAnzahl == 3) {
                if (block > 4) {
                    val blocksForDay = convertFromBlocksCodeToBlocksId(block)
                    if (blocksForDay.contains(4)) {
                        blocksAnzahl = 4
                    }
                } else if (block == 4) {
                    blocksAnzahl = 4
                }
            }
        }
    }
    return blocksAnzahl
}
@Composable
// Malt auf der linken Seite am Rand die Zeitleiste
fun Zeitleiste(kalenderwoche: Int, aktuelleWoche: Boolean, blocksAnzahl: Int) {
    // Erstellt eine vertikale Anordnung für die Zeitleiste
    Column(modifier = Modifier
        .fillMaxHeight() // Füllt die gesamte Höhe aus
    ) {
        // Erstellt eine Karte für die Kalenderwoche
        Card(
            modifier = Modifier
                .height(50.dp) // Höhe der Karte
                .width(45.dp),  // Breite der Karte
            shape = RectangleShape, // Rechteckige Form der Karte
            colors = CardDefaults.cardColors(
                containerColor = Color.Transparent, // Hintergrundfarbe der Karte
            ),
        ) {
            // Box zur Zentrierung des Inhalts innerhalb der Karte
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 4.dp) // Fügt links einen Abstand hinzu
                , // Füllt die gesamte Größe der Karte aus
                contentAlignment = Alignment.Center // Zentriert den Inhalt

            ) {
                // Text für die Kalenderwoche
                AutoResizedText(
                    text = "KW $kalenderwoche", // Anzeige der Kalenderwoche
                    color = if (aktuelleWoche)  MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                )
            }
        }
        val intervals = mutableListOf("08:05", "09:35", "09:55", "11:25", "11:45", "13:15", "14:00", "15:30")
        if (blocksAnzahl == 4) {
            intervals.add("15:30")
            intervals.add("16:30")
        }
        // Liste von Zeitintervallen für den Stundenplan
        intervals.forEach { zeit ->
            Box(
                modifier = Modifier
                    .weight(1f)

            ) {
                // Erstellt eine Karte für jedes Zeitintervall
                Card(
                    modifier = Modifier
                        .width(45.dp), // Breite der Karte
                    shape = RectangleShape, // Rechteckige Form der Karte
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Transparent, // Hintergrundfarbe der Karte
                    ),

                    ) {
                    // Box zur Zentrierung des Inhalts innerhalb der Karte
                    Box(
                        contentAlignment = Alignment.Center, // Zentriert den Inhalt
                        modifier = Modifier
                            .fillMaxSize() // Füllt die gesamte Größe der Karte aus
                            .padding(start = 4.dp) // Fügt links einen Abstand hinzu
                    ) {
                        // Text für das Zeitintervall
                        AutoResizedText(
                            text = zeit, // Anzeige des Zeitintervalls
                            // Größe geteilt durch Wert Vergrößerungsfaktor, damit Schriftgröße Darstellung nicht beeinträchtigt
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                }
            }

        }
    }
}
@Composable
fun DatumLeiste(datumListe: List<LocalDate>, databaseViewModel: DatabaseViewModel) {
    val updateTrigger by databaseViewModel.hinweisUpdateTrigger.collectAsState()

    // 1. Zustand für die vorberechneten Hinweis-Informationen als Map.
    var hinweisMap by remember { mutableStateOf<Map<String, Boolean>>(emptyMap()) }

    // 2. Zustand, der sich merkt, FÜR WELCHES DATUM der Dialog angezeigt werden soll.
    var dialogFuerDatum by remember { mutableStateOf<LocalDate?>(null) }

    // 3. `LaunchedEffect` lädt die `hinweisMap` einmalig oder wenn sich `datumListe` ändert.
    LaunchedEffect(datumListe, updateTrigger) {
        val dateStrings = datumListe.map { it.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")) }
        hinweisMap = databaseViewModel.checkHinweiseForDateRange(dateStrings)
    }

    // 4. Der Dialog wird nur dann angezeigt, wenn `dialogFuerDatum` nicht `null` ist.
    if (dialogFuerDatum != null) {
        NoHinweiseDialog(onDismiss = { dialogFuerDatum = null }) // Beim Schließen wird der Zustand zurückgesetzt.
    }

    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier.fillMaxWidth()
    ) {
        datumListe.forEach { datum ->
            val today = LocalDate.now()
            val isCurrentDay = datum == today
            val isPastDate = datum < today
            val wochentag = datum.format(DateTimeFormatter.ofPattern("EEE")).replace(".", "")
            val datumString = datum.format(DateTimeFormatter.ofPattern("dd.MM"))
            val datumKey = datum.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))

            // Greife auf das vorbereitete Ergebnis aus der Map zu.
            val hinweiseExist = hinweisMap[datumKey] ?: true
            val showWarningIcon = !hinweiseExist && !isPastDate

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier
                    .height(70.dp)
                    .padding(start = 5.dp)
                    .weight(1f)
                    // c) Der Klick-Handler wird nur bei Bedarf hinzugefügt.
                    .then(
                        if (showWarningIcon) {
                            Modifier.clickable {
                                dialogFuerDatum = datum
                            }
                        } else {
                            Modifier // Ansonsten ist das Element nicht klickbar.
                        }
                    )
            ) {
                // Wochentag anzeigen
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(30.dp)
                        .then(
                            if (isCurrentDay) Modifier
                                .padding(horizontal = 12.dp)
                                .border(
                                    width = 2.dp,
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(6.dp)
                                )
                            else Modifier
                        )
                ) {
                    AutoResizedText(
                        text = wochentag,
                        style = MaterialTheme.typography.headlineSmall,
                        color = if (isCurrentDay) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    )
                }

                    // Datum und optionales Warn-Icon anzeigen
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(30.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(30.dp) // Feste Höhe für das Datum
                        ) {
                            val finalDatumString = if (showWarningIcon) {
                                "$datumString ⚠️"
                            } else {
                                datumString
                            }

                        AutoResizedText(
                            text = finalDatumString, // Übergib den finalen String.
                            style = MaterialTheme.typography.titleSmall,
                            color = if (isCurrentDay) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}
@Composable
fun NoHinweiseDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss, // Schließen durch ein Klick außerhalb des Dialogs
        title = { Text("Keine Hinweise gefunden") },
        text = { Text("Die App konnte keine Hinweise für diesen Tag finden. Überprüfe unbedingt die aktuellen Hinweise auf der Schulwebseite!") },
        confirmButton = {
            Button(
                onClick = {
                    val intent = Intent(Intent.ACTION_VIEW,
                        "https://cvo-gyo.de/aktuelle-hinweise".toUri())
                    // Optional: Intent chooser
                    val chooser = Intent.createChooser(intent, "Open with")
                    context.startActivity(chooser)
                    onDismiss()
                }

            ) { // Schließen durch Bestätigen
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text("Aktuelle Hinweise öffnen")
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.OpenInNew,
                        contentDescription = null, // Barrierefreiheit
                        modifier = Modifier
                            .padding(start = 4.dp) // Abstand zwischen Icon und Text
                            .size(20.dp)
                    )
                }
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) { // Schließen durch Abbrechen
                Text("Verstanden")
            }
        }
    )
}

@Composable
fun SwipeNavigationArea(
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    content: @Composable () -> Unit
) {
    // Summe der Wischbewegung
    var offsetX by remember { mutableStateOf(0f) }

    Box(
        modifier = Modifier.pointerInput(Unit) {
            detectHorizontalDragGestures(
                onDragEnd = {
                    // Wenn die Geste endet, prüfen, ob weit genug gewischt wurde
                    // (z.B. mehr als 100 Pixel)
                    if (offsetX > 100) {
                        onSwipeRight()
                    } else if (offsetX < -100) {
                        onSwipeLeft()
                    }
                    offsetX = 0f // Reset für den nächsten Swipe
                },
                onHorizontalDrag = { change, dragAmount ->
                    change.consume() // Verhindert, dass andere Elemente das Event abfangen
                    offsetX += dragAmount
                }
            )
        }
    ) {
        content()
    }
}

@Composable
fun DrawBloecke(
    databaseViewModel: DatabaseViewModel,
    kalenderwoche: Int,
    datumListe: List<LocalDate>,
    navController: NavController,
    kurse: List<Kurs>,
    blocksAnzahl: Int,
) {
    val updateTrigger by databaseViewModel.hinweisUpdateTrigger.collectAsState()
    val areCardsFilled by databaseViewModel.areCardsFilled.collectAsState()
    // Erstellt eine horizontale Zeile für die Darstellung der Blöcke
    Row(
        modifier = Modifier
            .fillMaxWidth(), // Füllt die gesamte Breite aus
        horizontalArrangement = Arrangement.SpaceEvenly // Gleichmäßige Verteilung der Spalten
    ) {
        // Liest die Nutzerkurse aus der Datenbank

        // Beide for-Loops sind dazu da, alle Kurse an ihrer Position zu verteilen
        // D.h. man geht für jeden Tag jeden Block durch und platziert dort Cards
        // Man hat also immer 5 * 4 Cards (5 Tage, 4 Blöcke pro Tag)
        for (day in 0..4) {
            Column(modifier = Modifier.weight(1f)) { // Erstellt eine Spalte für jeden Tag
                for (block in 0..blocksAnzahl) { // Iteriert über die 4 Blöcke pro Tag
                    // Standardmäßig false, weil unklar, ob an der Stelle ein Kurs eingetragen ist
                    var cardVisible = false

                    // Remember-Variablen, um den Zustand von Kurs und Hinweis zu speichern
                    val kursState = remember { mutableStateOf<Kurs?>(null) }
                    val hinweisState = remember { mutableStateOf<Hinweis?>(null) }

                    val date = datumListe[day].format(DateTimeFormatter.ofPattern("dd.MM.yyyy")).toString()
                    // LaunchedEffect sorgt dafür, dass der Inhalt der UI sich aktualisiert,
                    // wenn eines der Parameter sich verändern

                    LaunchedEffect(kurse, day, block, kalenderwoche, updateTrigger) {
                        val kursObject = getKursObject(kurse, day, block, kalenderwoche)
                        kursState.value = kursObject
                        // Setze den alten Hinweis zurück, um veraltete Daten zu vermeiden.
                        hinweisState.value = null

                        if (kursObject != null) {
                            val hinweis = databaseViewModel.findSpecificHinweis(
                                kursName = kursObject.kursName,
                                date = date
                            )
                            hinweisState.value = hinweis
                        }
                    }

                    // Überprüft, ob ein Kurs vorhanden ist
                    if (kursState.value != null) {
                        cardVisible = true
                    }


                    val isKlausur = cardVisible && klausurStatus(databaseViewModel, date, kursState)
                    // Erstellt eine Karte für den Block
                    val kursColor = Color(kursState.value?.kursColor ?: 0x00000000)
                    val shapeSize = if (areCardsFilled) {
                        12.dp
                    } else {
                        8.dp
                    }
                    Card(
                        shape = RoundedCornerShape(shapeSize),
                        modifier = Modifier
                            .weight(1f) // Gewichtung für gleichmäßige Verteilung
                            .padding(top = 6.dp, start = 5.dp) // Abstand oben und links
                            // Für Klausurtag in zukünftlihcen Releases
                            .then(
                                if (isKlausur) {
                                    Modifier
                                        .border(
                                            width = 2.dp,
                                            color = Color(0xFFDC143C),
                                            shape = RoundedCornerShape(shapeSize + 4.dp)
                                        )
                                        .padding(4.dp) // Packt die Border nach Außen
                                } else {
                                    Modifier
                                }
                            )
                            .then(
                                // Macht die Karte voll transparent / unsichtbar, wenn kein Kurs vorhanden ist
                                if (!cardVisible) Modifier.alpha(0f) else Modifier
                            ),
                        colors = CardDefaults.cardColors(
                            containerColor = if (areCardsFilled) {
                                kursColor
                            } else {
                                MaterialTheme.colorScheme.surfaceContainerHighest

                            },
                            contentColor = if (areCardsFilled) {
                                getTextColor(kursState)
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        ),
                    ) {
                        // Wenn die Karte sichtbar ist, füge interaktive Elemente hinzu
                        if (cardVisible) {
                            Box(
                                modifier = Modifier
                                    .clickable {
                                        // Navigiert zur Kursansicht, wenn die Karte angeklickt wird
                                        navigateToKursAnsicht(
                                            navController,
                                            kursState.value,
                                            hinweisState.value,
                                            date
                                        )
                                    },
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(), // The Row itself should fill the Card
                                    verticalAlignment = Alignment.CenterVertically // Aligns the line and the infos vertically
                                ) {
                                    if (!areCardsFilled) {
                                        Canvas(modifier = Modifier
                                            .fillMaxHeight() // The line should be as tall as the Card
                                            .width(12.dp) // The thickness of the line
                                        ) {
                                            drawLine(
                                                color = kursState.value?.kursColor?.let { Color(it) } ?: Color.Transparent,
                                                start = Offset(x = center.x, y = 26f), // Draw in the center of the Canvas's width
                                                end = Offset(x = center.x, y = size.height - 26f),
                                                strokeWidth = 12f,
                                                cap = StrokeCap.Round
                                            )
                                        }
                                    }
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight() // Fill the available height
                                            .weight(1f), // Take up all remaining horizontal space
                                        contentAlignment = Alignment.Center // Zentriert den Inhalt
                                    ) {
                                        // Zeigt die Informationen für den Block an
                                        BlockInfos(kursState, hinweisState, databaseViewModel, date)
                                    }
                                }
                            }

                        }
                    }
                    // Spacer(modifier = Modifier.height(30.dp)) Gut als Pause vllt
                }
                Spacer(modifier = Modifier.height(22.dp)) // Space für Zuletzt Aktualisiert
            }
        }
    }
}
// Sorgt dafür, dass die Schriftfarbe auf dunklen Hintergründen
// hell ist und andersrum
fun getTextColor(kursState: MutableState<Kurs?>): Color {
    val darkBackgrounds = listOf(
        farben["Lila"],
        farben["Braun"],
        farben["Grau"],
        farben["Schwarz"],
        farben["Dunkelblau"],
        farben["Blau"],
        farben["Dunkelrot"],
        farben["Rot"],
        farben["Dunkellila"],
        )
    if (kursState.value != null) {
        if (kursState.value!!.kursColor in darkBackgrounds) {
            return Color.White }
    }
    return Color.Black
}

fun klausurStatus(databaseViewModel: DatabaseViewModel, date: String, kursState: MutableState<Kurs?>): Boolean {
    var kursNote: KursNote? = null
    runBlocking {
        val datesList = databaseViewModel.getDistinctDates()
        val dateAlreadyExists = datesList.contains(date)
        if (dateAlreadyExists) {
            val dayId = databaseViewModel.getIdByDate(date)
            kursNote = databaseViewModel.getKursNoteByDayIdAndKursName(dayId, kursState.value!!.kursName)
        }
    }
    return kursNote?.klausur == true
}

@Composable
// Stellt die Informationen für den Kurs dar
fun BlockInfos(kursState: MutableState<Kurs?>, hinweisState: MutableState<Hinweis?>, databaseViewModel: DatabaseViewModel, date: String) {
    var kursNote by remember { mutableStateOf<KursNote?>(null) }
    runBlocking {
        val dayId = databaseViewModel.getIdByDate(date)
        kursNote = databaseViewModel.getKursNoteByDayIdAndKursName(dayId, kursState.value?.kursName ?: "")
    }
    val hausaufgabe = kursNote?.homework != null && kursNote?.homeworkCompleted == false
    val bemerkung = kursNote?.comment != null
        // Erstellt eine vertikale Anordnung für die Kursinformationen
    val note = hausaufgabe || bemerkung
    Column(
        modifier = Modifier
            .fillMaxHeight() // Füllt die gesamte Höhe aus
            .padding(
                start = 6.dp,
                bottom = if (note) 18.dp else 6.dp,
                top = 6.dp,
                end = 6.dp
            ), // Fügt Abstand in alle 4 Richtungen hinzu
        verticalArrangement = Arrangement.SpaceBetween, // Verteilt die Elemente gleichmäßig
    ) {
        // Zeigt den Namen des Kurses an
        Box {
            AutoResizedText(
                text = kursState.value?.kursName ?: "", // Wenn kein Kurs-Objekt vorhanden ist, wird ein leerer String angezeigt

                )
        }
        Box{
            // Zeigt das Kürzel des Lehrers an
            AutoResizedText(
                text = kursState.value?.lehrerkuerzel ?: "", // Wenn kein Kurs-Objekt vorhanden ist, wird ein leerer String angezeigt
            )
        }
        Box {
            // Zeigt den Raum an, in dem der Kurs stattfindet
            AutoResizedText(
                text = kursState.value?.raum ?: "", // Wenn kein Kurs-Objekt vorhanden ist, wird ein leerer String angezeigt
            )
        }
        Box {
            // Zeigt den Hinweistyp an, falls vorhanden
            AutoResizedText(
                text = hinweisState.value?.hinweisTyp ?: "", // Wenn kein Hinweis vorhanden ist, wird ein leerer String angezeigt
                color = Color.Black, // Schriftfarbe
                fontWeight = FontWeight.Bold, // Fette Schrift
                modifier = Modifier
                    .fillMaxWidth() // Füllt die gesamte Breite aus
                    .clip(RoundedCornerShape(4.dp)) // Abgerundete Ecken
                    .then(
                        // Sorgt für die weiße Box bei Hinweisen
                        if (hinweisState.value?.hinweisTyp != null) {
                            Modifier.background(Color.White) // Hintergrundfarbe weiß, wenn ein Hinweis vorhanden ist
                        } else {
                            Modifier // Andernfalls kein zusätzlicher Modifier
                        }
                    )
            )
        }
    }
    val areCardsFilled by databaseViewModel.areCardsFilled.collectAsState()
    val contentColor = if (areCardsFilled) {
        getTextColor(kursState)
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    if (note) {
        Row(
            horizontalArrangement = Arrangement.End, // Align items to the right
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(4.dp)

        ) {
            if (hausaufgabe) {
                BlockInfosIcon(contentColor, Icons.Filled.Home, "Hausaufgabe")
            }
            if (bemerkung) {
                BlockInfosIcon(contentColor, Icons.Filled.Info, "Info")
            }
        }
    }
}

@Composable
fun BlockInfosIcon(contentColor: Color, imageVector: ImageVector, contentDescription: String) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .padding(end = 2.dp)
            .border(
                BorderStroke(1.dp, contentColor.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(16.dp)
            ) // Semi-transparent border
            .size(12.dp) // Size of the circle

    ) {

        Icon(
            imageVector = imageVector,
            contentDescription = contentDescription,
            tint = contentColor, // Icon tint
            modifier = Modifier
                .alpha(1f)
                .size(10.dp)
        )
    }
}

fun getKursObject(
    kurse: List<Kurs>,
    day: Int,
    block: Int,
    kalenderwoche: Int
): Kurs? {
    for (kurs in kurse) {
        // Kriegt die Regelmäßigkeit für den Tag
        // also 0, 1, 2 für jede, ungerade, gerade Woche
        val courseRegularityForDay = when (day) {
            0 -> kurs.mondayRegularity
            1 -> kurs.tuesdayRegularity
            2 -> kurs.wednesdayRegularity
            3 -> kurs.thursdayRegularity
            else -> kurs.fridayRegularity
        }
        // Bool, ob Regelmäßigkeit übergebene kalenderwoche betrifft.
        val isCurrentWeek = if (courseRegularityForDay == 0) { // 0 bedeutet jede Woche
            true
        } else if (kalenderwoche.mod(2) == 1 && courseRegularityForDay == 1) {
            true
        } else if (kalenderwoche.mod(2) == 0 && courseRegularityForDay == 2) {
            true
        } else {
            false
        }

        if (isCurrentWeek) {
            // Holt den Block, der am gefragten Tag für den Kurs wäre
            val courseBlockForDay = when (day) {
                0 -> kurs.mondayBlock
                1 -> kurs.tuesdayBlock
                2 -> kurs.wednesdayBlock
                3 -> kurs.thursdayBlock
                else -> kurs.fridayBlock
            }

            // Hier spezielle Nummern checken
            if (courseBlockForDay != null) {

                val courseBlocks: List<Int> = when (courseBlockForDay) {
                    0 -> listOf(0) // 1/2
                    1 -> listOf(1) // 3/4
                    2 -> listOf(2) // 5/6
                    3 -> listOf(3) // 7/8
                    4 -> listOf(4) // 9/10

                    else -> convertFromBlocksCodeToBlocksId(courseBlockForDay) // Fallback für ungültige Werte
                }

                if (courseBlocks.contains(block)) {
                    return kurs
                }
            }

        }
    }
    return null
}

fun navigateToProfilAnsicht(navController: NavController) {
    navController.navigate("ProfilAnsicht")

}
fun navigateToKursAnsicht(
    navController: NavController,
    kurs: Kurs?,
    hinweis: Hinweis?,
    date: String
) {
    // Konvertiere in Json-String, damit komplexe Daten
    // an Navigation übergeben werden können
    val kursJson = Gson().toJson(kurs)
    val hinweisJson = Gson().toJson(hinweis)

    // URLEncoder.encode() wandelt spezielle Zeichen in ein freundliches Format für Routen
    // Routen Bedeutung: Wegbeschreibung zum Ziel, also Screen
    val encodedKursJson = URLEncoder.encode(kursJson, StandardCharsets.UTF_8.toString())
    val encodedHinweisJson = URLEncoder.encode(hinweisJson, StandardCharsets.UTF_8.toString())

    // Navigiere zur KursAnsicht und übergebe die kodierten JSON-Strings als Parameter
    navController.navigate("KursAnsicht/$encodedKursJson/$encodedHinweisJson/$date")
}

