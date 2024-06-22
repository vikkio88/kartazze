package io.github.vikkio88.kartazze.enums

enum class DbType(val productName: String) {
    POSTGRESQL("PostgreSQL"),
    MYSQL("MySQL"),
    SQLITE("SQLite"),
    ORACLE("Oracle"),
    SQLSERVER("Microsoft SQL Server"),
    MARIADB("MariaDB"),
    H2("H2"),
    DB2("DB2"),
    DERBY("Apache Derby"),
    HSQLDB("HSQL Database Engine"),
    FIREBIRD("Firebird");

    companion object {
        fun fromProductName(productName: String): DbType? {
            return entries.find { it.productName.equals(productName, ignoreCase = true) }
        }
    }
}