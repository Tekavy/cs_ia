package ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import ui.theme.AppTheme

@Composable
@Preview
fun App() {
    AppTheme(
        darkTheme = true,
    ) {
        Surface(
            Modifier.fillMaxSize()
        ) {
            AppContent()
        }
    }
}

private enum class Screen { Main, Cashier, Accountant }

@Composable
private fun AppContent() {
    var screen by remember { mutableStateOf(Screen.Main) }
    when (screen) {
        Screen.Main -> MainMenu(
            onCashier = { screen = Screen.Cashier },
            onAccountant = { screen = Screen.Accountant }
        )
        Screen.Cashier -> Cashier().Screen { screen = Screen.Main }
        Screen.Accountant -> Accountant().Screen { screen = Screen.Main }
    }
}

@Composable
private fun MainMenu(onCashier: () -> Unit, onAccountant: () -> Unit) {
    Row(
        Modifier.fillMaxSize().padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Button(onClick = onCashier) { Text("Cashier") }
        Button(onClick = onAccountant) { Text("Accountant") }
    }
}
