package com.line98.game

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.line98.game.ui.Line98App
import com.line98.game.ui.theme.Line98Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Line98Theme {
                Line98App()
            }
        }
    }
}
