package cvo.stundenplan.app.screen


import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.ImportExport
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import cvo.stundenplan.app.data.DatabaseViewModel
import cvo.stundenplan.app.data.Kurs
import cvo.stundenplan.app.data.Profil
import cvo.stundenplan.app.data.ProfilKursRelation
import kotlinx.coroutines.runBlocking

@Composable
fun ProfilAnsicht(
    databaseViewModel: DatabaseViewModel,
    navController: NavController,
) {

    Scaffold(
        topBar = {
            TopBar(navController)
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                // Inhalte der Spalte zum oben im Bildschirm
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 40.dp)
                ) {
                    LazyColumn {
                        item {
                            Row(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Info,
                                    contentDescription = null, // Beschreibung für Barrierefreiheit
                                    modifier = Modifier
                                        .padding(end = 10.dp)
                                        .size(24.dp) // Größe des Icons
                                )
                                Text(
                                    text = "Tippe auf ein Profil, um es zu bearbeiten."
                                )
                            }
                            Spacer(
                                modifier = Modifier
                                    .height(1.dp)
                                    .fillMaxWidth()
                                    .border(
                                        1.dp,
                                        MaterialTheme.colorScheme.onSurface
                                    )
                            )
                            val profileState = databaseViewModel.profiles.collectAsState()
                            val profile = profileState.value

                            val selectedProfilId = remember { mutableIntStateOf(0) }
                            val selectedProfilBezeichnung = remember { mutableStateOf("") }
                            val showHinzufuegenDialog = remember { mutableStateOf(false) }
                            val showLoeschenDialog = remember { mutableStateOf(false) }
                            ProfilHinzufuegenDialog(databaseViewModel, showHinzufuegenDialog, selectedProfilBezeichnung, selectedProfilId)
                            ProfilLoeschenBestaetigung(databaseViewModel, showLoeschenDialog, selectedProfilId, selectedProfilBezeichnung)


                            if (profile.isNotEmpty()) {
                                profile.forEach { profil ->
                                    ProfilAnsichtButton(
                                        imageVector = Icons.Outlined.PersonOutline,
                                        text = profil.profilBezeichnung,
                                        onClick = {
                                            selectedProfilId.intValue = profil.id
                                            selectedProfilBezeichnung.value = profil.profilBezeichnung
                                            showHinzufuegenDialog.value = true
                                        },
                                        profil = profil,
                                        databaseViewModel = databaseViewModel,
                                        onClickLoeschen = {
                                            showLoeschenDialog.value = true
                                            selectedProfilId.intValue = profil.id
                                            selectedProfilBezeichnung.value = profil.profilBezeichnung
                                        },

                                        )
                                }
                            } else {
                                Text("Es gibt keine Profile")
                            }
                            Spacer(
                                modifier = Modifier
                                    .height(1.dp)
                                    .fillMaxWidth()
                                    .border(
                                        1.dp,
                                        MaterialTheme.colorScheme.onSurface
                                    )
                            )


                            ProfilAnsichtButton(
                                imageVector = Icons.Outlined.ImportExport,
                                text = "Als Standardprofil importieren",
                                onClick = {
                                    databaseViewModel.overwriteDefaultProfil = true
                                    databaseViewModel.callImportIntent()
                                }
                            )
                            ProfilAnsichtButton(
                                imageVector = Icons.Outlined.FileDownload,
                                text = "Neues Profil importieren",
                                onClick = {
                                    databaseViewModel.callImportIntent()
                                }
                            )

                            ProfilAnsichtButton(
                                imageVector = Icons.Outlined.Add,
                                text = "Neues Profil hinzufügen",
                                onClick = {
                                    selectedProfilId.intValue = 0
                                    selectedProfilBezeichnung.value = ""
                                    showHinzufuegenDialog.value = true
                                }
                            )

                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(navController: NavController) {

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
            Text("Profile")
        },
    )
}
@Composable
fun ProfilAnsichtButton(
    imageVector: ImageVector,
    text: String,
    onClick: () -> Unit,
    profil: Profil? = null,
    databaseViewModel: DatabaseViewModel? = null,
    onClickLoeschen: (() -> Unit)? = null

    ) {
    Button(
        onClick = { onClick() },
        shape = MaterialTheme.shapes.extraSmall,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        modifier = Modifier
            .background(Color.Transparent)
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = imageVector,
                contentDescription = null, // Beschreibung für Barrierefreiheit
                modifier = Modifier
                    .padding(end = 10.dp)
                    .size(24.dp) // Größe des Icons
            )
            Text(
                text = text,
            )
            // Box füllt Raum aus, damit der Rest rechts ist
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                // leerer Raum
            }
            if (profil != null) {
                if (profil.id != 1) {
                    IconButton(
                        onClick = { onClickLoeschen!!() },
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.DeleteOutline,
                            contentDescription = "Profil löschen",
                        )
                    }
                }
                IconButton(
                    onClick = {
                        convertProfilToJson(databaseViewModel!!, profil)
                        databaseViewModel.callExportIntent()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Outlined.FileUpload,
                        contentDescription = "Profil exportieren"
                    )
                }
            }

        }
    }
}


