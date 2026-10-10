package dev.quangtrang.callsieve

object RuleMatcher {
    fun compile(pattern: String): Regex? =
        try {
            Regex(pattern)
        } catch (e: Exception) {
            null
        }

    /** Returns the first enabled rule that matches the number, or null if none match. */
    fun firstMatch(number: String, rules: List<Rule>): Rule? =
        rules.firstOrNull { rule ->
            rule.enabled && compile(rule.pattern)?.containsMatchIn(number) == true
        }

    /** A regex that matches even very different numbers would block almost every call. */
    fun isTooBroad(regex: Regex): Boolean =
        listOf("0912345678", "0287654321", "+12025550123", "19001234")
            .all { regex.containsMatchIn(it) }
}
