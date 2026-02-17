package org.cs

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import ui.App

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "cs_ia",
    ) {
        App()
    }
}