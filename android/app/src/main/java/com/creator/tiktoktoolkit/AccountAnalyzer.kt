package com.creator.tiktoktoolkit

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.animation.core.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import org.json.JSONObject
import java.util.Locale

data class TikTokProfile(
    val username: String, val displayName: String, val bio: String,
    val avatarUrl: String, val profileUrl: String, val followers: Long,
    val following: Long, val likes: Long, val videos: Long, val verified: Boolean
)

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun AccountAnalyzer() {
    var username by remember { mutableStateOf("") }
    var requestKey by remember { mutableIntStateOf(0) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    var profile by remember { mutableStateOf<TikTokProfile?>(null) }
    val context = androidx.compose.ui.platform.LocalContext.current

    Box(Modifier.fillMaxSize()) {
        AnimatedCyberBackground()
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp).padding(top = 58.dp, bottom = 38.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(Modifier.size(90.dp).clip(CircleShape).background(Color(0xFF103A42)), contentAlignment = Alignment.Center) {
                Text("⬡", fontSize = 56.sp, color = Color(0xFF20F0D0), fontWeight = FontWeight.Bold)
                Text("✓", fontSize = 23.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }
            Text("TK_SABR", fontSize = 25.sp, color = Color(0xFF37F4D3), fontWeight = FontWeight.ExtraBold)
            Text("محلّل حسابات تيك توك", fontSize = 23.sp, color = Color.White, fontWeight = FontWeight.ExtraBold)
            Text("اكتب اسم المستخدم لفحص بيانات الحساب العامة", color = Color(0xFFD7E5EA), fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = username,
                onValueChange = { username = it.replace("@", "").trim() },
                label = { Text("اسم المستخدم") },
                placeholder = { Text("username") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp)
            )
            Button(
                onClick = { profile = null; error = ""; loading = true; requestKey++ },
                enabled = username.isNotBlank() && !loading,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(18.dp)
            ) { Text(if (loading) "جاري فحص الحساب..." else "فحص الحساب", fontWeight = FontWeight.Bold) }
            if (loading) {
                ScanProgress()
            }
            profile?.let { p ->
                ProfileCard(p)
                StatsCard(p)
                SecurityCard(p)
                Button(
                    onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(p.profileUrl))) },
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)
                ) { Text("فتح البروفايل في TikTok") }
            }
            if (error.isNotBlank()) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer), shape = RoundedCornerShape(16.dp)) {
                    Text(error, Modifier.padding(16.dp), color = MaterialTheme.colorScheme.onErrorContainer)
                }
            }
            Text("يعتمد الفحص على بيانات TikTok العامة. قد يمنع الحساب الخاص أو تقييد TikTok ظهور النتائج.", fontSize = 12.sp, color = Color(0xFFB5C7D0))
            Text("تصميم وبرمجة: المجهول", fontSize = 13.sp, color = Color(0xFF37F4D3), fontWeight = FontWeight.Bold)
        }
        if (loading) {
            TikTokWebReader(username, requestKey) {
                loading = false
                if (it == null) error = "تعذر قراءة بيانات الحساب. تأكد من اليوزر وأن الحساب عام."
                else profile = it
            }
        }
    }
}

@Composable
private fun AnimatedCyberBackground() {
    val transition = rememberInfiniteTransition(label = "background")
    val drift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 34f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "gridDrift"
    )
    val pulse by transition.animateFloat(
        initialValue = 0.12f,
        targetValue = 0.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowPulse"
    )
    Canvas(Modifier.fillMaxSize()) {
        drawRect(Color(0xFF03070C))
        val step = 34.dp.toPx()
        var x = -step + drift.dp.toPx()
        while (x < size.width + step) {
            drawLine(
                Color(0xFF00D9C0).copy(alpha = 0.07f),
                androidx.compose.ui.geometry.Offset(x, 0f),
                androidx.compose.ui.geometry.Offset(x, size.height),
                1f
            )
            x += step
        }
        var y = -step + drift.dp.toPx()
        while (y < size.height + step) {
            drawLine(
                Color(0xFF00D9C0).copy(alpha = 0.07f),
                androidx.compose.ui.geometry.Offset(0f, y),
                androidx.compose.ui.geometry.Offset(size.width, y),
                1f
            )
            y += step
        }
        drawCircle(
            Color(0xFF00D9C0).copy(alpha = pulse),
            radius = size.minDimension * 0.34f,
            center = androidx.compose.ui.geometry.Offset(size.width * 0.5f, size.height * 0.18f)
        )
        drawCircle(
            Color(0xFF7B5CFF).copy(alpha = pulse * 0.55f),
            radius = size.minDimension * 0.28f,
            center = androidx.compose.ui.geometry.Offset(size.width * 0.15f, size.height * 0.82f)
        )
    }
}

