package com.example.townexplorer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.townexplorer.ui.navigation.NavigationBar
import com.example.townexplorer.ui.theme.TownExplorerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TownExplorerTheme() {
                NavigationBar()
            }

        }
    }

}