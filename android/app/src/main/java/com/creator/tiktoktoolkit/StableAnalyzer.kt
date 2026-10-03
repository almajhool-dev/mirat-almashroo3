package com.creator.tiktoktoolkit

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
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
    var requestKey by remember { mutableIntStateOf(0) }
    var loading by remember { mutableStateOf(false) }
    var profile by remember { mutableStateOf<TikTokProfile?>(null) }
    var error by remember { mutableStateOf("") }
    val context = androidx.compose.ui.platform.LocalContext.current

    Box(Modifier.fillMaxSize()) {
        StableAnimatedBackground()
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).statusBarsPadding()
                .padding(horizontal = 20.dp).padding(top = 18.dp, bottom = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(Modifier.size(124.dp).clip(CircleShape).border(2.dp, Color(0xFF20F0D0), CircleShape)) {
                Image(painterResource(R.drawable.tk_sabr_logo_vector), "TK_SABR", Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            }
            Text("TK_SABR", color = Color(0xFF22E6D0), fontSize = 31.sp, fontWeight = FontWeight.Black)
            Text("محلّل حسابات تيك توك", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
            Text("اكتب اسم المستخدم لفحص بيانات الحساب العامة", color = Color(0xFFDCE7EA), fontSize = 14.sp)

            OutlinedTextField(
                value = username,
                onValueChange = { username = it.replace("@", "").trim(); error = "" },
                label = { Text("اسم المستخدم") },
                singleLine = true,
                enabled = !loading,
                modifier = Modifier.fillMaxWidth().height(64.dp),
                shape = RoundedCornerShape(22.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF20F0D0), unfocusedBorderColor = Color(0xFF20F0D0),
                    focusedLabelColor = Color(0xFF20F0D0), unfocusedLabelColor = Color(0xFFDCE7EA),
                    cursorColor = Color(0xFF20F0D0), focusedTextColor = Color.White, unfocusedTextColor = Color.White
                )
            )

            Button(
                onClick = { profile = null; error = ""; loading = true; requestKey++ },
                enabled = username.isNotBlank() && !loading,
                modifier = Modifier.fillMaxWidth().height(60.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF18DED0), contentColor = Color(0xFF031014),
                    disabledContainerColor = Color(0xFF18DED0).copy(alpha = .45f)
                )
            ) {
                Text(if (loading) "جاري فحص الحساب..." else "فحص الحساب  ⌕", fontSize = 18.sp, fontWeight = FontWeight.Black)
            }

            if (loading) {
                ScanProgress()
                LaunchedEffect(requestKey) {
                    val result = readPublicTikTok(username)
                    profile = result
                    loading = false
                    if (result == null) error = "تعذر قراءة بيانات الحساب. تأكد من اسم المستخدم وأن الحساب عام."
                }
            }

            profile?.let { p ->
                ProfileCard(p)
                StatsCard(p)
                SecurityCard(p)
                Button(
                    onClick = { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(p.profileUrl))) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF4B91), contentColor = Color(0xFF160812))
                ) {
                    Text("فتح البروفايل في TikTok", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                }
            }

            if (error.isNotBlank()) {
                Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFF351923)), shape = RoundedCornerShape(16.dp)) {
                    Text(error, Modifier.padding(16.dp), color = Color(0xFFFFA5B5), fontSize = 14.sp)
                }
            }

            Text("يعتمد الفحص على بيانات TikTok العامة فقط. الحساب الخاص أو تقييد TikTok قد يمنع ظهور بعض النتائج.", fontSize = 12.sp, color = Color(0xFFB5C7D0))
            Text("تصميم وبرمجة: المجهول", fontSize = 13.sp, color = Color(0xFF37F4D3), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun StableAnimatedBackground() {
    val transition = rememberInfiniteTransition(label = "stable_bg")
    val x by transition.animateFloat(-18f, 18f, infiniteRepeatable(tween(8500, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "x")
    val y by transition.animateFloat(-12f, 12f, infiniteRepeatable(tween(10500, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "y")
    Box(Modifier.fillMaxSize().background(Color.Black)) {
        Image(
            painterResource(R.drawable.tk_background), null,
            Modifier.fillMaxSize().graphicsLayer {
                translationX = x
                translationY = y
                scaleX = 1.07f
                scaleY = 1.07f
                alpha = .96f
            },
            contentScale = ContentScale.Crop
        )
        Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = .18f)))
        Canvas(Modifier.fillMaxSize()) {
            drawRect(
                Brush.radialGradient(
                    listOf(Color(0xFF0B5A58).copy(alpha = .22f), Color.Transparent),
                    center = androidx.compose.ui.geometry.Offset(size.width * .5f, size.height * .4f),
                    radius = size.maxDimension * .75f
                )
            )
        }
    }
}

@Composable
private fun ScanProgress() {
    val transition = rememberInfiniteTransition(label = "scan")
    val rotation by transition.animateFloat(0f, 360f, infiniteRepeatable(tween(1050, easing = LinearEasing), RepeatMode.Restart), label = "rotation")
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color(0xE6091720))) {
        Column(
            Modifier.fillMaxWidth().padding(vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Box(Modifier.size(106.dp), contentAlignment = Alignment.Center) {
                Canvas(Modifier.fillMaxSize()) {
                    val r = size.minDimension / 2f - 8.dp.toPx()
                    drawCircle(Color(0xFF17323B), r)
                    drawArc(Color(0xFF37F4D3), rotation, 285f, false, style = Stroke(6.dp.toPx(), cap = StrokeCap.Round))
                    drawArc(Color.White.copy(alpha = .8f), rotation + 290f, 35f, false, style = Stroke(4.dp.toPx(), cap = StrokeCap.Round))
                }
                Text("SCAN", color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
            }
            Text("جاري تحليل الحساب", color = Color(0xFF37F4D3), fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
            Text("يتم جلب الصورة والإحصائيات والبيانات العامة...", color = Color(0xFFD7E5EA), fontSize = 12.sp)
        }
    }
}

@Composable
private fun ProfileCard(p: TikTokProfile) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color(0xE62F2D34))) {
        Column(
            Modifier.fillMaxWidth().padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Avatar(p.avatarUrl)
            Text(p.displayName.ifBlank { "@" + p.username }, fontSize = 23.sp, color = Color.White, fontWeight = FontWeight.ExtraBold)
            Text("@" + p.username, color = Color(0xFF20E6D0), fontSize = 16.sp, fontWeight = FontWeight.Bold)
            if (p.verified) {
                Surface(shape = RoundedCornerShape(50), color = Color(0xFF103C3B)) {
                    Text("✓ موثّق", Modifier.padding(horizontal = 12.dp, vertical = 5.dp), color = Color(0xFF37F4D3), fontWeight = FontWeight.Bold)
                }
            }
            if (p.bio.isNotBlank()) Text(p.bio, Modifier.padding(top = 5.dp), color = Color(0xFFDCE4E7), fontSize = 14.sp)
        }
    }
}

