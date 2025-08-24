package db

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.javatime.datetime
import java.io.File
import java.time.LocalDate

object DatabaseManager {
    private var dbPath: String? = null


    sealed interface InitResult {
        data object Ready : InitResult
        data class NeedUserSelection(val existing: List<String>) : InitResult
    }

    fun init(searchDir: File = File(".")): InitResult {
        val dbFiles = searchDir.listFiles { _, name -> name.endsWith(".db") }?.toList() ?: emptyList()
        return when (dbFiles.size) {
            0 -> {
                createDatabase(searchDir)
                InitResult.Ready
            }
            1 -> {
                connect(dbFiles[0].absolutePath)
                InitResult.Ready
            }
            else -> {
                InitResult.NeedUserSelection(dbFiles.map { it.absolutePath })
            }
        }
    }

    fun selectDatabase(path: String) {
        connect(path)
    }

    private fun connect(path: String) {
        dbPath = path
        Database.connect("jdbc:sqlite:$path", driver = "org.sqlite.JDBC")
        transaction {
            SchemaUtils.createMissingTablesAndColumns(
                Banks,
                PosMachines,
                CommissionRates,
                BanksMachines,
                Payments
            )
            adjustInitialData()
        }
    }

    fun createDatabase(directory: File = File(".")): String {
        val now = LocalDate.now()
        val fileName = "app_${now.year}_${now.monthValue.toString().padStart(2, '0')}.db"
        val path = File(directory, fileName).absolutePath
        connect(path)
        return path
    }

    fun databaseName(): String = dbPath?.let { File(it).name } ?: ""

    private fun adjustInitialData() {
        val banksEmpty = Banks.selectAll().empty()
        val posEmpty = PosMachines.selectAll().empty()
        if (banksEmpty && posEmpty) {
            val bankAId = Banks.insert { it[bankName] = "bankA" }[Banks.bankId]
            val bankBId = Banks.insert { it[bankName] = "bankB" }[Banks.bankId]
            val bankCId = Banks.insert { it[bankName] = "bankC" }[Banks.bankId]
            val bankDId = Banks.insert { it[bankName] = "bankD" }[Banks.bankId]

            val posAId = PosMachines.insert { it[location] = "posmachineA" }[PosMachines.posMachineId]
            val posBId = PosMachines.insert { it[location] = "posmachineB" }[PosMachines.posMachineId]

            BanksMachines.insert {
                it[bankId] = bankAId
                it[posMachineId] = posAId
                it[isPrimary] = true
            }
            listOf(bankCId, bankDId).forEach { bankIdValue ->
                BanksMachines.insert {
                    it[bankId] = bankIdValue
                    it[posMachineId] = posAId
                    it[isPrimary] = false
                }
            }
            BanksMachines.insert {
                it[bankId] = bankBId
                it[posMachineId] = posBId
                it[isPrimary] = true
            }
        }
    }

    //  ------------------------------------------------------------------------------
    object Banks : Table("banks") {
        val bankId = integer("bank_id").autoIncrement()
        val bankName = text("bank_name")
        override val primaryKey = PrimaryKey(bankId)
    }

    object PosMachines : Table("pos_machines") {
        val posMachineId = integer("pos_machine_id").autoIncrement()
        val location = text("location")
        override val primaryKey = PrimaryKey(posMachineId)
    }

    object CommissionRates : Table("commission_rates") {
        val commissionId = integer("commission_id").autoIncrement()
        val bankId = reference("bank_id", Banks.bankId)
        val isInternal = bool("is_internal")
        val rate = decimal("rate", precision = 10, scale = 2)
        val cardType = text("card_type")
        override val primaryKey = PrimaryKey(commissionId)
    }

    object BanksMachines : Table("banks_machines") {
        val bmId = integer("bm_id").autoIncrement()
        val bankId = reference("bank_id", Banks.bankId)
        val posMachineId = reference("pos_machine_id", PosMachines.posMachineId)
        val isPrimary = bool("is_primary")
        override val primaryKey = PrimaryKey(bmId)
    }

    object Payments : Table("payments") {
        val paymentId = integer("payment_id").autoIncrement()
        val bmId = reference("bm_id", BanksMachines.bmId)
        val commissionId = reference("commission_id", CommissionRates.commissionId)
        val timestamp = datetime("timestamp")
        val amount = decimal("amount", precision = 10, scale = 2)
        override val primaryKey = PrimaryKey(paymentId)
    }
}