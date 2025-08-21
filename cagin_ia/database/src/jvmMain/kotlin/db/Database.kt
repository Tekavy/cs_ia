package db

import org.jetbrains.exposed.sql.Database

object DatabaseFactory {
    fun connect(url: String, driver: String, user: String? = null, password: String? = null) {
        Database.connect(url = url, driver = driver, user = user ?: "", password = password ?: "")
    }
}
