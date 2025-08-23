package db

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.javatime.datetime
import java.io.File

/**
 * Handles selection and initialization of the application's database.
 *
 * The manager searches for local SQLite database files. If none are found a
 * new database is created using the default schema. When a single database is
 * present it is used automatically. If multiple databases exist the caller is
 * expected to ask the user which one should be used and then provide the
 * chosen path to [selectDatabase].
 */
object DatabaseManager {
    private var dbPath: String? = null

    /**
     * Result of [init]. When multiple databases are present the caller should
     * prompt the user to select one of [existing].
     */
    sealed interface InitResult {
        data object Ready : InitResult
        data class NeedUserSelection(val existing: List<String>) : InitResult
    }

    /**
     * Initialises the manager. Creates a new database if none exist, uses the
     * single existing database or returns [InitResult.NeedUserSelection] when
     * there are multiple choices.
     */
    fun init(searchDir: File = File(".")): InitResult {
        val dbFiles = searchDir.listFiles { _, name -> name.endsWith(".db") }?.toList() ?: emptyList()
        return when (dbFiles.size) {
            0 -> {
                val path = File(searchDir, "app.db").absolutePath
                createAndConnect(path)
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

    /**
     * Connects to the database at [path] and ensures all tables exist.
     */
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
        }
    }

    private fun createAndConnect(path: String) {
        connect(path)
    }

    /** Returns the active database file name or an empty string if not set. */
    fun databaseName(): String = dbPath?.let { File(it).name } ?: ""

    // Table definitions ----------------------------------------------------
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