@Composable
fun ProfilHinzufuegenDialog(databaseViewModel: DatabaseViewModel, showDialog: MutableState<Boolean>, profilBezeichnung: MutableState<String>, profilId: MutableIntState) {
    if (showDialog.value) {

        Dialog(onDismissRequest = { showDialog.value = false }) {

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
            ) {
                Box(
                    modifier = Modifier
                        .padding(top = 18.dp)
                        .align(Alignment.CenterHorizontally)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.PersonAdd,
                        contentDescription = null,
                        modifier = Modifier.size(38.dp)
                    )
                }

                OutlinedTextField(
                    value = profilBezeichnung.value,
                    singleLine = true,
                    onValueChange = { profilBezeichnung.value = it },
                    label = { Text("Name des Profils") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 12.dp)
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(
                        onClick = { showDialog.value = false },
                        modifier = Modifier.padding(4.dp),
                    ) {
                        Text("Abbrechen")
                    }
                    TextButton(
                        onClick = {
                            showDialog.value = false

                            profilHinzufuegen(databaseViewModel, profilBezeichnung.value, profilId.intValue)
                        },
                        modifier = Modifier.padding(4.dp),
                    ) {
                        Text("Bestätigen")
                    }

                }
            }
        }
    }
}


@Composable
fun ProfilLoeschenBestaetigung(databaseViewModel: DatabaseViewModel, showDialog: MutableState<Boolean>, profilId: MutableIntState, profilBezeichnung: MutableState<String>) {
    if (showDialog.value) {

        Dialog(onDismissRequest = { showDialog.value = false }) {

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
            ) {
                Box(
                    modifier = Modifier
                        .padding(top = 18.dp)
                        .align(Alignment.CenterHorizontally)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.DeleteOutline,
                        contentDescription = null,
                        modifier = Modifier.size(38.dp)
                    )
                }

                Text(
                    text = "Bist du dir sicher, dass du das Profil \"${profilBezeichnung.value}\" löschen möchtest?",
                    modifier = Modifier
                        .padding(12.dp)
                        .wrapContentSize(Alignment.Center),
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.weight(1f))
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(
                        onClick = { showDialog.value = false },
                        modifier = Modifier.padding(4.dp),
                    ) {
                        Text("Abbrechen")
                    }
                    TextButton(
                        onClick = {
                            showDialog.value = false
                            val profil = Profil(profilBezeichnung.value, profilId.intValue)
                            onDeleteProfilButtonClicked(profil, databaseViewModel)
                        },
                        modifier = Modifier.padding(4.dp),
                    ) {
                        Text(text = "Löschen", color =  MaterialTheme.colorScheme.error)
                    }

                }
            }
        }
    }
}

fun convertProfilToJson(databaseViewModel: DatabaseViewModel, profil: Profil) {
    runBlocking {
        val kurse = databaseViewModel.getKurseByProfil(profil.id)
        val gson = GsonBuilder().setPrettyPrinting().create()
        val kurseJson = gson.toJson(kurse)

        databaseViewModel.jsonContent = kurseJson
    }
}