@Composable
private fun Avatar(url: String) {
    var bitmap by remember(url) { mutableStateOf<Bitmap?>(null) }
    LaunchedEffect(url) {
        bitmap = if (url.isBlank()) null else withContext(Dispatchers.IO) {
            try {
                val c = URL(url).openConnection().apply {
                    connectTimeout = 8000
                    readTimeout = 8000
                    setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 Chrome/140 Mobile Safari/537.36")
                }
                c.getInputStream().use { BitmapFactory.decodeStream(it) }
            } catch (_: Throwable) { null }
        }
    }
    Box(
        Modifier.size(100.dp).clip(CircleShape).border(2.dp, Color(0xFF20F0D0), CircleShape).background(Color(0xFF10181B)),
        contentAlignment = Alignment.Center
    ) {
        if (bitmap != null) {
            Image(bitmap!!.asImageBitmap(), "صورة الحساب", Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
        } else {
            Image(painterResource(R.drawable.tk_sabr_logo_vector), null, Modifier.fillMaxSize().padding(8.dp), contentScale = ContentScale.Crop)
        }
    }
}

@Composable
private fun StatsCard(p: TikTokProfile) {
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = Color(0xE62F2D34))) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
            Text("إحصائيات الحساب", color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.ExtraBold)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Stat("المتابعون", p.followers)
                Stat("المتابَعون", p.following)
                Stat("الإعجابات", p.likes)
                Stat("الفيديوهات", p.videos)
            }
        }
    }
}

