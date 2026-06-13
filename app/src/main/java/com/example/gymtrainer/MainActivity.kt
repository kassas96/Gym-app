package com.example.gymtrainer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.gymtrainer.ui.BuilderScreen
import com.example.gymtrainer.ui.GuidedPlansScreen
import com.example.gymtrainer.ui.MyPlansScreen
import com.example.gymtrainer.ui.ProfileScreen
import com.example.gymtrainer.ui.ProgressScreen
import com.example.gymtrainer.ui.SessionScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = Color(0xFFFF6B35),     // ember orange - effort
                    secondary = Color(0xFF53D8A4),   // mint - completed / progress
                    surface = Color(0xFF16181D),
                    background = Color(0xFF0E1013)
                )
            ) {
                Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    AppNav()
                }
            }
        }
    }
}

@Composable
fun AppNav() {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = "home") {
        composable("home") { HomeScreen(nav) }
        composable("guided") { GuidedPlansScreen(viewModel(), onSetupProfile = { nav.navigate("nutrition") }) }
        composable("nutrition") { ProfileScreen(viewModel()) }
        composable("builder") { BuilderScreen(viewModel()) { nav.popBackStack() } }
        composable("plans") { MyPlansScreen(viewModel(), onStartSession = { planId ->
            nav.navigate("session/$planId")
        }, onBuildNew = { nav.navigate("builder") }) }
        composable(
            "session/{planId}",
            arguments = listOf(navArgument("planId") { type = NavType.LongType })
        ) { entry ->
            val planId = entry.arguments?.getLong("planId") ?: return@composable
            SessionScreen(viewModel(), planId) { nav.popBackStack() }
        }
        composable("progress") { ProgressScreen(viewModel()) }
    }
}

@Composable
fun HomeScreen(nav: androidx.navigation.NavHostController) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("GymTrainer", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Black)
        Text(
            "Build. Lift. Log. Overload.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Spacer(Modifier.height(28.dp))
        HomeCard("Guided Plans", "Pre-built programs with workouts, daily macros and meals") {
            nav.navigate("guided")
        }
        HomeCard("Nutrition", "Enter your stats for personalized calorie and macro targets") {
            nav.navigate("nutrition")
        }
        HomeCard("My Workouts", "Your custom plans - start a session and log every set") {
            nav.navigate("plans")
        }
        HomeCard("Workout Builder", "Build a plan: muscle group, exercise, target sub-muscle") {
            nav.navigate("builder")
        }
        HomeCard("Progress", "Review weight x reps history per exercise over time") {
            nav.navigate("progress")
        }
    }
}

@Composable
private fun HomeCard(title: String, subtitle: String, onClick: () -> Unit) {
    Card(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable(onClick = onClick)
    ) {
        Column(Modifier.padding(18.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(
                subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
            )
        }
    }
}
