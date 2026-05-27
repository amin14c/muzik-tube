package com.amindev.muziktube

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.amindev.muziktube.ui.screens.HomeScreen
import com.amindev.muziktube.ui.theme.MuzikTubeTheme
import com.amindev.muziktube.viewmodel.PlayerViewModel

class MainActivity : ComponentActivity() {
    private val vm: PlayerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MuzikTubeTheme {
                HomeScreen(vm = vm)
            }
        }
    }
}
