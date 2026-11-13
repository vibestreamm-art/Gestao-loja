package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.navigation.GestaoAppRoot
import com.example.ui.theme.GestaoStoreTheme
import com.example.viewmodel.StoreViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT)
        )
        val app = application as GestaoStoreApplication
        val factory = StoreViewModelFactory(app.repository)

        setContent {
            GestaoStoreTheme {
                GestaoAppRoot(viewModelFactory = factory)
            }
        }
    }
}
