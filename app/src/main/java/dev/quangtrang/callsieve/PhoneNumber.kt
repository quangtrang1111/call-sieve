package dev.quangtrang.callsieve

object PhoneNumber {
    /**
     * Normalizes a number before regex matching:
     * - Strips spaces, dashes, parentheses...
     * - +84xxxxxxxxx or 84xxxxxxxxx -> 0xxxxxxxxx
     * - Foreign numbers keep the form +<country code><number>
     * - Hidden / unknown numbers -> empty string
     */
    fun normalize(raw: String?): String {
        if (raw.isNullOrBlank()) return ""
        val hasPlus = raw.trim().startsWith("+")
        val digits = raw.filter { it.isDigit() }
        if (digits.isEmpty()) return ""
        return when {
            hasPlus && digits.startsWith("84") -> "0" + digits.substring(2)
            hasPlus -> "+$digits"
            digits.startsWith("84") && digits.length >= 11 -> "0" + digits.substring(2)
            else -> digits
        }
    }
}
