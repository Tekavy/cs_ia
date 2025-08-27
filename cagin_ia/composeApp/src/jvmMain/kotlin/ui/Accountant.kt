package ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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

class Accountant {
    @Composable
    fun Screen(onBack: () -> Unit) {
        var bankName by remember { mutableStateOf("") }
        var sum: Double by remember { mutableStateOf(0.0) }
        var machineName by remember { mutableStateOf("") }
        var internalText by remember { mutableStateOf("") }
        var dayFrom by remember { mutableStateOf("") }
        var dayTo by remember { mutableStateOf("") }
        var payments by remember { mutableStateOf(DatabaseManager.queryPayments()) }

        Box(Modifier.fillMaxSize()) {
            Button(
                onClick = onBack,
                modifier = Modifier.align(Alignment.TopStart).padding(start = 16.dp, top = 16.dp)
            ) {
                Text("Back")
            }
            Row(
                modifier = Modifier.align(Alignment.Center),
                horizontalArrangement = Arrangement.spacedBy(60.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Column(horizontalAlignment = Alignment.Start) {
                        Text("Bank Name")
                        TextField(value = bankName, onValueChange = { bankName = it })
                    }
                    Column(horizontalAlignment = Alignment.Start) {
                        Text("POS Machine Name")
                        TextField(value = machineName, onValueChange = { machineName = it })
                    }
                    Column(horizontalAlignment = Alignment.Start) {
                        Text("Internal (true/false)")
                        TextField(value = internalText, onValueChange = { internalText = it })
                    }
                    Column(horizontalAlignment = Alignment.Start) {
                        Text("Day From")
                        TextField(value = dayFrom, onValueChange = { dayFrom = it })
                    }
                    Column(horizontalAlignment = Alignment.Start) {
                        Text("Day To")
                        TextField(value = dayTo, onValueChange = { dayTo = it })
                    }
                    Button(onClick = {
                        val internal = when (internalText.trim().lowercase()) {
                            "true", "internal" -> true
                            "false", "external" -> false
                            else -> null
                        }
                        val from = dayFrom.toIntOrNull()
                        val to = dayTo.toIntOrNull()
                        payments = DatabaseManager.queryPayments(
                            bankName.ifBlank { null },
                            machineName.ifBlank { null },
                            internal,
                            from,
                            to
                        )
                        sum = DatabaseManager.calcsum(payments);
                    }) {
                        Text("Filter")
                    }
                }
                Column {
                    Text("Database: ${DatabaseManager.getdatabaseName()}")
                    payments.forEach { p ->
                        Text("${p.paymentId}: bank ${p.bankName}, pos ${p.posMachineName}, rate ${p.commissionrate}, day ${p.day}, amount ${p.amount}")
                    }
                    Spacer(modifier = Modifier.size(30.dp))
                    Text("Sum: ${sum}")
                }
            }
        }
    }
}