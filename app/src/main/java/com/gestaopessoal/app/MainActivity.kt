package com.gestaopessoal.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.gestaopessoal.app.ui.AppNavHost
import com.gestaopessoal.app.ui.theme.GestaoPessoalTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GestaoPessoalTheme {
                AppNavHost()
            }
        }
    }
}
