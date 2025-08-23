package ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import db.DatabaseManager

class Cashier {
    @Composable
    fun Screen(onBack: () -> Unit) {
        var cardBank by remember { mutableStateOf("") }
        var machineName by remember { mutableStateOf("") }
        var amount by remember { mutableStateOf("") }

        Box(Modifier.fillMaxSize()) {
            Button(onClick = onBack, modifier = Modifier.align(Alignment.TopStart)) {
                Text("Exit")
            }
            Button(
                onClick = { DatabaseManager.createDatabase() },
                modifier = Modifier.align(Alignment.TopStart).padding(start = 16.dp, top = 56.dp)
            ) {
                Text("New Database")
            }

            Column(
                modifier = Modifier.align(Alignment.Center),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(horizontalAlignment = Alignment.Start) {
                    Text("Card Bank")
                    TextField(
                        value = cardBank,
                        onValueChange = { cardBank = it }
                    )
                }
                Column(horizontalAlignment = Alignment.Start) {
                    Text("Machine Name")
                    TextField(
                        value = machineName,
                        onValueChange = { machineName = it }
                    )
                }
                Column(horizontalAlignment = Alignment.Start) {
                    Text("Amount")
                    TextField(
                        value = amount,
                        onValueChange = { amount = it }
                    )
                }
            }
        }
    }
}
