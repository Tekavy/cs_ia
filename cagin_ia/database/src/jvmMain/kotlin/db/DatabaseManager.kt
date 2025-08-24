package db

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.javatime.datetime
import java.io.File
import java.time.LocalDate
import java.time.LocalDateTime
import java.math.BigDecimal

object DatabaseManager {
    private var dbPath: String? = null
    private val posMachineBanks = mutableMapOf<Int, List<Int>>()


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
        refreshPosMachineBanks()
    }

    fun createDatabase(directory: File = File(".")): String {
        val now = LocalDate.now()
        val fileName = "app_${now.year}_${now.monthValue.toString().padStart(2, '0')}.db"
        val path = File(directory, fileName).absolutePath
        connect(path)
        return path
    }

    fun databaseName(): String = dbPath?.let { File(it).name } ?: ""

    data class PaymentRecord(
        val paymentId: Int,
        val bmId: Int,
        val commissionId: Int,
        val day: Int,
        val amount: BigDecimal
    )

    private fun refreshPosMachineBanks() {
        val map = mutableMapOf<Int, MutableList<Int>>()
        transaction {
            BanksMachines.selectAll().forEach { row ->
                val pos = row[BanksMachines.posMachineId]
                val bank = row[BanksMachines.bankId]
                map.getOrPut(pos) { mutableListOf() }.add(bank)
            }
        }
        posMachineBanks.clear()
        posMachineBanks.putAll(map.mapValues { it.value.toList() })
    }

    fun addPayment(cardBankId: Int, machineId: Int, amount: BigDecimal, cardType: String) {
        refreshPosMachineBanks()
        transaction {
            val banks = posMachineBanks[machineId] ?: emptyList()
            val commissionRow: ResultRow
            val bmId: Int
            if (banks.contains(cardBankId)) {
                val bmRow = BanksMachines.select {
                    (BanksMachines.bankId eq cardBankId) and (BanksMachines.posMachineId eq machineId)
                }.single()
                bmId = bmRow[BanksMachines.bmId]
                val isInternal = bmRow[BanksMachines.isPrimary]
                commissionRow = CommissionRates.select {
                    (CommissionRates.bankId eq cardBankId) and
                            (CommissionRates.isInternal eq isInternal) and
                            (CommissionRates.cardType eq cardType)
                }.single()
            } else {
                val primaryRow = BanksMachines.select {
                    (BanksMachines.posMachineId eq machineId) and (BanksMachines.isPrimary eq true)
                }.single()
                bmId = primaryRow[BanksMachines.bmId]
                val primaryBankId = primaryRow[BanksMachines.bankId]
                commissionRow = CommissionRates.select {
                    (CommissionRates.bankId eq primaryBankId) and
                            (CommissionRates.isInternal eq false) and
                            (CommissionRates.cardType eq cardType)
                }.single()
            }
            val commissionId = commissionRow[CommissionRates.commissionId]
            val day = LocalDateTime.now().dayOfMonth
            val timestamp = LocalDateTime.of(0, 1, day, 0, 0)
            Payments.insert {
                it[Payments.bmId] = bmId
                it[Payments.commissionId] = commissionId
                it[Payments.timestamp] = timestamp
                it[Payments.amount] = amount
            }
        }
    }

    fun getPayments(): List<PaymentRecord> = transaction {
        Payments.selectAll().map {
            PaymentRecord(
                it[Payments.paymentId],
                it[Payments.bmId],
                it[Payments.commissionId],
                it[Payments.timestamp].dayOfMonth,
                it[Payments.amount]
            )
        }
    }

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

            CommissionRates.insert {
                it[bankId] = bankAId
                it[isInternal] = true
                it[rate] = BigDecimal("0.01")
                it[cardType] = "credit card"
            }
            CommissionRates.insert {
                it[bankId] = bankAId
                it[isInternal] = false
                it[rate] = BigDecimal("0.04")
                it[cardType] = "credit card"
            }
            CommissionRates.insert {
                it[bankId] = bankBId
                it[isInternal] = true
                it[rate] = BigDecimal("0.02")
                it[cardType] = "credit card"
            }
            CommissionRates.insert {
                it[bankId] = bankBId
                it[isInternal] = false
                it[rate] = BigDecimal("0.06")
                it[cardType] = "credit card"
            }
            CommissionRates.insert {
                it[bankId] = bankCId
                it[isInternal] = true
                it[rate] = BigDecimal("0.03")
                it[cardType] = "credit card"
            }
            CommissionRates.insert {
                it[bankId] = bankCId
                it[isInternal] = false
                it[rate] = BigDecimal("0.07")
                it[cardType] = "credit card"
            }
            CommissionRates.insert {
                it[bankId] = bankDId
                it[isInternal] = true
                it[rate] = BigDecimal("0.05")
                it[cardType] = "credit card"
            }
            CommissionRates.insert {
                it[bankId] = bankDId
                it[isInternal] = false
                it[rate] = BigDecimal("0.08")
                it[cardType] = "credit card"
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
}