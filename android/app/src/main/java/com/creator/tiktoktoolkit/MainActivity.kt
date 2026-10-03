package com.creator.tiktoktoolkit

import android.app.Activity
import android.os.Bundle
import java.security.MessageDigest
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.creator.tiktoktoolkit.ui.AppTheme
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

private const val OWNER_EMAIL_SHA256 = "16538c1b0bbe8cf0002acdb02015caf5dbf5afdb9835003652aee8abad88c26b"
private const val WEB_CLIENT_ID = "205830966158-hrlf09nalsc90j1cd1ot5682udi199p8.apps.googleusercontent.com"

private fun isOwner(email: String?): Boolean {
    if (email.isNullOrBlank()) return false
    val bytes = MessageDigest.getInstance("SHA-256").digest(email.lowercase().toByteArray())
    return bytes.joinToString("") { "%02x".format(it) } == OWNER_EMAIL_SHA256
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                val repository = remember { LocalAdminRepository() }
                var adminOpen by remember { mutableStateOf(false) }
                var ownerVerified by remember { mutableStateOf(isOwner(GoogleSignIn.getLastSignedInAccount(this)?.email)) }
                var secretTapCount by remember { mutableIntStateOf(0) }

                val gso = remember {
                    GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .requestIdToken(WEB_CLIENT_ID)
                        .build()
                }
                val googleClient = remember { GoogleSignIn.getClient(this, gso) }
                val signInLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                    if (result.resultCode == Activity.RESULT_OK) {
                        runCatching { GoogleSignIn.getSignedInAccountFromIntent(result.data).result }
                            .onSuccess { account ->
                                ownerVerified = isOwner(account.email)
                                adminOpen = ownerVerified
                            }
                    }
                }

                if (adminOpen && ownerVerified) {
                    AdminControlPanel(repository = repository, onClose = { adminOpen = false })
                } else {
                    Box(Modifier.fillMaxSize()) {
                        StableAccountAnalyzer()
                        Box(
                            Modifier
                                .align(Alignment.BottomEnd)
                                .size(64.dp)
                                .clickable {
                                    secretTapCount++
                                    if (secretTapCount >= 5) {
                                        secretTapCount = 0
                                        if (ownerVerified) adminOpen = true
                                        else signInLauncher.launch(googleClient.signInIntent)
                                    }
                                }
                        )
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
