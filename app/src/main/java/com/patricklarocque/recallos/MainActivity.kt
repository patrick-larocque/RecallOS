package com.patricklarocque.recallos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.patricklarocque.recallos.core.designsystem.theme.RecallOsTheme
import com.patricklarocque.recallos.navigation.RecallOsNavHost
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RecallOsTheme {
                RecallOsNavHost()
            }
        }
    }
}
