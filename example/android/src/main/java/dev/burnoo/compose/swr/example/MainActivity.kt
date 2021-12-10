package dev.burnoo.compose.swr.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import dev.burnoo.compose.swr.example.component.IpApp
import dev.burnoo.compose.swr.example.ui.theme.AppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WithKoin {
                AppTheme {
                    Surface(color = MaterialTheme.colors.background) {
                        Column {
                            CounterApp()
                            IpApp()
                        }
                    }
                }
            }
        }
    }
}