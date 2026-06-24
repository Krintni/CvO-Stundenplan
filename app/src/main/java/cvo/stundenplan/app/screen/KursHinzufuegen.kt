package cvo.stundenplan.app.screen


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableLongState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import cvo.stundenplan.app.backend.convertFromBlocksCodeToBlocksId
import cvo.stundenplan.app.backend.convertToBlockString
import cvo.stundenplan.app.backend.convertToCode
import cvo.stundenplan.app.data.DatabaseViewModel
import cvo.stundenplan.app.data.Kurs
import cvo.stundenplan.app.data.ProfilKursRelation
import kotlinx.coroutines.runBlocking

// Hauptfunktion, die das UI zum Hinzufügen eines Kurses definiert
@Composable
fun KursHinzufuegen(modifier: Modifier = Modifier, databaseViewModel: DatabaseViewModel, navController: NavController, kurs: Kurs? = null) {
    val ausgewaehlteTage = remember { mutableStateMapOf<String, Boolean>() }
    val wocheAuswahl = remember { mutableStateMapOf<String, String>() }
    // Die stundenBlockAuswahl Map für die Auswahl der Stundenblöcke
    val stundenBlockAuswahl = remember { mutableStateMapOf<String, List<String>>() }

    val kursName = remember { mutableStateOf("") }
    val lehrerkuerzel = remember { mutableStateOf("") }
    val raum = remember { mutableStateOf("") }
    val farbauswahl = remember { mutableLongStateOf(0xD9757575) }

    if (kurs != null) {
        enterExistingData(
            kurs,
            ausgewaehlteTage,
            wocheAuswahl,
            stundenBlockAuswahl,
            kursName,
            lehrerkuerzel,
            raum,
            farbauswahl
            )
    }
    Surface(
        modifier = Modifier.fillMaxSize(), // Nimmt die ganze Seite ein
        color = MaterialTheme.colorScheme.background // Hintergrundfarbe, die automatisch im Dark Mode angepasst wird
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp) // Allgemeines Padding für die Box
        ) {
            // Scrollbare Liste für Eingabefelder
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 100.dp) // Platz für die Buttons unten reservieren
            ) {
                // Eingabefelder für Kursdaten
                item { Spacer(modifier = Modifier.height(70.dp)) }
                var oldKursName = kursName.value
                item {
                    Field(
                        label = "Kursname: ",
                        value = kursName.value,
                        optional = false,
                        onValueChange = {
                            if (oldKursName.length > 1 && oldKursName.last().isDigit()) {
                                oldKursName = oldKursName.last().toString() // Nimmt den Raster

                            }
                            kursName.value = it
                            // Checkt den Raster
                            if (it.length > 1 && it.first().isDigit() && it.last().isDigit()) {
                                if (it.first().digitToInt() != 1) { // E-Phase läuft nicht nach Rastern
                                    var raster = it.last().toString()
                                    if (raster != oldKursName) {
                                        if (it[it.length -2].isDigit()) {
                                            raster = it[it.length -2].toString() + raster // Vorletzes Zeichen, falls z.B 10 Raster ist
                                        }
                                        insertRaster(raster.toInt(), ausgewaehlteTage, wocheAuswahl, stundenBlockAuswahl)
                                    }

                                }
                            }
                        }
                    )
                }
                item {
                    Field(
                        label = "Lehrerkürzel: ",
                        value = lehrerkuerzel.value,
                        onValueChange = { lehrerkuerzel.value = it }
                    )
                }
                item {
                    Field(
                        label = "Raum: ",
                        value = raum.value,
                        onValueChange = { raum.value = it }
                    )
                }
                item { Spacer(modifier = Modifier.height(10.dp)) }

                // Farbauswahl mit RadioButtons
                item {
                    FarbAuswahl(farbauswahl)
                }
                item {
                    Text(
                        text = "Tage auswählen:",
                        fontSize = 20.sp,
                        style = MaterialTheme.typography.bodyLarge
                    )

                }

                //Listen für Tage
                val tage = listOf("Montag", "Dienstag", "Mittwoch", "Donnerstag", "Freitag")

                // Checkboxen der Unterrichtstage
                tage.forEach { tag ->
                    item {
                        Column(
                            modifier = Modifier.padding(bottom = 8.dp)
                                .padding(top = 10.dp)
                        ) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, if (isSystemInDarkTheme()) Color.Gray else Color.LightGray)
                            ) {
                                TagAuswahl(ausgewaehlteTage, tag)

                                Spacer(modifier = Modifier.height(4.dp))
                                if (ausgewaehlteTage[tag] == true) {
                                    Column(modifier = Modifier.padding(start = 20.dp)) {
                                        WochenAuswahl(wocheAuswahl, tag)
                                        BlockAuswahl(stundenBlockAuswahl, tag)
                                    }
                                }
                            }

                        }
                    }
                }
            }
            //Call der Buttons Funktion, für die richtige plazierung
            Buttons(
                oldKurs = kurs,
                kursName = kursName.value,
                lehrerkuerzel = lehrerkuerzel.value,
                raum = raum.value,
                farbAuswahl = farbauswahl.longValue,
                ausgewaehlteTage = ausgewaehlteTage,
                wocheAuswahl = wocheAuswahl,
                stundenBlockAuswahl = stundenBlockAuswahl,
                databaseViewModel = databaseViewModel,
                navController = navController,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            )
        }
    }
}

