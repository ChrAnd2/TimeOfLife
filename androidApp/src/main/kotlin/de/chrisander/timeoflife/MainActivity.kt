package de.chrisander.timeoflife

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import de.chrisander.timeoflife.ui.TimeOfLifeApp
import de.chrisander.timeoflife.ui.theme.TimeOfLifeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            TimeOfLifeTheme {
                TimeOfLifeApp()
            }
        }
    }
}
