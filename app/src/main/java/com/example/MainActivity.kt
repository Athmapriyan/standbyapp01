package com.example

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.lifecycle.ViewModelProvider
import com.example.data.AppDatabase
import com.example.data.StandbyRepository
import com.example.ui.ScreenState
import com.example.ui.StandbyViewModel
import com.example.ui.StandbyViewModelFactory
import com.example.ui.screens.StandbyClockScreen
import com.example.ui.screens.StandbyMainScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private lateinit var viewModel: StandbyViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 1. Initialize Room Database and Repository
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = StandbyRepository(database.standbyDao())

        // 2. Instantiate ViewModel using proprietary factory
        val factory = StandbyViewModelFactory(application, repository)
        viewModel = ViewModelProvider(this, factory)[StandbyViewModel::class.java]

        setContent {
            MyApplicationTheme {
                // Global gesture / interaction listener interceptor
                // Tapping anywhere across the dashboard resets the idle-seconds autostart timers
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {
                            awaitPointerEventScope {
                                while (true) {
                                    awaitPointerEvent()
                                    viewModel.resetTouch()
                                }
                            }
                        }
                ) {
                    val currentScreenState by viewModel.screenState.collectAsState()

                    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            AnimatedContent(
                                targetState = currentScreenState,
                                transitionSpec = {
                                    fadeIn(animationSpec = tweenSpec()).togetherWith(
                                        fadeOut(animationSpec = tweenSpec())
                                    )
                                },
                                label = "screen_navigation_anim"
                            ) { state ->
                                when (state) {
                                    ScreenState.DASHBOARD -> {
                                        StandbyMainScreen(
                                            viewModel = viewModel,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                    ScreenState.AOD_STAGES -> {
                                        StandbyClockScreen(
                                            viewModel = viewModel,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (::viewModel.isInitialized) {
            viewModel.onConfigurationChanged(newConfig)
        }
    }
}

private fun <T> tweenSpec(): androidx.compose.animation.core.TweenSpec<T> {
    return androidx.compose.animation.core.tween(durationMillis = 500)
}