// Funktion zum Laden vorhandener Kursdaten in die State-Variablen
fun enterExistingData(
    kurs: Kurs,
    ausgewaehlteTage: MutableMap<String, Boolean>,
    wocheAuswahl: MutableMap<String, String>,
    stundenBlockAuswahl: SnapshotStateMap<String, List<String>>, // Angepasste Datenstruktur
    kursName: MutableState<String>,
    lehrerkuerzel: MutableState<String>,
    raum: MutableState<String>,
    farbauswahl: MutableLongState
) {
    val wochentage = listOf(
        "Montag",
        "Dienstag",
        "Mittwoch",
        "Donnerstag",
        "Freitag"
    )
    val tageRegularity = listOf(
        kurs.mondayRegularity,
        kurs.tuesdayRegularity,
        kurs.wednesdayRegularity,
        kurs.thursdayRegularity,
        kurs.fridayRegularity
    )
    val tageBlock = listOf(
        kurs.mondayBlock,
        kurs.tuesdayBlock,
        kurs.wednesdayBlock,
        kurs.thursdayBlock,
        kurs.fridayBlock
    )

    for ((index, tagRegularity) in tageRegularity.withIndex()) {
        if (tagRegularity != null) {
            ausgewaehlteTage[wochentage[index]] = true
            val block = when (tagRegularity) {
                0 -> "Jede Woche"
                1 -> "Ungerade Woche"
                else -> "Gerade Woche"
            }
            wocheAuswahl[wochentage[index]] = block
        }
    }

    var blocks = mutableListOf<String>()
    for ((index, tagBlock) in tageBlock.withIndex()) {
        if (tagBlock != null) {
            // Stunden
            when (tagBlock) {
                0 -> blocks.add("1/2")
                1 -> blocks.add("3/4")
                2 -> blocks.add("5/6")
                3 -> blocks.add("7/8")
                4 -> blocks.add("9/10")
                else -> blocks.addAll(convertToBlockString(convertFromBlocksCodeToBlocksId(tagBlock)))
                // Sonderzahlen für mehrere Blöcke
                // istr so, weil man nur eine Zahl in DB speichern kann

            }
            // Hier wird die Liste aktualisiert
            stundenBlockAuswahl[wochentage[index]] = blocks // Liste mit einem Element
            blocks = mutableListOf<String>()
        }
    }

    kursName.value = kurs.kursName
    lehrerkuerzel.value = kurs.lehrerkuerzel ?: ""
    raum.value = kurs.raum ?: ""
    farbauswahl.longValue = kurs.kursColor
}