@Composable
private fun ScanProgress() {
    val transition = rememberInfiniteTransition(label = "scan")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1150, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0A1720))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(Modifier.size(118.dp), contentAlignment = Alignment.Center) {
                Canvas(Modifier.fillMaxSize()) {
                    drawCircle(Color(0xFF17323B), radius = size.minDimension / 2f)
                    drawCircle(
                        Color(0xFF37F4D3),
                        radius = size.minDimension / 2f - 7.dp.toPx(),
                        style = Stroke(width = 5.dp.toPx())
                    )
                    val angle = Math.toRadians(rotation.toDouble())
                    val r = size.minDimension / 2f - 10.dp.toPx()
                    val x = size.width / 2f + kotlin.math.cos(angle).toFloat() * r
                    val y = size.height / 2f + kotlin.math.sin(angle).toFloat() * r
                    drawLine(
                        Color.White,
                        androidx.compose.ui.geometry.Offset(size.width / 2f, size.height / 2f),
                        androidx.compose.ui.geometry.Offset(x, y),
                        strokeWidth = 4.dp.toPx()
                    )
                }
                Text("SCAN", color = Color.White, fontWeight = FontWeight.Black, fontSize = 17.sp)
            }
            Text("جاري تحليل الحساب", color = Color(0xFF37F4D3), fontSize = 19.sp, fontWeight = FontWeight.ExtraBold)
            Text("الدائرة تدور حتى تكتمل قراءة البيانات العامة...", color = Color(0xFFD7E5EA), fontSize = 13.sp)
        }
    }
}

private data class SecurityCheck(val text: String, val problem: Boolean)

private fun securityScore(p: TikTokProfile): Int {
    var score = 100
    if (p.videos == 0L) score -= 35
    if (p.following > p.followers && p.following > 50L) score -= 20
    if (p.followers > 0L && p.following >= p.followers * 3L) score -= 15
    if (p.bio.isBlank()) score -= 10
    if (p.avatarUrl.isBlank()) score -= 10
    return score.coerceIn(0, 100)
}

private fun securityChecks(p: TikTokProfile): List<SecurityCheck> {
    val checks = mutableListOf<SecurityCheck>()
    if (p.videos == 0L) checks += SecurityCheck("لا توجد منشورات عامة حالياً", true)
    if (p.following > p.followers && p.following > 50L) checks += SecurityCheck("عدد المتابَعين أكبر من عدد المتابعين", true)
    if (p.bio.isBlank()) checks += SecurityCheck("النبذة التعريفية فارغة", true)
    if (p.avatarUrl.isBlank()) checks += SecurityCheck("صورة الحساب غير متاحة", true)
    if (checks.isEmpty()) checks += SecurityCheck("لم تظهر مؤشرات مشكلة من البيانات العامة المتاحة", false)
    return checks
}

@Composable
private fun SecurityCard(p: TikTokProfile) {
    val score = securityScore(p)
    val checks = securityChecks(p)
    Card(
        Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0A1720))
    ) {
        Column(
            Modifier.fillMaxWidth().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("نسبة أمان الحساب", fontSize = 21.sp, color = Color.White, fontWeight = FontWeight.ExtraBold)
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(130.dp)) {
                CircularProgressIndicator(
                    progress = { score / 100f },
                    modifier = Modifier.fillMaxSize(),
                    color = if (score >= 70) Color(0xFF37F4D3) else if (score >= 40) Color(0xFFFFC857) else Color(0xFFFF667A),
                    trackColor = Color(0xFF20323A),
                    strokeWidth = 10.dp
                )
                Text("$score%", fontSize = 28.sp, color = Color.White, fontWeight = FontWeight.Black)
            }
            Text("المؤشر من 0 إلى 100 ويعتمد فقط على إشارات الحساب العامة، وليس فحصاً داخلياً لأمان TikTok.", fontSize = 12.sp, color = Color(0xFFB8CBD2))
            HorizontalDivider(color = Color(0xFF20323A))
            Text("المشاكل الظاهرة", modifier = Modifier.fillMaxWidth(), color = Color.White, fontWeight = FontWeight.Bold)
            checks.forEach { check ->
                Row(
                    Modifier.fillMaxWidth().background(
                        if (check.problem) Color(0xFF2A1820) else Color(0xFF102722),
                        RoundedCornerShape(14.dp)
                    ).padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(if (check.problem) "⚠" else "✓", fontSize = 18.sp, color = if (check.problem) Color(0xFFFF8A9A) else Color(0xFF37F4D3))
                    Spacer(Modifier.width(10.dp))
                    Text(check.text, color = Color(0xFFE5EEF1), fontSize = 13.sp)
                }
            }
            Text("حالة انتهاكات TikTok غير متاحة من صفحة الحساب العامة، لذلك لا يتم اختلاق نتيجة عن وجود مخالفة.", fontSize = 11.sp, color = Color(0xFF8FA6AF))
        }
    }
}

