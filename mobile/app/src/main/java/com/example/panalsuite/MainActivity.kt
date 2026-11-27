package com.example.panalsuite

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import com.example.panalsuite.repository.Persistence
import com.example.panalsuite.navigation.AppNavigation
import com.example.panalsuite.ui.theme.PanalSuiteTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Persistence.restoreAll(applicationContext)
        Persistence.startAutoSave(lifecycleScope, applicationContext)
        enableEdgeToEdge()
        setContent {
            PanalSuiteTheme {
                AppNavigation()
            }
        }
    }
}