package com.creator.tiktoktoolkit

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

@Composable
fun StableAccountAnalyzer() {
    var username by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var profile by remember { mutableStateOf<TikTokProfile?>(null) }
    var error by remember { mutableStateOf("") }
    val context = androidx.compose.ui.platform.LocalContext.current

    Box(Modifier.fillMaxSize()) {
        StableAnimatedBackground()
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).statusBarsPadding()
                .padding(horizontal = 20.dp, vertical = 22.dp).padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(Modifier.size(150.dp).clip(CircleShape).border(2.dp, Color(0xFF20F0D0), CircleShape)) {
                Image(painterResource(R.drawable.tk_sabr_logo_vector), "TK_SABR", Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            }
            Text("TK_SABR", color = Color(0xFF22E6D0), fontSize = 34.sp, fontWeight = FontWeight.Black)
            Text("محلّل حسابات تيك توك", color = Color.White, fontSize = 23.sp, fontWeight = FontWeight.ExtraBold)
            Text("اكتب اسم المستخدم لفحص بيانات الحساب العامة", color = Color(0xFFDCE7EA), fontSize = 15.sp)

            OutlinedTextField(
                value = username,
                onValueChange = { username = it.replace("@", "").trim() },
                label = { Text("اسم المستخدم") },
                singleLine = true, enabled = !loading,
                modifier = Modifier.fillMaxWidth().height(64.dp),
                shape = RoundedCornerShape(22.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF20F0D0), unfocusedBorderColor = Color(0xFF20F0D0),
                    focusedLabelColor = Color(0xFF20F0D0), unfocusedLabelColor = Color(0xFFDCE7EA),
                    cursorColor = Color(0xFF20F0D0)
                )
            )
            Button(
                onClick = { loading = true; error = ""; profile = null },
                enabled = username.isNotBlank() && !loading,
                modifier = Modifier.fillMaxWidth().height(60.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF18DED0), contentColor = Color(0xFF031014))
            ) {
                Text(if (loading) "جاري الفحص..." else "فحص الحساب  ⌕", fontSize = 18.sp, fontWeight = FontWeight.Black)
            }

            if (loading) {
                StableScanner()
                LaunchedEffect(username) {
                    val result = readPublicTikTok(username)
                    profile = result
                    loading = false
                    if (result == null) error = "تعذر قراءة بيانات الحساب. تأكد من اليوزر وأن الحساب عام."
                }
            }

            profile?.let { p ->
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp)) {
                    Column(Modifier.fillMaxWidth().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(p.displayName.ifBlank { "@${p.username}" }, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                        Text("@${p.username}", color = Color(0xFF13CDBF), fontWeight = FontWeight.Bold)
                        if (p.verified) Text("✓ موثّق", color = Color(0xFF20F0D0), fontWeight = FontWeight.Bold)
                        if (p.bio.isNotBlank()) Text(p.bio, Modifier.padding(top = 8.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp)) {
                    Column(Modifier.fillMaxWidth().padding(18.dp)) {
                        Text("إحصائيات الحساب", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Row(Modifier.fillMaxWidth().padding(top = 14.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                            StableStat("المتابعون", p.followers); StableStat("المتابَعون", p.following)
                            StableStat("الإعجابات", p.likes); StableStat("الفيديوهات", p.videos)
                        }
                    }
                }
                Button(
                    onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(p.profileUrl))) },
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(18.dp)
                ) { Text("فتح البروفايل في TikTok") }
            }
            if (error.isNotBlank()) Text(error, color = Color(0xFFFF9AAA), modifier = Modifier.fillMaxWidth())
            Text("يعتمد الفحص على بيانات TikTok العامة فقط.", fontSize = 12.sp, color = Color(0xFFB5C7D0))
            Text("تصميم وبرمجة: المجهول", fontSize = 13.sp, color = Color(0xFF37F4D3), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun StableAnimatedBackground() {
    val transition = rememberInfiniteTransition(label = "stable_bg")
    val x by transition.animateFloat(-20f, 20f, infiniteRepeatable(tween(7000), RepeatMode.Reverse), label = "x")
    val y by transition.animateFloat(-12f, 12f, infiniteRepeatable(tween(9000), RepeatMode.Reverse), label = "y")
    Canvas(Modifier.fillMaxSize().background(Color.Black)) {
        val center = androidx.compose.ui.geometry.Offset(size.width * .52f + x, size.height * .40f + y)
        drawRect(Brush.radialGradient(listOf(Color(0xFF063A3A), Color(0xFF02090B), Color.Black), center = center, radius = size.maxDimension * .85f))
        drawCircle(Color(0xFF11DCCB).copy(alpha = .055f), size.minDimension * .38f,
            androidx.compose.ui.geometry.Offset(size.width * .16f - x, size.height * .78f - y))
    }
}

@Composable
private fun StableScanner() {
    CircularProgressIndicator(
        progress = { .72f }, modifier = Modifier.padding(12.dp).size(68.dp),
        color = Color(0xFF37F4D3), trackColor = Color(0xFF20323A),
        strokeWidth = 6.dp, strokeCap = StrokeCap.Round
    )
}

@Composable
private fun StableStat(label: String, value: Long) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(stableFormat(value), fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
        Text(label, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun stableFormat(value: Long): String = when {
    value >= 1_000_000_000 -> String.format(Locale.US, "%.1fB", value / 1_000_000_000.0)
    value >= 1_000_000 -> String.format(Locale.US, "%.1fM", value / 1_000_000.0)
    value >= 1_000 -> String.format(Locale.US, "%.1fK", value / 1_000.0)
    else -> value.toString()
}

private suspend fun readPublicTikTok(username: String): TikTokProfile? = withContext(Dispatchers.IO) {
    try {
        val connection = (URL("https://www.tiktok.com/@$username").openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"; connectTimeout = 10000; readTimeout = 12000; instanceFollowRedirects = true
            setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 Chrome/140 Mobile Safari/537.36")
            setRequestProperty("Accept-Language", "en-US,en;q=0.9"); setRequestProperty("Accept-Encoding", "identity")
        }
        val code = connection.responseCode
        if (code !in 200..399) return@withContext null
        val html = connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        connection.disconnect()
        parseStableTikTok(html, username)
    } catch (_: Throwable) { null }
}

private fun parseStableTikTok(html: String, fallback: String): TikTokProfile? {
    return try {
        val regex = Regex("""<script[^>]+id=["']__UNIVERSAL_DATA_FOR_REHYDRATION__["'][^>]*>(.*?)</script>""", setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.IGNORE_CASE))
        val m = regex.find(html) ?: return null
        val root = JSONObject(m.groupValues[1])
        val info = findStableUserInfo(root, 0) ?: return null
        val user = info.optJSONObject("user") ?: return null
        val stats = info.optJSONObject("stats") ?: JSONObject()
        val id = user.optString("uniqueId").ifBlank { fallback }
        TikTokProfile(id, user.optString("nickname"), user.optString("signature"), user.optString("avatarLarger"),
            "https://www.tiktok.com/@$id", stats.optLong("followerCount"), stats.optLong("followingCount"),
            stats.optLong("heartCount"), stats.optLong("videoCount"), user.optBoolean("verified"))
    } catch (_: Throwable) { null }
}

private fun findStableUserInfo(value: Any?, depth: Int): JSONObject? {
    if (value == null || depth > 12) return null
    if (value is JSONObject) {
        val direct = value.optJSONObject("userInfo")
        if (direct != null && (direct.has("user") || direct.has("stats"))) return direct
        val keys = value.keys()
        while (keys.hasNext()) findStableUserInfo(value.opt(keys.next()), depth + 1)?.let { return it }
    } else if (value is org.json.JSONArray) {
        for (i in 0 until value.length()) findStableUserInfo(value.opt(i), depth + 1)?.let { return it }
    }
    return null
}
