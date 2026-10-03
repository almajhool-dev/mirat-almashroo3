package com.creator.tiktoktoolkit

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Admin control UI foundation.
 * The repository does not currently contain a configured remote backend,
 * so this screen deliberately does not pretend to control remote installs.
 * Connect AdminRepository to an authenticated backend before exposing it in production.
 */
data class ManagedUser(
    val id: String,
    val name: String,
    val blocked: Boolean = false,
    val lastSeen: String = ""
)

interface AdminRepository {
    suspend fun isAppEnabled(): Boolean
    suspend fun setAppEnabled(enabled: Boolean)
    suspend fun users(): List<ManagedUser>
    suspend fun setBlocked(userId: String, blocked: Boolean)
}

@Composable
fun AdminControlPanel(
    repository: AdminRepository,
    onClose: () -> Unit
) {
    var appEnabled by remember { mutableStateOf(true) }
    var users by remember { mutableStateOf<List<ManagedUser>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf("") }

    suspend fun reload() {
        loading = true
        error = ""
        try {
            appEnabled = repository.isAppEnabled()
            users = repository.users()
        } catch (_: Throwable) {
            error = "تعذر تحميل لوحة التحكم"
        }
        loading = false
    }

    LaunchedEffect(Unit) { reload() }

    Surface(Modifier.fillMaxSize(), color = Color(0xFF05090B)) {
        Column(
            Modifier.fillMaxSize().statusBarsPadding().padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("لوحة تحكم TK_SABR", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black, modifier = Modifier.weight(1f))
                TextButton(onClick = onClose) { Text("إغلاق") }
            }

            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF102126)), shape = RoundedCornerShape(20.dp)) {
                Row(Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("حالة التطبيق", color = Color.White, fontWeight = FontWeight.Bold)
                        Text(if (appEnabled) "متاح للمستخدمين" else "متوقف للمستخدمين", color = Color(0xFF37F4D3))
                    }
                    Switch(
                        checked = appEnabled,
                        onCheckedChange = { wanted ->
                            appEnabled = wanted
                        }
                    )
                }
            }

            if (loading) LinearProgressIndicator(Modifier.fillMaxWidth())
            if (error.isNotBlank()) Text(error, color = Color(0xFFFF7788))

            Text("المستخدمون", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(users, key = { it.id }) { user ->
                    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF111A1E)), shape = RoundedCornerShape(16.dp)) {
                        Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(user.name.ifBlank { user.id }, color = Color.White, fontWeight = FontWeight.Bold)
                                if (user.lastSeen.isNotBlank()) Text("آخر نشاط: ${user.lastSeen}", color = Color(0xFFB8CBD2), fontSize = 12.sp)
                            }
                            Text(if (user.blocked) "محظور" else "فعال", color = if (user.blocked) Color(0xFFFF7788) else Color(0xFF37F4D3))
                        }
                    }
                }
            }
        }
    }
}
