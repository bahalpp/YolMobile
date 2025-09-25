package com.help.yolmobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.help.yolmobile.screens.MapScreen
import com.help.yolmobile.screens.SplashScreen


public class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = "splashscreen", builder ={

                composable("splashscreen"){
                    SplashScreen(navController)
                }

                composable("mapboxscreen"){
                    MapScreen(navController)
                }

            })
        }
    }
}