@Composable
private fun Stat(label: String, value: Long) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(formatCount(value), color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
        Text(label, color = Color(0xFFC5D0D4), fontSize = 10.sp)
    }
}

private data class SecurityCheck(val text: String, val problem: Boolean)

private fun securityScore(p: TikTokProfile): Int {
    var score = 100
    if (p.videos == 0L) score -= 30
    if (p.bio.isBlank()) score -= 10
    if (p.avatarUrl.isBlank()) score -= 10
    if (p.following > p.followers && p.following > 50L) score -= 15
    if (p.followers > 0L && p.following >= p.followers * 3L) score -= 15
    return score.coerceIn(0, 100)
}

private fun securityChecks(p: TikTokProfile): List<SecurityCheck> {
    val checks = mutableListOf<SecurityCheck>()
    if (p.videos == 0L) checks += SecurityCheck("لا توجد منشورات عامة حالياً", true)
    if (p.bio.isBlank()) checks += SecurityCheck("النبذة التعريفية فارغة", true)
    if (p.avatarUrl.isBlank()) checks += SecurityCheck("صورة الحساب غير متاحة", true)
    if (p.following > p.followers && p.following > 50L) checks += SecurityCheck("عدد المتابَعين أكبر من عدد المتابعين", true)
    if (checks.isEmpty()) checks += SecurityCheck("لم تظهر مؤشرات مشكلة من البيانات العامة المتاحة", false)
    return checks
}

@Composable
private fun SecurityCard(p: TikTokProfile) {
    val score = securityScore(p)
    val checks = securityChecks(p)
    val scoreColor = when {
        score >= 70 -> Color(0xFF37F4D3)
        score >= 40 -> Color(0xFFFFC857)
        else -> Color(0xFFFF667A)
    }
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color(0xE62F2D34))) {
        Column(
            Modifier.fillMaxWidth().padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("نسبة أمان الحساب", color = Color.White, fontSize = 21.sp, fontWeight = FontWeight.ExtraBold)
            Box(Modifier.size(122.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { score / 100f },
                    modifier = Modifier.fillMaxSize(),
                    color = scoreColor,
                    trackColor = Color(0xFF20323A),
                    strokeWidth = 10.dp,
                    strokeCap = StrokeCap.Round
                )
                Text("$score%", color = Color.White, fontSize = 27.sp, fontWeight = FontWeight.Black)
            }
            Text("مؤشر تقديري من إشارات الحساب العامة فقط، وليس فحصاً داخلياً لأمان TikTok.", color = Color(0xFFB8CBD2), fontSize = 11.sp)
            HorizontalDivider(color = Color(0xFF26363D))
            Text("المشاكل الظاهرة", Modifier.fillMaxWidth(), color = Color.White, fontWeight = FontWeight.Bold)
            checks.forEach { check ->
                Row(
                    Modifier.fillMaxWidth().background(
                        if (check.problem) Color(0xFF3A1C25) else Color(0xFF102C27),
                        RoundedCornerShape(13.dp)
                    ).padding(11.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(if (check.problem) "⚠" else "✓", color = if (check.problem) Color(0xFFFF8A9A) else Color(0xFF37F4D3), fontSize = 17.sp)
                    Spacer(Modifier.width(9.dp))
                    Text(check.text, color = Color(0xFFE5EEF1), fontSize = 12.sp)
                }
            }
            Text("حالة الحظر أو المخالفات الداخلية لا يمكن تأكيدها من صفحة الحساب العامة.", color = Color(0xFF8FA6AF), fontSize = 10.sp)
        }
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
