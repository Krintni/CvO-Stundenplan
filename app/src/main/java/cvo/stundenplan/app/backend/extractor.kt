package cvo.stundenplan.app.backend

import android.util.Log
import cvo.stundenplan.app.data.DatabaseViewModel
import cvo.stundenplan.app.data.Day
import cvo.stundenplan.app.data.Hinweis
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup
import org.jsoup.select.Elements

// Webscraper
suspend fun fetchHtmlContent(): List<Elements>? {
    val url = "https://www.cvo-gyo.de/aktuelle-hinweise"
    return withContext(Dispatchers.IO) {
        try {
            // fetch html von website
            val document = Jsoup.connect(url).get()
            val dates =
                document.select(".hinweisheading")
            val hinweistext =
                document.select(".hinweistext.w-richtext")

            // Returnt eine Liste mit beiden Elementen
            var results: List<Elements>? = null
            if (dates.isNotEmpty() || hinweistext.isNotEmpty()) {
                results = listOf(dates, hinweistext)
            }
            results
        } catch (e: Exception) {
            Log.e("JsoupError", "Fehler beim fetches des HTML", e)
            // return null wenn Fetchen unmöglich
            null
        }
    }
}

suspend fun manageDataExtraction(databaseViewModel: DatabaseViewModel) =
    // Startet eine Coroutine
    coroutineScope {
        val content: Deferred<List<Elements>?> = async { fetchHtmlContent() } // Asynchroner Aufruf mit Versprechung auf ein Ergebnis
        val contentAwait = content.await() // Hält das Programm an, bis das Ergebnis geliefert wurde
        if (contentAwait != null) {
            val dates = contentAwait[0]
            val hinweistext = contentAwait[1]

            val extractedDates = getDates(dates)
            val extractedData = manageHinweisExtraction(extractedDates, hinweistext)

            insertDataToDatabase(extractedData, databaseViewModel)
            databaseViewModel.setHinweisExtraktorFehler(false)
            databaseViewModel.onDataExtractionComplete()
        } else {
            Log.e("DataExtractionError", "Unable to extract Data: Empty content")
            databaseViewModel.setHinweisExtraktorFehler(true)
        }

    }
fun getDates(dates: Elements): MutableList<String> {
    val extractedDates = mutableListOf<String>()
    for (date in dates) { // Iteriere durch jedes dates Element
        val dateStr = date.toString() // Wandle das element in String um
        val start = dateStr.indexOf(",") + 2 // Findet die Position des ersten Kommas
        val end = dateStr.indexOf("</h2") - 1 // Die die Endposition
        val extractedDate = dateStr.slice(start..end) // Extrahiert den Teil des Strings zwischen den Positionen
        extractedDates.add(extractedDate)
    }
    return extractedDates
}
fun manageHinweisExtraction(dates: MutableList<String>, hinweistext: Elements): MutableMap<String, MutableMap<String, MutableList<String>>> {
    val extractedData = mutableMapOf<String, MutableMap<String, MutableList<String>>>()
    // Hinweisbox ist jeweils ein Tag auf der Website
    for ((index, hinweisBox) in hinweistext.withIndex()) {
        val date = dates[index]
        val hinweisBoxString = hinweisBox.toString()
        val extractedKurse = extractStrongBlock(hinweisBoxString)
        extractedData[date] = extractedKurse
    }
    return extractedData
}
fun extractStrongBlock(hinweisBox: String): MutableMap<String, MutableList<String>> {
    /* Extrahiert jeden Strong Block, wo jeweils ein Hinweistyp ist*/
    val resultMap = mutableMapOf<String, MutableList<String>>()
    var hinweisBoxProcessed = hinweisBox
    while (hinweisBoxProcessed != "") {
        var start = hinweisBoxProcessed.indexOf("<p>") +3 // Finde die Startposition
        var end = hinweisBoxProcessed.indexOf("</p>") // Finde die Endposition
        val startStrong = hinweisBoxProcessed.indexOf("<strong>") + 8 // Finde die Startposition
        val endStrong = hinweisBoxProcessed.indexOf("</strong>") // Finde die Endposition
        var newEndAddition = 4
        if (startStrong -8 == start && endStrong +9 == end) {
            start = startStrong
            end = endStrong
            newEndAddition = 9
        }

        val extractedStrongBlock = hinweisBoxProcessed.slice(start..<end) // Extrahiert den Inhalt zwischen den Positionen

        // Aktualisiere Variable, sodass die Hinweisbox ab von der Endposition beginnt
        hinweisBoxProcessed = hinweisBoxProcessed.slice(end + newEndAddition..<hinweisBoxProcessed.count())

        if (extractedStrongBlock != "") {
            var hinweisTyp = getHinweisTyp(extractedStrongBlock)

            // Replace: Fix für die falsche Kodierung von > aufgrund von HTML
            var hinweisBlock = extractedStrongBlock.replace("&gt;", ">") // getHinweise(extractedStrongBlock, hinweisTyp)

            if (hinweisTyp != null) {
                hinweisBlock = hinweisBlock.slice(hinweisTyp.length + 2 ..<hinweisBlock.count())
            } else {
                hinweisTyp = "Sonstiges"
            }

            if (resultMap[hinweisTyp] != null) {
                resultMap[hinweisTyp]?.addAll(listOf(hinweisBlock))
            } else {
                resultMap[hinweisTyp] = mutableListOf(hinweisBlock)
            }
        }
    }
    return resultMap
}

