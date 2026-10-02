package com.example

import android.app.Presentation
import android.content.Context
import android.hardware.display.DisplayManager
import android.os.Bundle
import android.view.Display
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import com.example.ui.navigation.AppNavigation
import com.example.ui.screens.queue.TvQueueDisplayScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.BarberViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: BarberViewModel by viewModels()
    private var presentation: Presentation? = null
    private var displayManager: DisplayManager? = null

    private val displayListener = object : DisplayManager.DisplayListener {
        override fun onDisplayAdded(displayId: Int) {
            checkAndShowPresentation()
        }

        override fun onDisplayRemoved(displayId: Int) {
            presentation?.dismiss()
            presentation = null
        }

        override fun onDisplayChanged(displayId: Int) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        displayManager = getSystemService(Context.DISPLAY_SERVICE) as? DisplayManager
        displayManager?.registerDisplayListener(displayListener, null)
        checkAndShowPresentation()

        setContent {
            val shopConfig by viewModel.shopConfig.collectAsState()

            MyApplicationTheme(
                darkTheme = shopConfig.isDarkMode,
                themeColor = shopConfig.themeColor
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation(viewModel = viewModel)
                }
            }
        }
    }

    private fun checkAndShowPresentation() {
        val dm = displayManager ?: return
        val displays = dm.getDisplays(DisplayManager.DISPLAY_CATEGORY_PRESENTATION)
        if (displays.isNotEmpty()) {
            val targetDisplay = displays[0]
            if (presentation == null || presentation?.display?.displayId != targetDisplay.displayId) {
                presentation?.dismiss()
                presentation = object : Presentation(this, targetDisplay) {
                    override fun onCreate(savedInstanceState: Bundle?) {
                        super.onCreate(savedInstanceState)
                        val composeView = ComposeView(context).apply {
                            setContent {
                                val shopConfig by viewModel.shopConfig.collectAsState()
                                MyApplicationTheme(
                                    darkTheme = true,
                                    themeColor = shopConfig.themeColor
                                ) {
                                    TvQueueDisplayScreen(
                                        viewModel = viewModel,
                                        onBack = { dismiss() }
                                    )
                                }
                            }
                        }
                        setContentView(composeView)
                    }
                }
                try {
                    presentation?.show()
                } catch (_: Exception) {}
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        presentation?.dismiss()
        presentation = null
        displayManager?.unregisterDisplayListener(displayListener)
    }
}
