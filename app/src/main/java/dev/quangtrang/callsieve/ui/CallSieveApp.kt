package dev.quangtrang.callsieve.ui

import android.app.role.RoleManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import dev.quangtrang.callsieve.PhoneNumber
import dev.quangtrang.callsieve.Rule
import dev.quangtrang.callsieve.RuleMatcher
import dev.quangtrang.callsieve.RuleStore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallSieveApp(roleHeld: Boolean, onRoleResult: () -> Unit) {
    val context = LocalContext.current
    val rules = remember { mutableStateListOf<Rule>().apply { addAll(RuleStore.load(context)) } }
    fun persist() = RuleStore.save(context, rules.toList())

    var dialogOpen by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<Rule?>(null) }

    val roleLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { onRoleResult() }

    Scaffold(
        topBar = { TopAppBar(title = { Text("CallSieve") }) },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { editing = null; dialogOpen = true },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Add rule") },
            )
        },
    ) { inner ->
        LazyColumn(
            contentPadding = PaddingValues(
                top = inner.calculateTopPadding() + 8.dp,
                bottom = inner.calculateBottomPadding() + 96.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            item {
                StatusCard(
                    roleHeld = roleHeld,
                    activeRules = rules.count { it.enabled },
                    onEnable = {
                        val rm = context.getSystemService(RoleManager::class.java)
                        if (rm != null && rm.isRoleAvailable(RoleManager.ROLE_CALL_SCREENING)) {
                            roleLauncher.launch(rm.createRequestRoleIntent(RoleManager.ROLE_CALL_SCREENING))
                        }
                    },
                )
            }
            item { NumberTester(rules) }
            item { HorizontalDivider(Modifier.padding(top = 8.dp)) }

            if (rules.isEmpty()) {
                item {
                    Text(
                        "No rules yet. Tap Add rule, e.g. ^1900 to block every number starting with 1900.",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }

            items(rules, key = { it.id }) { rule ->
                RuleRow(
                    rule = rule,
                    onToggle = { on ->
                        val i = rules.indexOfFirst { it.id == rule.id }
                        if (i >= 0) { rules[i] = rule.copy(enabled = on); persist() }
                    },
                    onClick = { editing = rule; dialogOpen = true },
                )
            }
        }
    }

    if (dialogOpen) {
        val current = editing
        RuleDialog(
            initial = current,
            onDismiss = { dialogOpen = false },
            onSave = { name, pattern ->
                if (current == null) {
                    rules.add(Rule(id = System.currentTimeMillis(), name = name, pattern = pattern))
                } else {
                    val i = rules.indexOfFirst { it.id == current.id }
                    if (i >= 0) rules[i] = current.copy(name = name, pattern = pattern)
                }
                persist()
                dialogOpen = false
            },
            onDelete = current?.let { r ->
                {
                    rules.removeAll { it.id == r.id }
                    persist()
                    dialogOpen = false
                }
            },
        )
    }
}

@Composable
private fun StatusCard(roleHeld: Boolean, activeRules: Int, onEnable: () -> Unit) {
    val colors = if (roleHeld) {
        CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    } else {
        CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    }
    Card(colors = colors, modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (roleHeld) {
                Text("Screening calls", style = MaterialTheme.typography.titleMedium)
                Text(
                    "$activeRules ${if (activeRules == 1) "rule" else "rules"} enabled. Matching calls are rejected silently and still recorded in the call log.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            } else {
                Text("CallSieve isn't allowed to screen calls yet", style = MaterialTheme.typography.titleMedium)
                Text(
                    "Grant permission once so Android sends calls from unknown numbers through CallSieve before ringing.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Button(onClick = onEnable) { Text("Allow call screening") }
            }
        }
    }
}

@Composable
private fun NumberTester(rules: List<Rule>) {
    var input by remember { mutableStateOf("") }
    Column(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        OutlinedTextField(
            value = input,
            onValueChange = { input = it },
            label = { Text("Test a phone number") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth(),
        )
        if (input.isNotBlank()) {
            val normalized = PhoneNumber.normalize(input)
            val hit = RuleMatcher.firstMatch(normalized, rules)
            Text(
                if (hit != null) "Would be blocked by rule: ${hit.name.ifBlank { hit.pattern }}"
                else "Not blocked",
                style = MaterialTheme.typography.bodyMedium,
                color = if (hit != null) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.onSurface,
            )
            Text(
                "Normalized number: $normalized",
                style = MaterialTheme.typography.bodySmall,
            )
        }
    }
}

@Composable
private fun RuleRow(rule: Rule, onToggle: (Boolean) -> Unit, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(rule.name.ifBlank { "Unnamed rule" }) },
        supportingContent = { Text(rule.pattern, fontFamily = FontFamily.Monospace) },
        trailingContent = { Switch(checked = rule.enabled, onCheckedChange = onToggle) },
        modifier = Modifier.clickable(onClick = onClick),
    )
}

@Composable
private fun RuleDialog(
    initial: Rule?,
    onDismiss: () -> Unit,
    onSave: (name: String, pattern: String) -> Unit,
    onDelete: (() -> Unit)?,
) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var pattern by remember { mutableStateOf(initial?.pattern ?: "") }
    val regex = remember(pattern) { if (pattern.isBlank()) null else RuleMatcher.compile(pattern) }
    val invalid = pattern.isNotBlank() && regex == null
    val tooBroad = regex != null && RuleMatcher.isTooBroad(regex)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Add rule" else "Edit rule") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Rule name (optional)") },
                    singleLine = true,
                )
                OutlinedTextField(
                    value = pattern,
                    onValueChange = { pattern = it },
                    label = { Text("Regex") },
                    singleLine = true,
                    isError = invalid,
                    textStyle = LocalTextStyle.current.copy(fontFamily = FontFamily.Monospace),
                    supportingText = {
                        when {
                            invalid -> Text("Invalid regex. Check brackets and special characters.")
                            tooBroad -> Text("This rule would block almost every call.")
                            else -> Text("Example: ^1900 or ^02[48]")
                        }
                    },
                )
                Text(
                    "Numbers are converted to 0xxxxxxxxx before matching. Hidden numbers are an empty string; use ^\$ to block them.",
                    style = MaterialTheme.typography.bodySmall,
                )
                if (onDelete != null) {
                    TextButton(onClick = onDelete) {
                        Text("Delete rule", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(name.trim(), pattern) }, enabled = regex != null) {
                Text("Save")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}
