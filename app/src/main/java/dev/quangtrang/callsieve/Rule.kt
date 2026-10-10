package dev.quangtrang.callsieve

data class Rule(
    val id: Long,
    val name: String,
    val pattern: String,
    val enabled: Boolean = true,
)
