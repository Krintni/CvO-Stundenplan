package cvo.stundenplan.app.screen

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.google.gson.Gson
import cvo.stundenplan.app.data.DatabaseViewModel
import cvo.stundenplan.app.data.Hinweis
import cvo.stundenplan.app.data.Kurs
import java.net.URLDecoder
import java.nio.charset.StandardCharsets


@SuppressLint("UnusedContentLambdaTargetStateParameter")
@Composable
// Diese Funktion definiert die Navigation zwischen verschiedenen Ansichten (Screens) in der App.
// Sie verwendet einen NavHost, um die Navigation zu steuern und die verschiedenen Routen zu verwalten.
fun AppNavigation(
    databaseViewModel: DatabaseViewModel,
    navController: NavHostController,
    dateStateHolder: DateStateHolder,
) {
    // Definiert den NavHost mit dem Startbildschirm "Stundenplan".
    NavHost(navController = navController,
        startDestination = if (databaseViewModel.firstStart.value) "AboutPage" else "Stundenplan",
        enterTransition = { EnterTransition.None },  // Keine Übergangseffekte beim Betreten des Screens
        exitTransition = { ExitTransition.None }  // Keine Übergangseffekte beim Verlassen des Screens
    ) {
        val durationMillis = 300 // Dauer der Übergangseffekte in Millisekunden

        // Definiert die Route für den Stundenplan-Screen
        composable("Stundenplan") {
            HomeScreen(databaseViewModel, navController, dateStateHolder) // Aufruf der Funktion für den Stundenplan-Screen
        }
        // Definiert die Route für das Hinzufügen eines Kurses
        composable("KursHinzufuegen/{kursJson}",
            // Übergangseffekt beim Betreten des Screens: von rechts nach links hereinschieben
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    tween(durationMillis)
                )
            },
            // Übergangseffekt beim Verlassen des Bildschirms: nach links rausschieben
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    tween(durationMillis)
                )
            }
        ) { backStackEntry ->
            // Hole die kodierten JSON-Strings aus den Argumenten
            val kursJson = backStackEntry.arguments?.getString("kursJson")
            // Dekodieren der JSON-Strings
            val decodedKursJson = URLDecoder.decode(kursJson, StandardCharsets.UTF_8.toString())
            // Deserialisieren der JSON-Strings ins Kurs Objekt
            val kurs = decodedKursJson?.let { Gson().fromJson(it, Kurs::class.java) }

            // Aufruf der Funktion für den KursHinzufügen-Screen
            KursHinzufuegen(
                databaseViewModel = databaseViewModel,
                navController = navController,
                kurs = kurs,
            )
        }
        // Definiert die Route für die Kursansicht
        composable("KursAnsicht/{kursJson}/{hinweisJson}/{date}",
            // Übergangseffekt beim Betreten des Screens: von rechts nach links hereinschieben
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    tween(durationMillis)
                )
            },
            // Übergangseffekt beim Verlassen des Bildschirms: nach links rausschieben
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    tween(durationMillis)
                )
            }
        ) { backStackEntry ->
            // Hole die kodierten JSON-Strings aus den Argumenten
            val kursJson = backStackEntry.arguments?.getString("kursJson")
            val hinweisJson = backStackEntry.arguments?.getString("hinweisJson")
            val date = backStackEntry.arguments?.getString("date")


            // Dekodieren der JSON-Strings
            val decodedKursJson = URLDecoder.decode(kursJson, StandardCharsets.UTF_8.toString())
            val decodedHinweisJson = URLDecoder.decode(hinweisJson, StandardCharsets.UTF_8.toString())

            // Deserialisieren der JSON-Strings in die Objekte
            val kurs = decodedKursJson?.let { Gson().fromJson(it, Kurs::class.java) }
            val hinweis = decodedHinweisJson?.let { Gson().fromJson(it, Hinweis::class.java) }

            if (kurs != null ) {
                // Aufruf der Funktion zum KursAnsicht-Screen
                KursAnsicht(
                    navController = navController,
                    databaseViewModel = databaseViewModel,
                    kurs = kurs,
                    hinweis = hinweis,
                    date = date!!
                )
            }
        }
        composable("ProfilAnsicht",
            // Übergangseffekt beim Betreten des Screens: von rechts nach links hereinschieben
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    tween(durationMillis)
                )
            },
            // Übergangseffekt beim Verlassen des Bildschirms: nach links rausschieben
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    tween(durationMillis)
                )
            }
        ) {
            ProfilAnsicht(
                databaseViewModel,
                navController,)

        }
        composable("Einstellungen",
            // Übergangseffekt beim Betreten des Screens: von rechts nach links hereinschieben
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    tween(durationMillis)
                )
            },
            // Übergangseffekt beim Verlassen des Bildschirms: nach links rausschieben
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    tween(durationMillis)
                )
            }
        ) {
            Einstellungen(
                navController,
            )
        }
        composable("AboutPage",
            // Übergangseffekt beim Betreten des Screens: von rechts nach links hereinschieben
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    tween(durationMillis)
                )
            },
            // Übergangseffekt beim Verlassen des Bildschirms: nach links rausschieben
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    tween(durationMillis)
                )
            }
        ) {
            AboutPage(
                navController,
                databaseViewModel
            )
        }
        composable("AppearancePage",
            // Übergangseffekt beim Betreten des Screens: von rechts nach links hereinschieben
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    tween(durationMillis)
                )
            },
            // Übergangseffekt beim Verlassen des Bildschirms: nach links rausschieben
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    tween(durationMillis)
                )
            }
        ) {
            AppearancePage(
                navController,
                databaseViewModel
            )
        }
    }
}