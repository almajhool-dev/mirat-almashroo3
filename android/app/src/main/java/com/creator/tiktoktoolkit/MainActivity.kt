package com.creator.tiktoktoolkit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.creator.tiktoktoolkit.ui.AppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                var adminOpen by remember { mutableStateOf(false) }
                val repository = remember { LocalAdminRepository() }

                if (adminOpen) {
                    AdminControlPanel(repository = repository, onClose = { adminOpen = false })
                } else {
                    Box {
                        StableAccountAnalyzer()
                        FloatingActionButton(
                            onClick = { adminOpen = true },
                            modifier = Modifier.align(Alignment.BottomEnd).padding(18.dp)
                        ) { Text("إدارة") }
                    }
                }
            }
        }
    }
}

private class LocalAdminRepository : AdminRepository {
    private var enabled = true
    private val managedUsers = mutableListOf<ManagedUser>()

    override suspend fun isAppEnabled(): Boolean = enabled
    override suspend fun setAppEnabled(enabled: Boolean) { this.enabled = enabled }
    override suspend fun users(): List<ManagedUser> = managedUsers.toList()
    override suspend fun setBlocked(userId: String, blocked: Boolean) {
        val index = managedUsers.indexOfFirst { it.id == userId }
        if (index >= 0) managedUsers[index] = managedUsers[index].copy(blocked = blocked)
    }
}
