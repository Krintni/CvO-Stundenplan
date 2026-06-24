package cvo.stundenplan.app.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.TextSnippet
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.HomeWork
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.Room
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.gson.Gson
import cvo.stundenplan.app.data.DatabaseViewModel
import cvo.stundenplan.app.data.Day
import cvo.stundenplan.app.data.Hinweis
import cvo.stundenplan.app.data.Kurs
import cvo.stundenplan.app.data.KursNote
import cvo.stundenplan.app.data.ProfilKursRelation
import kotlinx.coroutines.runBlocking
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun KursAnsicht(
    databaseViewModel: DatabaseViewModel,
    navController: NavController,
    kurs: Kurs,
    hinweis: Hinweis?,
    date: String
    ) {
    Surface(
        modifier = Modifier.fillMaxSize(), // Nimmt die ganze Seite ein
        color = MaterialTheme.colorScheme.background // Hintergrundfarbe, die automatisch im Dark Mode angepasst wird
    ){
        Scaffold(
            topBar = { KursTopBar(navController) }
        ) { innerpadding ->
            Box(
                modifier = Modifier
                    .padding(innerpadding)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Inhalte der Spalte zum oben im Bildschirm
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 0.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            // Icon bleibt in der Mitte
                            Box(
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Book, // Ersetze "Home" durch das gewünschte Icon
                                    contentDescription = "Kurs", // Beschreibung für Barrierefreiheit
                                    modifier = Modifier
                                        .size(40.dp) // Größe des Icons
                                )
                            }
                            // Text unter dem Icon
                            Spacer(Modifier.height(6.dp))
                            Text(
                                text = kurs.kursName,
                                fontSize = 24.sp,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(top = 120.dp, bottom = 100.dp)
                    ) {
                        item {

                            kurs.lehrerkuerzel?.let {
                                Row {
                                    Icon(
                                        imageVector = Icons.Outlined.PersonOutline, // Ersetze "Home" durch das gewünschte Icon
                                        contentDescription = "Lehrerkürzel", // Beschreibung für Barrierefreiheit
                                        modifier = Modifier
                                            .padding(end = 10.dp)
                                            .size(26.dp) // Größe des Icons
                                    )
                                    Text(
                                        kurs.lehrerkuerzel
                                    )

                                }

                            }
                            Spacer(modifier = Modifier.height(20.dp))

                            kurs.raum?.let {
                                Row {
                                    Icon(
                                        imageVector = Icons.Outlined.Room,
                                        contentDescription = "Raum",
                                        modifier = Modifier
                                            .padding(end = 10.dp)
                                            .size(26.dp) // Größe des Icons
                                    )
                                    Text(
                                        text = it
                                    )
                                }

                            }
                            Spacer(modifier = Modifier.height(10.dp))

                            var kursNote = KursNote(
                                dayId = -1,
                                kurs = kurs.kursName,
                                homework = null,
                                homeworkCompleted = false,
                                comment = null,
                                klausur = false,
                            )
                            runBlocking {
                                val datesList = databaseViewModel.getDistinctDates()
                                val dateAlreadyExists = datesList.contains(date)
                                if (dateAlreadyExists) {
                                    val dayId = databaseViewModel.getIdByDate(date)
                                    val kursNoteDB = databaseViewModel.getKursNoteByDayIdAndKursName(kursName = kurs.kursName, dayId = dayId)
                                    if (kursNoteDB != null) {
                                        kursNote = kursNoteDB
                                    }
                                }
                            }
                            if (kursNote.dayId == -1) {
                                runBlocking {
                                    databaseViewModel.upsertKursNote(kursNote)
                                    val kursNoteId = databaseViewModel.getKursNoteByDayIdAndKursName(-1, kurs.kursName)!!.id
                                    kursNote.id = kursNoteId
                                    databaseViewModel.deleteKursNote(kursNote)
                                }
                            }

                            BasicNote(
                                imageVector = Icons.Outlined.HomeWork,
                                contentDescription = "Hausaufgabe",
                                title = "Hausaufgabe",
                                noteFromDB = kursNote.homework,
                                homeworkCompleted = kursNote.homeworkCompleted,
                                date = if (kursNote.dayId == -1) date else "",
                                kursNote = kursNote,
                                databaseViewModel = databaseViewModel
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            BasicNote(
                                imageVector = Icons.Outlined.EditNote,
                                contentDescription = "Bemerkung",
                                title = "Bemerkung",
                                noteFromDB = kursNote.comment,
                                date = if (kursNote.dayId == -1) date else "",
                                kursNote = kursNote,
                                databaseViewModel = databaseViewModel
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            KlausurCheckbox(
                                klausur = kursNote.klausur,
                                date = if (kursNote.dayId == -1) date else "",
                                databaseViewModel = databaseViewModel,
                                kursNote = kursNote
                                )
                            Spacer(modifier = Modifier.height(10.dp))

                            if (hinweis != null) {
                                val hinweisTyp = hinweis.hinweisTyp
                                val shortHinweis = getShortHinweis(hinweis.hinweis, kurs.kursName)
                                Row {
                                    Icon(
                                        imageVector = Icons.Outlined.Info,
                                        contentDescription = "Kurzer Hinweis",
                                        modifier = Modifier
                                            .padding(end = 10.dp)
                                            .size(26.dp) // Größe des Icons

                                    )
                                    Text(text = "${hinweisTyp}: $shortHinweis")
                                }
                                Spacer(modifier = Modifier.height(20.dp))

                                Row {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Outlined.TextSnippet,
                                        contentDescription = "Vollständiger Hinweis",
                                        modifier = Modifier
                                            .padding(end = 10.dp)
                                            .size(26.dp) // Größe des Icons

                                    )
                                    val regex = Regex(kurs.kursName, RegexOption.IGNORE_CASE)
                                    val hinweisSplit = regex.split(hinweis.hinweis)
                                    Text(
                                        text = buildAnnotatedString {

                                            append("${hinweisTyp}: ${hinweisSplit[0]}")
                                            pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                                            append(kurs.kursName)
                                            pop()
                                            append(hinweisSplit[1])
                                        }
                                    )
                                }

                            }
                        }

                    }
                    // Buttons am unteren Rand des Bildschirms
                    Buttons(
                        kurs = kurs,
                        databaseViewModel = databaseViewModel,
                        navController = navController,
                        modifier = Modifier
                            .padding(0.dp)
                            .align(Alignment.BottomCenter) // Ganz unten ausrichten
                            .fillMaxWidth() // Buttons über die ganze Breite strecken
                    )
                }
            }

        }

    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KursTopBar(navController: NavController) {
    CenterAlignedTopAppBar(
        navigationIcon = {
            IconButton(
                onClick = {navController.popBackStack()}
            ) {
                Icon(
                    imageVector = Icons.Outlined.Close,
                    contentDescription = "Profilansicht schließen"
                )
            }
        },
        title = {
            Text("Kursdetails")
        }
    )
}

@Composable
fun BasicNote(
    imageVector: ImageVector,
    contentDescription: String,
    title: String,
    noteFromDB: String?,
    homeworkCompleted: Boolean = false,
    date: String,
    kursNote: KursNote,
    databaseViewModel: DatabaseViewModel) {

    var isCompleted by remember { mutableStateOf(homeworkCompleted) }

    var note by remember { mutableStateOf(noteFromDB ?: "") }
    val isNoteEmpty = note == ""
    var showText by remember { mutableStateOf(true) }

    val focusRequester = remember { FocusRequester() }

    // Makes Row clickable
    Button(
        onClick = { showText = false },
        shape = MaterialTheme.shapes.extraSmall,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        contentPadding = PaddingValues(0.dp),
        modifier = Modifier
            .background(Color.Transparent)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = imageVector,
                contentDescription = contentDescription,
                modifier = Modifier
                    .padding(end = 10.dp)
                    .size(26.dp) // Größe des Icons
            )
            Box(
                modifier = Modifier
                    .weight(1f)
            ) {
                if (showText) {
                    Column {
                        Text(
                            text = title,
                            fontWeight = FontWeight.Normal,
                            fontSize = 16.sp
                        )
                        Text(
                            text = if (isNoteEmpty) "(Tippen um zu bearbeiten)" else note,
                            fontWeight = FontWeight.Normal,
                            fontSize = 14.sp
                        )
                    }


                } else {
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text(title) },
                        maxLines = 5,
                        modifier = Modifier
                            .focusRequester(focusRequester) // FokusRequester zuweisen
                    )

                    LaunchedEffect(Unit) {
                        focusRequester.requestFocus() // Fordere den Fokus an, wenn das Eingabefeld sichtbar wird

                    }
                }
            }
            Box {
                Row {
                    // Anzeige ob Hausaufgabe erledigt ist
                    if (showText && title == "Hausaufgabe" && note != "") {
                        IconButton(onClick = {
                            isCompleted = !isCompleted
                            runBlocking {
                                kursNote.homeworkCompleted = isCompleted
                                databaseViewModel.upsertKursNote(kursNote)
                            }
                        }) {
                            Icon(
                                imageVector = if (isCompleted) {
                                    Icons.Outlined.CheckCircle // Symbol für erledigt
                                } else {
                                    Icons.Outlined.RadioButtonUnchecked // Symbol für nicht erledigt
                                },
                                contentDescription = if (isCompleted) {
                                    "Erledigt"
                                } else {
                                    "Nicht erledigt"
                                },
                                tint = if (isCompleted) Color.Green else Color.Red,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    } else if (!showText) {
                        IconButton(onClick = {
                            showText = true
                            // Wenn Date "" ist, dann heißt es, dass es bereits eine DayId gibt
                            if (date != "") {
                                runBlocking {
                                    databaseViewModel.insertDay(Day(date))
                                    kursNote.dayId = databaseViewModel.getIdByDate(date) // Id für Datum finden
                                }
                            }
                            if (title == "Hausaufgabe") {
                                kursNote.homework = note
                                kursNote.homeworkCompleted = isCompleted
                            } else {
                                kursNote.comment = note
                            }
                            runBlocking {
                                // In Case Nutzer löscht Text, also ""
                                if (kursNote.homework == "") {
                                    kursNote.homework = null
                                }
                                if (kursNote.comment == "") {
                                    kursNote.comment = null
                                }
                                databaseViewModel.upsertKursNote(kursNote)
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Outlined.Check, // Symbol für die vorherige Woche
                                contentDescription = "Bestätigen" // Beschreibung für Barrierefreiheit
                            )
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun KlausurCheckbox(klausur: Boolean?, date: String = "", databaseViewModel: DatabaseViewModel, kursNote: KursNote) {
    // State für die Checkbox
    var checkedState by remember { mutableStateOf(klausur ?: false) }

    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.Edit,
            contentDescription = "Raum",
            modifier = Modifier
                .size(26.dp) // Größe des Icons

        )
        Box(
            modifier = Modifier
                .weight(1f)
                .padding(start = 10.dp)
        ) {
            Text(text = "Als Klausurtag markieren") // Text neben der Checkbox

        }
        Box() {
            Checkbox(
                checked = checkedState, // Der aktuelle Zustand der Checkbox
                onCheckedChange = {
                    checkedState = it
                    if (date != "") {
                        runBlocking {
                            databaseViewModel.insertDay(Day(date))
                            kursNote.dayId = databaseViewModel.getIdByDate(date) // Id für Datum finden
                        }
                    }
                    kursNote.klausur = checkedState
                    runBlocking {
                        databaseViewModel.upsertKursNote(kursNote)
                    }
                } // Ändere den Zustand, wenn die Checkbox angeklickt wird
            )
        }
    }

}


fun getShortHinweis(extractedHinweisBlock: String, kursName: String): String? {
    var hinweisBlock = extractedHinweisBlock

    val start = 0

    while (hinweisBlock != "") {
        val commaPos = hinweisBlock.indexOf(",")
        val semicolonPos = hinweisBlock.indexOf(";")

        // Sucht den nächsten Separator
        val separatorPos = when {
            commaPos != -1 && semicolonPos != -1 ->
                minOf(commaPos, semicolonPos)
            commaPos == -1 && semicolonPos == -1 ->
                // Wenn kein Separator, nimm Ende des HinweisBlocks
                hinweisBlock.count()
            else ->
                maxOf(commaPos, semicolonPos)
        }
        // Extraktion des Hinweises von Start bis Ende des Hinweises
        val hinweis = hinweisBlock.slice(start..<separatorPos)
        val regex = Regex(kursName, RegexOption.IGNORE_CASE)
        if (regex.containsMatchIn(hinweis)) {
            return hinweis
        }
        // Erstellt einen kürzeren hinweisBlock ab dem letzten Separator
        // das sorgt dafür, dass der nächste Separator gefunden werden kann
        hinweisBlock = hinweisBlock.slice(separatorPos + 2..<hinweisBlock.count())
    }
    return null
}


// Funktion für untere Buttons
@Composable
fun Buttons(
    kurs: Kurs,
    databaseViewModel: DatabaseViewModel,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(bottom = 0.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp), // Abstand zwischen den Buttons
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Kurs löschen Button
        OutlinedButton(
            modifier = Modifier.weight(1f), // Teilt den Platz gleichmäßig auf
            onClick = { onDeleteButtonClicked(kurs, databaseViewModel, navController) }
        ) {
            Text("Kurs löschen")
        }

        // Kurs bearbeiten Button
        Button(
            modifier = Modifier.weight(1f), // Teilt den Platz gleichmäßig auf
            onClick = { onEditButtonClicked(kurs, navController) }
        ) {
            Text("Kurs bearbeiten")
        }
    }
}

fun onDeleteButtonClicked(kurs: Kurs, databaseViewModel: DatabaseViewModel, navController: NavController) {
    navController.popBackStack()
    runBlocking {
        val kursId = kurs.id
        val profilId = databaseViewModel.getCurrentProfilId().value
        val profilKursRelationId = databaseViewModel.getProfilKursRelationId(profilId, kursId)
        val profilKursRelation = ProfilKursRelation(profilId, kursId, profilKursRelationId)

        databaseViewModel.deleteProfilKursRelation(profilKursRelation)
        val kurseId = databaseViewModel.getKurseIdInProfiles()
        if (kurs.id !in kurseId) {
            databaseViewModel.deleteKurs(kurs)
        }
    }

}
fun onEditButtonClicked(kurs: Kurs, navController: NavController) {
    encodeAndNavigateToKursBearbeiten(navController, kurs)
}

fun encodeAndNavigateToKursBearbeiten(navController: NavController, kurs: Kurs) {
    val kursJson = Gson().toJson(kurs)
    val encodedKursJson = URLEncoder.encode(kursJson, StandardCharsets.UTF_8.toString())
    navController.navigate("KursHinzufuegen/${encodedKursJson}")
}