fun getHinweisTyp(extractedHinweis: String): String? {
    val hinweisTypen = listOf("Entfall", "Aufgaben", "Aufgabe", "Raumänderungen", "Raumänderung", "Raumverlegungen", "Raumverlegung", "Vertretung", "Hinweis")
    for (hinweisTyp in hinweisTypen) {
        val hinweisTypPosStart = extractedHinweis.indexOf(hinweisTyp)
        // Wenn -1 dann wurde der Hinweistyp nicht gefunden
        if (hinweisTypPosStart == -1) {
            continue
        } else {
            return hinweisTyp
        }
    }
    return null
}

suspend fun insertDataToDatabase(
    data: MutableMap<String, MutableMap<String, MutableList<String>>>,
    databaseViewModel: DatabaseViewModel) {

    val databaseAllHinweis = databaseViewModel.getAllHinweis() // Fetcht alle Einträge aus Tabelle Hinweis
    for (date in data.keys) {
        val disctinctDates = databaseViewModel.getDistinctDates() // Fetcht nur jedes Datum aus der Tabelle Day

        // Fügt Datum hinzu wenn nicht eingetragen
        if (date !in disctinctDates) {
            val day = Day(date)
            databaseViewModel.insertDay(day) // Tag in die DB einfügen
        }

        val dayId = databaseViewModel.getIdByDate(date)
        val hinweisTypMap = data[date] ?: continue // Hinweistyp und zugehörige Kurse
        for (hinweisTyp in hinweisTypMap.keys) {
            val hinweisList = hinweisTypMap[hinweisTyp] ?: continue
            for (hinweis in hinweisList) {
                val kursNameList = getKursName(hinweis, databaseViewModel)
                if (kursNameList.isEmpty()) {
                    insertHinweis(null, hinweis, hinweisTyp, dayId, databaseAllHinweis, databaseViewModel)
                } else {
                    for (kursName in kursNameList) {
                        insertHinweis(kursName, hinweis, hinweisTyp, dayId, databaseAllHinweis, databaseViewModel)
                    }
                }
            }
        }
    }
}

suspend fun getKursName(hinweisMessage: String, databaseViewModel: DatabaseViewModel): MutableList<String> {
    // Aufruf zu Datenbank wo Nutzerkurse gespeichert werden
    val userKurse = databaseViewModel.getNutzerKursListe()
    val foundKurseInMessage = mutableListOf<String>()

    // Hole alle kursNamen und packe in eine Liste
    val userKurseNames = userKurse.map { it.kursName }

    val hinweisMessageLower = hinweisMessage.lowercase()
    for (userKursName in userKurseNames) {
        val userKursNameLower = userKursName.lowercase()
        val regex = Regex("\\b$userKursNameLower\\b")

        if (regex.containsMatchIn(hinweisMessageLower)) {
            foundKurseInMessage.add(userKursName.lowercase())
        }
    }
    return foundKurseInMessage
}

fun insertHinweis(
    kursName: String?,
    hinweis: String,
    hinweisTyp: String,
    dayId: Int,
    databaseAllHinweis: List<Hinweis>,
    databaseViewModel: DatabaseViewModel
) {
    val hinweisObject = Hinweis(
        kurs = kursName,
        hinweis = hinweis,
        hinweisTyp = hinweisTyp,
        dayId = dayId)
    // Checkt ob solch ein Hinweis bereits eingetragen ist und speichert ihn
    val existingHinweisObject = databaseAllHinweis.find {
        it.hinweis == hinweisObject.hinweis && it.dayId == hinweisObject.dayId
    }

    // 1. Wenn Hinweis leer -> ergänze
    // 2. Wenn Hinweis nicht leer -> erstelle eine Kopie


    // Für den Fall, dass ein Hinweis mit Kurs == null eingetragen wurde, aber der Nutzer den Kurs später einträgt
    // Checkt ob es den Hinweis schon gibt, ob dieser einen Kursnamen trägt und ob es einen gefunden Kursnamen gibt
    val hinweisObjectToUpdateId = if (existingHinweisObject != null && existingHinweisObject.kurs == null && hinweisObject.kurs != null) {
        existingHinweisObject.id
    } else {
        null
    }

    // Wenn Hinweis noch nicht existiert, eintragen
    if (hinweisObjectToUpdateId != null) {
        // Selbe Id wie vom existerierenden Hinweis annehmen zum aktualisieren
        hinweisObject.id = hinweisObjectToUpdateId
    }

    // Damit ein Hinweis nicht nochmal geschrieben wird
    val existingHinweisObjectFull = databaseAllHinweis.find {
        it.kurs == hinweisObject.kurs && it.hinweis == hinweisObject.hinweis && it.dayId == hinweisObject.dayId
    }
    if (existingHinweisObjectFull == null) {
        databaseViewModel.insertHinweis(hinweisObject)
    }
}