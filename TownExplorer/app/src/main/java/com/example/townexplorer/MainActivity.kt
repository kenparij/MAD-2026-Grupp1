package com.example.townexplorer

import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.townexplorer.ui.navigation.NavigationBar
import com.example.townexplorer.ui.theme.TownExplorerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Keep status bar icons dark, the app is always light so white icons weren't visible
        //   Source: https://developer.android.com/develop/ui/views/layout/edge-to-edge#change-color
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        )
        setContent {
            TownExplorerTheme() {
                NavigationBar()
            }

        }
    }

}