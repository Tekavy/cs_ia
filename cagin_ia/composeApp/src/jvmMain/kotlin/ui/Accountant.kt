package ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import db.DatabaseManager

class Accountant {
    @Composable
    fun Screen(onBack: () -> Unit) {
        var payments by remember { mutableStateOf(DatabaseManager.getPayments()) }
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Accountant interface")
            Text("Database: ${DatabaseManager.databaseName()}")
            payments.forEach { p ->
                Text("Payment ${'$'}{p.paymentId}: bm ${'$'}{p.bmId}, commissionId ${'$'}{p.commissionId}, day ${'$'}{p.day}, amount ${'$'}{p.amount}")
            }
            Button(onClick = { payments = DatabaseManager.getPayments() }) {
                Text("Refresh")
            }
            Button(onClick = onBack) {
                Text("Back")
            }
        }
    }
}