@Composable
private fun ProfileCard(p: TikTokProfile) {
    Card(shape = RoundedCornerShape(24.dp)) {
        Column(
            Modifier.fillMaxWidth().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Avatar(p.avatarUrl)
            Text(p.displayName.ifBlank { "@" + p.username }, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
            Text("@" + p.username, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
            if (p.verified) {
                Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.primaryContainer) {
                    Text("✓ موثّق", Modifier.padding(horizontal = 12.dp, vertical = 5.dp), fontWeight = FontWeight.Bold)
                }
            }
            if (p.bio.isNotBlank()) Text(p.bio, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun Avatar(url: String) {
    var failed by remember(url) { mutableStateOf(false) }
    if (url.isBlank() || failed) {
        Box(Modifier.size(96.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
            Text("TT", fontWeight = FontWeight.Black, fontSize = 24.sp)
        }
    } else {
        AndroidView(
            modifier = Modifier.size(96.dp).clip(CircleShape),
            factory = { context -> android.widget.ImageView(context).apply { scaleType = android.widget.ImageView.ScaleType.CENTER_CROP } },
            update = { view ->
                Thread {
                    try {
                        val c = java.net.URL(url).openConnection()
                        c.connectTimeout = 8000; c.readTimeout = 8000
                        c.getInputStream().use { stream ->
                            val bitmap = android.graphics.BitmapFactory.decodeStream(stream)
                            view.post { view.setImageBitmap(bitmap) }
                        }
                    } catch (_: Exception) { view.post { failed = true } }
                }.start()
            }
        )
    }
}

@Composable
private fun StatsCard(p: TikTokProfile) {
    Card(shape = RoundedCornerShape(22.dp)) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text("إحصائيات الحساب", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Stat("المتابعون", p.followers); Stat("المتابَعون", p.following)
            }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Stat("الإعجابات", p.likes); Stat("الفيديوهات", p.videos)
            }
        }
    }
}

@Composable
private fun Stat(label: String, value: Long) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(formatCount(value), fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun formatCount(value: Long): String = when {
    value >= 1_000_000_000 -> String.format(Locale.US, "%.1fB", value / 1_000_000_000.0)
    value >= 1_000_000 -> String.format(Locale.US, "%.1fM", value / 1_000_000.0)
    value >= 1_000 -> String.format(Locale.US, "%.1fK", value / 1_000.0)
    else -> value.toString()
}

@Composable
private fun TikTokWebReader(username: String, requestKey: Int, onResult: (TikTokProfile?) -> Unit) {
    val callback by rememberUpdatedState(onResult)

    LaunchedEffect(requestKey, username) {
        if (requestKey <= 0 || username.isBlank()) return@LaunchedEffect

        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            val result = try {
                val url = java.net.URL("https://www.tiktok.com/@" + username)
                val connection = (url.openConnection() as java.net.HttpURLConnection).apply {
                    requestMethod = "GET"
                    connectTimeout = 12000
                    readTimeout = 15000
                    instanceFollowRedirects = true
                    setRequestProperty(
                        "User-Agent",
                        "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 Chrome/140.0 Mobile Safari/537.36"
                    )
                    setRequestProperty("Accept-Language", "en-US,en;q=0.9")
                    setRequestProperty("Accept-Encoding", "identity")
                }

                connection.connect()
                val code = connection.responseCode
                if (code !in 200..399) {
                    connection.disconnect()
                    null
                } else {
                    val html = connection.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                    connection.disconnect()
                    parseTikTokHtml(html, username)
                }
            } catch (_: Throwable) {
                null
            }

            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.Main) {
                callback(result)
            }
        }
    }
}

private fun parseTikTokHtml(html: String, fallbackUsername: String): TikTokProfile? {
    return try {
        val scriptRegex = Regex(
            """<script[^>]+id=["']__UNIVERSAL_DATA_FOR_REHYDRATION__["'][^>]*>(.*?)</script>""",
            setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.IGNORE_CASE)
        )
        val match = scriptRegex.find(html) ?: return null
        val root = JSONObject(match.groupValues[1])
        val info = findUserInfo(root, 0) ?: return null
        val user = info.optJSONObject("user") ?: return null
        val stats = info.optJSONObject("stats") ?: JSONObject()

        val uniqueId = user.optString("uniqueId").ifBlank { fallbackUsername }
        TikTokProfile(
            username = uniqueId,
            displayName = user.optString("nickname"),
            bio = user.optString("signature"),
            avatarUrl = user.optString("avatarLarger")
                .ifBlank { user.optString("avatarMedium") }
                .ifBlank { user.optString("avatarThumb") },
            profileUrl = "https://www.tiktok.com/@" + uniqueId,
            followers = stats.optLong("followerCount", 0L),
            following = stats.optLong("followingCount", 0L),
            likes = stats.optLong("heartCount", 0L),
            videos = stats.optLong("videoCount", 0L),
            verified = user.optBoolean("verified", false)
        )
    } catch (_: Throwable) {
        null
    }
}

private fun findUserInfo(value: Any?, depth: Int): JSONObject? {
    if (value == null || depth > 12) return null

    if (value is JSONObject) {
        val direct = value.optJSONObject("userInfo")
        if (direct != null && (direct.has("user") || direct.has("stats"))) return direct

        val keys = value.keys()
        while (keys.hasNext()) {
            val found = findUserInfo(value.opt(keys.next()), depth + 1)
            if (found != null) return found
        }
    } else if (value is org.json.JSONArray) {
        for (i in 0 until value.length()) {
            val found = findUserInfo(value.opt(i), depth + 1)
            if (found != null) return found
        }
    }
    return null
}
