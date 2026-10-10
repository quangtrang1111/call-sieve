package dev.quangtrang.callsieve

import android.content.Context
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

/** Stores the rule list in SharedPreferences as a small JSON string. */
object RuleStore {
    private const val PREFS = "callsieve_rules"
    private const val KEY = "rules_json"

    fun load(context: Context): List<Rule> {
        val json = prefs(context).getString(KEY, null) ?: return emptyList()
        return try {
            val arr = JSONArray(json)
            List(arr.length()) { i ->
                val o = arr.getJSONObject(i)
                Rule(
                    id = o.getLong("id"),
                    name = o.optString("name"),
                    pattern = o.getString("pattern"),
                    enabled = o.optBoolean("enabled", true),
                )
            }
        } catch (e: JSONException) {
            emptyList()
        }
    }

    fun save(context: Context, rules: List<Rule>) {
        val arr = JSONArray()
        rules.forEach {
            arr.put(
                JSONObject()
                    .put("id", it.id)
                    .put("name", it.name)
                    .put("pattern", it.pattern)
                    .put("enabled", it.enabled)
            )
        }
        prefs(context).edit().putString(KEY, arr.toString()).apply()
    }

    private fun prefs(context: Context) =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
