package cvo.stundenplan.app.screen

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ColorLens
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Style
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import androidx.core.net.toUri
import androidx.navigation.NavController
import cvo.stundenplan.app.BuildConfig
import cvo.stundenplan.app.data.DatabaseViewModel

@Composable
fun Einstellungen(navController: NavController) {
    Scaffold(
        topBar = { EinstellungenTopBar(navController, "Einstellungen") }
    ) { innerpadding ->
        Column(
            Modifier.padding(innerpadding)
        ) {
            SettingsRow({navController.navigate("AppearancePage")}, Icons.Outlined.ColorLens, "Erscheinungsbild")
            SettingsRow({navController.navigate("AboutPage")}, Icons.Outlined.Info, "Über")

        }

    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EinstellungenTopBar(navController: NavController, title: String, onClick: (() -> Unit)? = null) {

    CenterAlignedTopAppBar(
        navigationIcon = {
            IconButton(
                onClick = {
                    if (onClick == null) {
                        navController.popBackStack()
                    } else {
                        onClick()
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Zurück gehen"
                )
            }
        },
        title = {
            Text(title)
        },
    )
}
@Composable
fun SettingsRow(onClick: () -> Unit, imageVector: ImageVector, text: String, textStyle: TextStyle = MaterialTheme.typography.titleLarge) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(20.dp), // Padding inside the clickable area
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = imageVector, contentDescription = null) // Optional icon
        Spacer(modifier = Modifier.width(30.dp))
        AutoResizedText(text = text, style = textStyle)
        Spacer(modifier = Modifier.weight(1f)) // Pushes the text to the left
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun AboutPage(navController: NavController, databaseViewModel: DatabaseViewModel) {
    val firstStart = databaseViewModel.firstStart.value
    val ctx = LocalContext.current
    Scaffold(
        topBar = {
            if (firstStart) {
                EinstellungenTopBar(navController, "Willkommen!") { navController.navigate("Stundenplan") {
                    popUpTo(navController.graph.startDestinationId) { inclusive = true }
                } }
            } else {
                EinstellungenTopBar(navController, "Über")
            }
        },
        bottomBar = {
            if (firstStart) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button (
                        modifier = Modifier
                            .padding(bottom = 34.dp)
                            .weight(1f),
                        onClick = { navController.navigate("Stundenplan") {
                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                        } }
                    ) {
                        AutoResizedText("Alles klar!")
                    }
                }
            }
        }

    ) { innerpadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerpadding)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                val icon = remember {
                    runCatching {
                        ctx.packageManager.getApplicationIcon(ctx.packageName)
                    }.getOrNull()
                }
                if (icon != null) {
                    Image(
                        bitmap = icon.toBitmap().asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.size(96.dp)
                    )
                }

                Spacer(Modifier.height(12.dp))

                Text("CvO Stundenplan", style = MaterialTheme.typography.headlineSmall)
                Text(BuildConfig.VERSION_NAME, style = MaterialTheme.typography.bodySmall)

                Spacer(Modifier.height(20.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = { openUrl(ctx, "https://krintni.github.io/CvO-Stundenplan/index.html") },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Language, // oder Icons.Default.Link, etc.
                                contentDescription = null,
                                modifier = Modifier
                                    .padding(end = 4.dp)
                                    .size(18.dp)
                            )
                            Text("Webseite")
                        }
                        OutlinedButton(
                            onClick = { openUrl(ctx, "https://krintni.github.io/CvO-Stundenplan/contact.html") },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Email, // oder Icons.Default.Link, etc.
                                contentDescription = null,
                                modifier = Modifier
                                    .padding(end = 4.dp)
                                    .size(18.dp)
                            )
                            Text("Kontakt")
                        }
                    }
                    Spacer(modifier = Modifier.padding(top = 10.dp))
                    Text("Mit CvO Stundenplan kannst du deinen Stundenplan mit für dich relevanten Hinweisen einsehen" +
                            " und kannst Hausaufgaben, Notizen oder Klausuren eintragen oder die Stundenpläne deiner Freunde einsehen."
                    )
                    Spacer(modifier = Modifier.padding(top = 10.dp))
                    Text("Der Quellcode der App ist auf GitHub verfügbar.")
                    Spacer(modifier = Modifier.padding(top = 30.dp))

                    Card(
                        modifier = Modifier
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(3.dp, Color(0xFF1976D2)
                        )
                    ) {
                        Column (Modifier.padding(10.dp)) {
                            Text(
                                text = "WICHTIG",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )

                            Text("Bitte beachte, dass die App kein voller Ersatz für die aktuellen Hinweise ist und" +
                                    " siehe regelmäßig bei den aktuellen Hinweisen nach."
                            )
                            Spacer(modifier = Modifier.padding(top = 20.dp))
                            Text("Solltest du auf Fehler stoßen, kannst du sie gerne melden."+
                                    " Bitte aktualisiere die App, wenn eine neue Version als verfügbar gemeldet wird, um die stabilste Version der App zu nutzen."
                            )
                        }
                    }
                }
            }
        }
    }
}

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun AppearancePage(navController: NavController, databaseViewModel: DatabaseViewModel) {
    val checked by databaseViewModel.areCardsFilled.collectAsState()

    Scaffold(
        topBar = { EinstellungenTopBar(navController, "Erscheinungsbild") }
    ) { innerpadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerpadding),
        ) {
            item {
                SwitchRow(
                    onClick = {databaseViewModel.setCardsFilled(!checked)},
                    imageVector = Icons.Outlined.Style,
                    settingName = "Ausgefüllte Karten",
                    checked = checked
                )
            }
        }
    }
}

@Composable
fun SwitchRow(onClick: () -> Unit, imageVector: ImageVector, settingName: String, checked: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                role = Role.Switch,
                onClick = { onClick() }
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Das Icon
            Icon(
                imageVector = imageVector, // Palette ist passender für Erscheinungsbild
                contentDescription = null // Beschreibung ist optional, wenn nur dekorativ
            )

            // Ein fester Abstand zwischen Icon und Text für ein sauberes Aussehen.
            Spacer(modifier = Modifier.width(16.dp))

            // Der Name
            Text(
                text = settingName,
                style = MaterialTheme.typography.bodyLarge
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = null // Die Row behandelt den Klick bereits.
        )
    }
}
private fun openUrl(ctx: Context, url: String) {
    ctx.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
}