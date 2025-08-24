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
import java.math.BigDecimal

class Cashier {
    @Composable
    fun Screen(onBack: () -> Unit) {
        var cardBankId by remember { mutableStateOf("") }
        var machineId by remember { mutableStateOf("") }
        var amount by remember { mutableStateOf("") }
        var cardType by remember { mutableStateOf("credit card") }

        Box(Modifier.fillMaxSize()) {
            Button(onClick = onBack, modifier = Modifier.align(Alignment.TopStart).padding(start = 16.dp, top = 16.dp)) {
                Text("Exit")
            }
            Button(
                onClick = { DatabaseManager.createDatabase() },
                modifier = Modifier.align(Alignment.TopStart).padding(start = 16.dp, top = 500.dp)
            ) {
                Text("New Month Database")
            }

            Column(
                modifier = Modifier.align(Alignment.Center),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(horizontalAlignment = Alignment.Start) {
                    Text("Card BankID")
                    TextField(
                        value = cardBankId,
                        onValueChange = { cardBankId = it }
                    )
                }
                Column(horizontalAlignment = Alignment.Start) {
                    Text("Card Type")
                    TextField(
                        value = cardType,
                        onValueChange = { cardType = it }
                    )
                }
                Column(horizontalAlignment = Alignment.Start) {
                    Text("Machine Name")
                    TextField(
                        value = machineId,
                        onValueChange = { machineId = it }
                    )
                }
                Column(horizontalAlignment = Alignment.Start) {
                    Text("Amount")
                    TextField(
                        value = amount,
                        onValueChange = { amount = it }
                    )
                }
                Button(onClick = {
                    val amt = amount.toBigDecimalOrNull() ?: BigDecimal.ZERO
                    val bankId = cardBankId.toIntOrNull() ?: 0
                    val machineIdInt = machineId.toIntOrNull() ?: 0
                    DatabaseManager.addPayment(bankId, machineIdInt, amt, cardType)
                    cardBankId = ""
                    machineId = ""
                    amount = ""
                    cardType = "credit card"
                }) {
                    Text("Add Payment")
                }
            }

        }
    }
}