package com.alfredang.sgtutors

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.alfredang.sgtutors.ui.RootScreen
import com.alfredang.sgtutors.ui.SGTutorsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SGTutorsTheme {
                RootScreen()
            }
        }
    }
}
