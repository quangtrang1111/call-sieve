package dev.quangtrang.callsieve

import android.app.role.RoleManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.mutableStateOf
import dev.quangtrang.callsieve.ui.CallSieveApp
import dev.quangtrang.callsieve.ui.CallSieveTheme

class MainActivity : ComponentActivity() {

    private val roleHeld = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CallSieveTheme {
                CallSieveApp(
                    roleHeld = roleHeld.value,
                    onRoleResult = { refreshRole() },
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refreshRole() // refresh in case the user just toggled it in Settings
    }

    private fun refreshRole() {
        val rm = getSystemService(RoleManager::class.java)
        roleHeld.value = rm?.isRoleHeld(RoleManager.ROLE_CALL_SCREENING) == true
    }
}
