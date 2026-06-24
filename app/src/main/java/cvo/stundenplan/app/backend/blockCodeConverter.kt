package cvo.stundenplan.app.backend

// Konvertiert die Codes für Blöcke aus der Datenbank.
fun convertFromBlocksCodeToBlocksId(block: Int, stundenplan: Boolean = false): List<Int> {
    val zahlen = listOf(3, 4, 5, 6, 7) // Mögliche Zahlen
    val gesuchtesErgebnis = block // Das Ergebnis, das du überprüfen möchtest

    // Finde alle Kombinationen, die das gesuchte Ergebnis ergeben
    for (r in 2..zahlen.size) { // Kombinationslängen von 2 bis zur Größe der Liste
        val kombinationen = generateCombinations(zahlen, r)
        for (kombination in kombinationen) {
            val produkt = kombination.reduce { acc, zahl -> acc * zahl }
            if (produkt == gesuchtesErgebnis) {
                val blocksId = kombination.map { it - 3 }
                return blocksId

            }
        }
    }
    return listOf()
}

// Funktion zur Generierung von Kombinationen
fun <T> generateCombinations(list: List<T>, r: Int): List<List<T>> {
    if (r == 0) return listOf(emptyList())
    if (list.isEmpty()) return emptyList()

    val combinations = mutableListOf<List<T>>()
    for (i in list.indices) {
        val element = list[i]
        val remaining = list.subList(i + 1, list.size)
        for (combination in generateCombinations(remaining, r - 1)) {
            combinations.add(listOf(element) + combination)
        }
    }
    return combinations
}

// Konvertiert die Ergebnisse von convertFromBlocksCodeToBlocksId zum String, um als Blöcke zu speichern.
fun convertToBlockString(blocksId: List<Int>): List<String> {
    val stringListe = blocksId.map { it.toString() }.toMutableList()
    val updatedList = stringListe.map {
        when (it) {
            "0" -> "1/2"
            "1" -> "3/4"
            "2" -> "5/6"
            "3" -> "7/8"
            "4" -> "9/10"
            else -> it // Behalte den ursprünglichen Wert, wenn keine Übereinstimmung vorliegt
        }
    }
    return updatedList
}

// FUnktion zum Convertieren zum Code
fun convertToCode(blockList: List<String>): Int {
    var code = 1

    if (blockList.size > 1) {
        val blockListInt = blockList.map {
            when (it) {
                "1/2" -> "3"
                "3/4" -> "4"
                "5/6" -> "5"
                "7/8" -> "6"
                "9/10" -> "7"
                else -> it // Behalte den ursprünglichen Wert, wenn keine Übereinstimmung vorliegt
            }
        }.mapNotNull {
            it.toIntOrNull() // Konvertiere zu Int, ignoriere ungültige Werte
        }

        for (blockNumber in blockListInt) {

            code *= blockNumber
        }
    } else {
        val blockListInt = blockList.map {
            when (it) {
                "1/2" -> "0"
                "3/4" -> "1"
                "5/6" -> "2"
                "7/8" -> "3"
                "9/10" -> "4"
                else -> it // Behalte den ursprünglichen Wert, wenn keine Übereinstimmung vorliegt
            }
        }.mapNotNull {
            it.toIntOrNull() // Konvertiere zu Int, ignoriere ungültige Werte
        }

        for (blockNumber in blockListInt) {
             code = blockNumber
        }
    }
    return code
}