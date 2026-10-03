package com.creator.tiktoktoolkit

import android.app.Activity
import android.os.Bundle
import java.security.MessageDigest
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.creator.tiktoktoolkit.ui.AppTheme
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

private const val OWNER_EMAIL_SHA256 = "26229eb3b872e1ec9197ef6939ae28e82d945c7c8c741eeb6c3e01349f5164e7"

private fun isOwner(email: String?): Boolean {
    if (email.isNullOrBlank()) return false
    val normalized = email.trim().lowercase()
    val bytes = MessageDigest.getInstance("SHA-256").digest(normalized.toByteArray())
    return bytes.joinToString("") { "%02x".format(it) } == OWNER_EMAIL_SHA256
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                val repository = remember { LocalAdminRepository() }
                val gso = remember {
                    GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .build()
                }
                val googleClient = remember { GoogleSignIn.getClient(this, gso) }
                var ownerVerified by remember { mutableStateOf(false) }
                var checkedAccount by remember { mutableStateOf(false) }
                var loginStarted by remember { mutableStateOf(false) }

                val signInLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                    checkedAccount = true
                    loginStarted = true
                    if (result.resultCode == Activity.RESULT_OK) {
                        runCatching { GoogleSignIn.getSignedInAccountFromIntent(result.data).result }
                            .onSuccess { ownerVerified = isOwner(it.email) }
                            .onFailure { ownerVerified = false }
                    } else {
                        ownerVerified = false
                    }
                }

                LaunchedEffect(Unit) {
                    val lastAccount = GoogleSignIn.getLastSignedInAccount(this@MainActivity)
                    ownerVerified = isOwner(lastAccount?.email)
                    checkedAccount = true
                    if (!ownerVerified && !loginStarted) {
                        loginStarted = true
                        signInLauncher.launch(googleClient.signInIntent)
                    }
                }

                when {
                    ownerVerified -> AdminControlPanel(repository = repository, onClose = {})
                    !checkedAccount -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("جاري التحقق من حساب المدير…")
                    }
                    else -> Box(Modifier.fillMaxSize()) {
                        StableAccountAnalyzer()
                        Button(
                            onClick = { signInLauncher.launch(googleClient.signInIntent) },
                            modifier = Modifier.align(Alignment.BottomCenter).padding(18.dp)
                        ) { Text("تسجيل دخول المدير") }
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
