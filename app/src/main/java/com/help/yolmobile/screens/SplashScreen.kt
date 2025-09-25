package com.help.yolmobile.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavHostController) {
    LaunchedEffect(Unit) {
        delay(2000)
        navController.navigate("mapboxscreen") {
            popUpTo("splashscreen") { inclusive = true }
        }
    }
    Column {
        Text("Splash Screen", modifier = Modifier.fillMaxSize())
    }

}