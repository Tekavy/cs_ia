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
import db.DatabaseManager
import db.DatabaseManager.InitResult
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

private enum class Screen { Loading, DbSelect, Main, Cashier, Accountant }

@Composable
private fun AppContent() {
    var screen by remember { mutableStateOf(Screen.Loading) }
    var existingDbs by remember { mutableStateOf<List<String>>(emptyList()) }

    LaunchedEffect(Unit) {
        when (val result = DatabaseManager.init()) {
            InitResult.Ready -> screen = Screen.Main
            is InitResult.NeedUserSelection -> {
                existingDbs = result.existing
                screen = Screen.DbSelect
            }
        }
    }

    when (screen) {
        Screen.Loading -> Text("Initializing database...")
        Screen.DbSelect -> DatabaseSelector(existingDbs) {
            DatabaseManager.selectDatabase(it)
            screen = Screen.Main
        }
        Screen.Main -> MainMenu(
            onCashier = { screen = Screen.Cashier },
            onAccountant = { screen = Screen.Accountant }
        )
        Screen.Cashier -> Cashier().Screen { screen = Screen.Main }
        Screen.Accountant -> Accountant().Screen { screen = Screen.Main }
    }
}
@Composable
private fun DatabaseSelector(options: List<String>, onSelect: (String) -> Unit) {
    var path by remember { mutableStateOf("") }
    Column(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Multiple databases found. Enter path to use:")
        options.forEach { Text(it) }
        Spacer(Modifier.size(8.dp))
        androidx.compose.material3.TextField(value = path, onValueChange = { path = it })
        Spacer(Modifier.size(8.dp))
        Button(onClick = { onSelect(path) }) { Text("Use Database") }
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