// UI-Komponente für Textfelder der Kursdetails
@Composable
fun Field(label: String, value: String, onValueChange: (String) -> Unit, optional: Boolean = true) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))
        val focusManager = LocalFocusManager.current

        val context = LocalContext.current
        val configuration = context.resources.configuration
        val fontScale = configuration.fontScale

        TextField(
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = {
                    focusManager.moveFocus(FocusDirection.Down)
                }
            ),
            singleLine = true,
            value = value,
            onValueChange = { newText -> onValueChange(newText) },
            label = { Text(label) },

            isError = !optional && value.isEmpty(), // Setze isError auf true, wenn der Wert leer ist
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (fontScale < 1.15 ) {
                        Modifier.height(55.dp)
                    } else if (fontScale < 1.16) {
                        Modifier.height(60.dp)
                    }
                    else if (fontScale < 1.85) { // 1.85 Max Font Scale
                        Modifier.height(75.dp)
                    }
                    else {
                        Modifier.height(80.dp)
                    }
                )
        )

        // Optional: Fehlermeldung anzeigen, wenn das Feld leer ist
        if (value.isEmpty() && !optional) {
            Text(
                text = "* Pflichtfeld",
                color = if (isSystemInDarkTheme()) Color(0xFFF2B8B6) else Color.Red ,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

// UI-Komponente zur Auswahl der Farbe
@Composable
fun FarbAuswahl(farbauswahl: MutableLongState) {
    Text(
        text = "Farbe auswählen:",
        fontSize = 20.sp,
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier.padding(vertical = 8.dp)
    )

    Column(
        modifier = Modifier.fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        farben.forEach { (name, colorLong) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = farbauswahl.longValue == colorLong,  // Überprüft, ob die Farbe ausgewählt wurde
                        onClick = {
                            farbauswahl.longValue = colorLong
                        }  // Setzt die Auswahl auf diese Farbe
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = farbauswahl.longValue == colorLong,  // Wählt den RadioButton aus, wenn die Farbe übereinstimmt
                    onClick = { farbauswahl.longValue = colorLong }
                )
                Spacer(modifier = Modifier.width(8.dp))  // Abstand zwischen Button und Farbblock
                Box(
                    modifier = Modifier
                        .size(24.dp) // Die Größe des Farbbereiches
                        .clip(CircleShape)
                        .background(Color(colorLong))  // Setzt die Hintergrundfarbe
                )
                Spacer(modifier = Modifier.width(8.dp))  // Abstand zwischen Farbfeld und Text
                Text(
                    text = name,  // Der Name der Farbe
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

fun insertRaster(
    raster: Int,
    ausgewaehlteTage: MutableMap<String, Boolean>,
    wocheAuswahl: MutableMap<String, String>,
    stundenBlockAuswahl: MutableMap<String, List<String>>, // Angepasste Datenstruktur
) {
    ausgewaehlteTage.forEach { (tag) ->
        ausgewaehlteTage[tag] = false
    }
    val jede = "Jede Woche"
    val gerade = "Gerade Woche"
    val ungerade = "Ungerade Woche"

    when (raster) {
        1 -> {
            ausgewaehlteTage["Montag"] = true
            wocheAuswahl["Montag"] = ungerade
            stundenBlockAuswahl["Montag"] = listOf("3/4") // Liste mit einem Element

            ausgewaehlteTage["Mittwoch"] = true
            wocheAuswahl["Mittwoch"] = jede
            stundenBlockAuswahl["Mittwoch"] = listOf("3/4")

            ausgewaehlteTage["Freitag"] = true
            wocheAuswahl["Freitag"] = jede
            stundenBlockAuswahl["Freitag"] = listOf("3/4")
        }
        2 -> {
            ausgewaehlteTage["Montag"] = true
            wocheAuswahl["Montag"] = gerade
            stundenBlockAuswahl["Montag"] = listOf("3/4")

            ausgewaehlteTage["Dienstag"] = true
            wocheAuswahl["Dienstag"] = jede
            stundenBlockAuswahl["Dienstag"] = listOf("3/4")

            ausgewaehlteTage["Donnerstag"] = true
            wocheAuswahl["Donnerstag"] = jede
            stundenBlockAuswahl["Donnerstag"] = listOf("3/4")
        }
        3 -> {
            ausgewaehlteTage["Dienstag"] = true
            wocheAuswahl["Dienstag"] = jede
            stundenBlockAuswahl["Dienstag"] = listOf("1/2")

            ausgewaehlteTage["Donnerstag"] = true
            wocheAuswahl["Donnerstag"] = ungerade
            stundenBlockAuswahl["Donnerstag"] = listOf("5/6")
        }
        4 -> {
            ausgewaehlteTage["Dienstag"] = true
            wocheAuswahl["Dienstag"] = jede
            stundenBlockAuswahl["Dienstag"] = listOf("5/6")

            ausgewaehlteTage["Donnerstag"] = true
            wocheAuswahl["Donnerstag"] = gerade
            stundenBlockAuswahl["Donnerstag"] = listOf("5/6")
        }
        5 -> {
            ausgewaehlteTage["Montag"] = true
            wocheAuswahl["Montag"] = ungerade
            stundenBlockAuswahl["Montag"] = listOf("7/8")

            ausgewaehlteTage["Donnerstag"] = true
            wocheAuswahl["Donnerstag"] = jede
            stundenBlockAuswahl["Donnerstag"] = listOf("1/2")
        }
        6 -> {
            ausgewaehlteTage["Montag"] = true
            wocheAuswahl["Montag"] = jede
            stundenBlockAuswahl["Montag"] = listOf("1/2")

            ausgewaehlteTage["Dienstag"] = true
            wocheAuswahl["Dienstag"] = gerade
            stundenBlockAuswahl["Dienstag"] = listOf("7/8")
        }
        7 -> {
            ausgewaehlteTage["Montag"] = true
            wocheAuswahl["Montag"] = jede
            stundenBlockAuswahl["Montag"] = listOf("5/6")

            ausgewaehlteTage["Dienstag"] = true
            wocheAuswahl["Dienstag"] = ungerade
            stundenBlockAuswahl["Dienstag"] = listOf("7/8")
        }
        8 -> {
            ausgewaehlteTage["Mittwoch"] = true
            wocheAuswahl["Mittwoch"] = jede
            stundenBlockAuswahl["Mittwoch"] = listOf("1/2")

            ausgewaehlteTage["Freitag"] = true
            wocheAuswahl["Freitag"] = gerade
            stundenBlockAuswahl["Freitag"] = listOf("1/2")
        }
        9 -> {
            ausgewaehlteTage["Mittwoch"] = true
            wocheAuswahl["Mittwoch"] = jede
            stundenBlockAuswahl["Mittwoch"] = listOf("5/6")

            ausgewaehlteTage["Freitag"] = true
            wocheAuswahl["Freitag"] = ungerade
            stundenBlockAuswahl["Freitag"] = listOf("1/2")
        }
        10 -> {
            ausgewaehlteTage["Montag"] = true
            wocheAuswahl["Montag"] = gerade
            stundenBlockAuswahl["Montag"] = listOf("7/8")

            ausgewaehlteTage["Freitag"] = true
            wocheAuswahl["Freitag"] = jede
            stundenBlockAuswahl["Freitag"] = listOf("5/6")

            // Weil Raster 1 immer vorher gewählt wird, soll es entfernt werden
            ausgewaehlteTage["Mittwoch"] = false
        }
    }
}


@Composable
fun TagAuswahl(ausgewaehlteTage: MutableMap<String, Boolean>, tag: String) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(
                value = ausgewaehlteTage[tag] == true, //Überprüft, ob der Tag ausgewählt ist
                onValueChange = { checked ->
                    updateTagSelection(ausgewaehlteTage, tag, checked)
                }
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = ausgewaehlteTage[tag] == true,
            onCheckedChange = { checked ->  //wird aufgerufen, bei änderung des Wertes für die Chackbox
                updateTagSelection(ausgewaehlteTage, tag, checked)
            }
        )
        Text(
            text = tag,
            style = MaterialTheme.typography.bodyLarge,
            fontSize = 18.sp,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

private fun updateTagSelection(ausgewaehlteTage: MutableMap<String, Boolean>, tag: String, checked: Boolean) {
    if (checked) {
        ausgewaehlteTage[tag] = true
    } else {
        ausgewaehlteTage.remove(tag)
    }
}

// UI-Komponente für die Auswahl der Tage
@Composable
fun WochenAuswahl(wocheAuswahl: MutableMap<String, String>, tag: String) {
    val wocheOptionen = listOf("Jede Woche", "Gerade Woche", "Ungerade Woche")

    Text(
        text = "Woche auswählen:",
        style = MaterialTheme.typography.bodySmall
    )
    Spacer(modifier = Modifier.height(4.dp))
    wocheOptionen.forEach { option ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .selectable(
                    selected = wocheAuswahl[tag] == option,
                    onClick = { wocheAuswahl[tag] = option }
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = wocheAuswahl[tag] == option,
                onClick = { wocheAuswahl[tag] = option }
            )
            Text(
                text = option,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

// UI-Komponente für die Auswahl der Stundenblöcke
@Composable
fun BlockAuswahl(stundenBlockAuswahl: MutableMap<String, List<String>>, tag: String) {
    // Radiobuttons für Block Auswahl
    Spacer(modifier = Modifier.height(8.dp)) // Abstände
    Text(
        text = "Block auswählen:",
        style = MaterialTheme.typography.bodySmall
    )
    Spacer(modifier = Modifier.height(4.dp))

    // Row für Stundenblock-Auswahl mit Checkbox
    val stundenOptionen = listOf("1/2", "3/4", "5/6", "7/8", "9/10")
    stundenOptionen.forEach { block ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .selectable(
                    selected = stundenBlockAuswahl[tag]?.contains(block) == true,
                    onClick = {
                        val currentSelection = stundenBlockAuswahl[tag]?.toMutableList() ?: mutableListOf()
                        if (currentSelection.contains(block)) {
                            currentSelection.remove(block) // Entfernen, wenn bereits ausgewählt
                        } else {
                            currentSelection.add(block) // Hinzufügen, wenn nicht ausgewählt
                        }
                        stundenBlockAuswahl[tag] = currentSelection // Aktualisieren der Auswahl
                    }
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start // Horizontale Anordnung
        ) {
            Checkbox(
                checked = stundenBlockAuswahl[tag]?.contains(block) == true,
                onCheckedChange = {
                    val currentSelection = stundenBlockAuswahl[tag]?.toMutableList() ?: mutableListOf()
                    if (currentSelection.contains(block)) {
                        currentSelection.remove(block) // Entfernen, wenn bereits ausgewählt
                    } else {
                        currentSelection.add(block) // Hinzufügen, wenn nicht ausgewählt
                    }
                    stundenBlockAuswahl[tag] = currentSelection // Aktualisieren der Auswahl
                }
            )
            Text(
                text = block,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}


// Funktion für untere Buttons
@Composable
fun Buttons(
    oldKurs: Kurs?,
    kursName: String,
    lehrerkuerzel: String,
    raum: String,
    farbAuswahl: Long,
    ausgewaehlteTage: Map<String, Boolean>,
    wocheAuswahl: Map<String, String>,
    stundenBlockAuswahl: Map<String, List<String>>, // Angepasste Datenstruktur
    databaseViewModel: DatabaseViewModel,
    modifier: Modifier = Modifier,
    navController: NavController
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Abbrechen Button
        OutlinedButton(
            modifier = Modifier.weight(1f),
            onClick = { onCancelButtonClicked(navController) }
        ) {
            AutoResizedText("Abbrechen")
        }

        // Hinzufügen Button
        Button(
            modifier = Modifier.weight(1f),
            onClick = { onAddButtonClicked(
                oldKurs = oldKurs,
                kursName = kursName,
                lehrerkuerzel = lehrerkuerzel,
                raum = raum,
                farbAuswahl = farbAuswahl,
                ausgewaehlteTage = ausgewaehlteTage,
                wocheAuswahl = wocheAuswahl,
                stundenBlockAuswahl = stundenBlockAuswahl,
                databaseViewModel = databaseViewModel,
                navController = navController
            ) },
            enabled = checkRequiredData(kursName, ausgewaehlteTage, wocheAuswahl, stundenBlockAuswahl)
        ) {
            AutoResizedText("Hinzufügen")
        }
    }
}


fun checkRequiredData(
    kursName: String,
    ausgewaehlteTage: Map<String, Boolean>,
    wocheAuswahl: Map<String, String>,
    stundenBlockAuswahl: Map<String, List<String>>
): Boolean {

    if (kursName != "") {
        for ((tag, selected) in ausgewaehlteTage) {
            if (selected && (wocheAuswahl[tag] == null || stundenBlockAuswahl[tag] == null)) { // Checkt, dass jeder gewählte Tag einen Block und Woche hat
                return false
            }
        }
        if (ausgewaehlteTage.values.any { it }) { // Checkt ob min. ein Tag true ist
            return true
        }
    }
    return false
}
fun onCancelButtonClicked(navController: NavController) {
    navigateBackToHomescreen(navController)
}

fun onAddButtonClicked(
    oldKurs: Kurs?,
    kursName: String,
    lehrerkuerzel: String,
    raum: String,
    farbAuswahl: Long,
    ausgewaehlteTage: Map<String, Boolean>,
    wocheAuswahl: Map<String, String>,
    stundenBlockAuswahl: Map<String, List<String>>,
    databaseViewModel: DatabaseViewModel,
    navController: NavController
) {


    navigateBackToHomescreen(navController)

    runBlocking {
        val kursListe = databaseViewModel.getNutzerKursListe()
        var kursNameMehrmals = false
        var kursId: Int? = null
        if (oldKurs != null) {
            kursId = oldKurs.id
        } else {
            val kursNameCounts = kursListe.count { it.kursName == kursName }
            if (kursNameCounts > 1) {
                kursNameMehrmals = true
            } else if (kursNameCounts == 1) {
                val ausgewaehlteKurse = kursListe.filter { it.kursName == kursName }
                val profilKurse = databaseViewModel.getKurseIdByProfilId(databaseViewModel.getCurrentProfilId().value)
                if (ausgewaehlteKurse[0].id !in profilKurse) {
                    kursId = ausgewaehlteKurse[0].id
                }
            }
        }

        var lehrerkuerzelNullable: String? = null
        var raumNullable: String? = null
        if (lehrerkuerzel != "") {
            lehrerkuerzelNullable = lehrerkuerzel
        }
        if (raum != "") {
            raumNullable = raum
        }
        val neuerKurs = Kurs(kursName = kursName, lehrerkuerzel = lehrerkuerzelNullable, raum = raumNullable, kursColor = farbAuswahl)
        if (kursId != null) {
            neuerKurs.id = kursId
        }
        ausgewaehlteTage.forEach { (tag) ->
            if (ausgewaehlteTage[tag] == true) {
                val wocheString = wocheAuswahl[tag]
                val blocks = stundenBlockAuswahl[tag] ?: listOf()

                val woche = when (wocheString) {
                    "Jede Woche" -> 0
                    "Ungerade Woche" -> 1
                    else -> 2
                }
                val block = convertToCode(blocks)
                when (tag) {
                    "Montag" -> {
                        neuerKurs.mondayRegularity = woche
                        neuerKurs.mondayBlock = block
                    }
                    "Dienstag" -> {
                        neuerKurs.tuesdayRegularity = woche
                        neuerKurs.tuesdayBlock = block
                    }
                    "Mittwoch" -> {
                        neuerKurs.wednesdayRegularity = woche
                        neuerKurs.wednesdayBlock = block
                    }
                    "Donnerstag" -> {
                        neuerKurs.thursdayRegularity = woche
                        neuerKurs.thursdayBlock = block
                    }
                    "Freitag" -> {
                        neuerKurs.fridayRegularity = woche
                        neuerKurs.fridayBlock = block
                    }
                }
            }
        }

        if (kursNameMehrmals) {
            val ausgewaehlterKurs = kursListe.filter {
                it.kursName == neuerKurs.kursName &&
                it.mondayBlock == neuerKurs.mondayBlock &&
                it.tuesdayBlock == neuerKurs.tuesdayBlock &&
                it.wednesdayBlock == neuerKurs.wednesdayBlock &&
                it.thursdayBlock == neuerKurs.thursdayBlock &&
                it.fridayBlock == neuerKurs.fridayBlock
            }
            if (ausgewaehlterKurs.isNotEmpty()) {
                neuerKurs.id = ausgewaehlterKurs[0].id
            }
        }

        // Ein völlig neuer Kurs
        if (neuerKurs.id == 0) {
            databaseViewModel.insertKurs(neuerKurs)
            val kurseId = databaseViewModel.getIdFromKurse()
            neuerKurs.id = kurseId.last()
        } else if (oldKurs != null) { // Wenn Kurs bearbeitet wird, dann überschreibt man
            databaseViewModel.insertKurs(neuerKurs)
        }

        // Wenn Kurs nicht bearbeitet wird, dann wird er ins Profil gepackt, wenn er neu ist, oder woanders auch schon ist)
        if (oldKurs == null) {
            val profilKursRelation = ProfilKursRelation(
                profilId = databaseViewModel.getCurrentProfilId().value,
                kursId = neuerKurs.id
            )
            databaseViewModel.insertProfilKursRelation(profilKursRelation)
        }
    }
}
fun navigateBackToHomescreen(navController: NavController) {
    val previousBackStackEntry = navController.previousBackStackEntry
    if (previousBackStackEntry != null) {
        val screen = previousBackStackEntry.destination.route
        if (screen == "KursAnsicht/{kursJson}/{hinweisJson}") {
            navController.popBackStack()
        }
    }
    navController.popBackStack()
}