fun convertJsonToProfil(databaseViewModel: DatabaseViewModel) {
    val jsonContent = databaseViewModel.jsonContent
    val gson = Gson()
    val listType = object : TypeToken<List<Kurs>>() {}.type
    val kursListe = gson.fromJson<List<Kurs>>(jsonContent, listType)
    profilImport(databaseViewModel, kursListe)
}
fun profilImport(databaseViewModel: DatabaseViewModel, kursListe: List<Kurs>) {
    runBlocking {
        val profilId: Int
        if (databaseViewModel.overwriteDefaultProfil) {
            profilId = 1
            val profilbezeichnung = databaseViewModel.getProfilBezeichnung(profilId)
            onDeleteProfilButtonClicked(Profil(profilbezeichnung, profilId), databaseViewModel)

            val newProfil = Profil(profilbezeichnung, profilId)
            databaseViewModel.insertProfil(newProfil)
            databaseViewModel.overwriteDefaultProfil = false
        } else {
            databaseViewModel.insertProfil(Profil("Importiertes Profil"))
            profilId = databaseViewModel.getProfiles().last().id
        }
        for (importKurs in kursListe) {
            val existingKurse = databaseViewModel.getNutzerKursListe()
            val kursNameCount = existingKurse.count { it.kursName == importKurs.kursName }

            // Wenn 0 dann gibt es den Kurs noch nicht
            if (kursNameCount == 0) {
                // Erstellt einen DummyKurs, damit man die neueste autogenerierte Id hat
                // Das ist wichtig, da Kurse eventuell gelöscht worden sind und deshalb ein
                // Lücke entstanden ist, was die Nutzung der größten Id + 1 nicht reliable ist.
                val dummyKurs = Kurs(kursName = "DummyKurs", kursColor = 0)
                databaseViewModel.insertKurs(dummyKurs)
                val lastKursId = databaseViewModel.getNutzerKursListe().last().id
                dummyKurs.id = lastKursId
                databaseViewModel.deleteKurs(dummyKurs)

                importKurs.id = lastKursId // Damit eine neue Id erstellt wird
                databaseViewModel.insertKurs(importKurs)
                databaseViewModel.insertProfilKursRelation(ProfilKursRelation(profilId, importKurs.id))
            }
            // Wenn nicht 0, dann gibt es den Kurs schon ein oder mehrmals
            else if (kursNameCount >= 1) {
                // Bestehende Kurse in neue ProfilKursRelation eintragen
                println("Kurs gibt es bereits, aber nicht im Profil")
                val existingKurseName = existingKurse.filter { it.kursName == importKurs.kursName }
                for (existingKursName in existingKurseName) {
                    val kurseIdInProfil = databaseViewModel.getKurseIdByProfilId(profilId)
                    // Verhindert mehrmaliges eintragen in die Relation
                    if (existingKursName.id !in kurseIdInProfil) {
                        databaseViewModel.insertProfilKursRelation(ProfilKursRelation(profilId, existingKursName.id))
                    }
                }
            }
        }
        // Damit die UI sich aktualisert
        databaseViewModel.fetchProfiles()
    }
}

fun profilHinzufuegen(databaseViewModel: DatabaseViewModel, profilbezeichnung: String, id: Int) {
    runBlocking {
        if (id != 0) {
            databaseViewModel.insertProfil(Profil(profilBezeichnung = profilbezeichnung, id = id))
        } else {
            databaseViewModel.insertProfil(Profil(profilBezeichnung = profilbezeichnung))
        }
        databaseViewModel.fetchProfiles()
    }

}

fun onDeleteProfilButtonClicked(profil: Profil, databaseViewModel: DatabaseViewModel) {
    runBlocking {
        databaseViewModel.deleteProfil(profil)
        databaseViewModel.deleteProfilKursRelationByProfilId(profil.id)

        val kurse = databaseViewModel.getNutzerKursListe()
        val kurseIdInProfiles = databaseViewModel.getKurseIdInProfiles()
        for (kurs in kurse) {
            if (kurs.id !in kurseIdInProfiles) {
                databaseViewModel.deleteKurs(kurs)
            }
        }
        databaseViewModel.changeCurrentProfilId(1)

    }
    databaseViewModel.fetchProfiles()
}

