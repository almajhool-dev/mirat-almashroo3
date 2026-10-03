package com.creator.tiktoktoolkit

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
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

    Box(Modifier.fillMaxSize().background(Color(0xFF060D16))) {
        Canvas(Modifier.fillMaxSize()) {
            val step = 34.dp.toPx()
            var x = 0f
            while (x < size.width) {
                drawLine(Color(0xFF00D9C0).copy(alpha = 0.055f), androidx.compose.ui.geometry.Offset(x, 0f), androidx.compose.ui.geometry.Offset(x, size.height), 1f)
                x += step
            }
            var y = 0f
            while (y < size.height) {
                drawLine(Color(0xFF00D9C0).copy(alpha = 0.055f), androidx.compose.ui.geometry.Offset(0f, y), androidx.compose.ui.geometry.Offset(size.width, y), 1f)
                y += step
            }
        }
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
            Text("CYBER • TIKTOK", fontSize = 25.sp, color = Color(0xFF37F4D3), fontWeight = FontWeight.ExtraBold)
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

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun TikTokWebReader(username: String, requestKey: Int, onResult: (TikTokProfile?) -> Unit) {
    val callback by rememberUpdatedState(onResult)
    AndroidView(
        modifier = Modifier.size(1.dp),
        factory = { context ->
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(1, 1)
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.userAgentString = "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 Chrome/140.0 Mobile Safari/537.36"
                webChromeClient = WebChromeClient()
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        if (requestKey <= 0 || username.isBlank()) return
                        val js = """
                            (function(){
                              try {
                                var el=document.getElementById('__UNIVERSAL_DATA_FOR_REHYDRATION__');
                                if(!el) return JSON.stringify({error:'no_data'});
                                var root=JSON.parse(el.textContent), found=null;
                                function walk(o){
                                  if(found||o===null||typeof o!=='object') return;
                                  if(o.userInfo && (o.userInfo.stats || o.userInfo.user)){found=o.userInfo;return;}
                                  if(Array.isArray(o)){for(var i=0;i<o.length&&!found;i++)walk(o[i]);}
                                  else {for(var k in o){if(Object.prototype.hasOwnProperty.call(o,k))walk(o[k]);}}
                                }
                                walk(root);
                                if(!found) return JSON.stringify({error:'profile_not_found'});
                                var u=found.user||{}, s=found.stats||{};
                                return JSON.stringify({
                                  username:u.uniqueId||'""" + username + """',
                                  displayName:u.nickname||'', bio:u.signature||'',
                                  avatarUrl:u.avatarLarger||u.avatarMedium||u.avatarThumb||'',
                                  profileUrl:'https://www.tiktok.com/@'+(u.uniqueId||'""" + username + """'),
                                  followers:Number(s.followerCount||0), following:Number(s.followingCount||0),
                                  likes:Number(s.heartCount||0), videos:Number(s.videoCount||0),
                                  verified:Boolean(u.verified)
                                });
                              } catch(e){return JSON.stringify({error:String(e)});}
                            })();
                        """.trimIndent()
                        view?.evaluateJavascript(js) { raw ->
                            try {
                                val clean = raw.trim().removePrefix("\"").removeSuffix("\"").replace("\\\"", "\"").replace("\\\\", "\\")
                                val json = JSONObject(clean)
                                if (json.has("error")) callback(null)
                                else callback(TikTokProfile(
                                    json.optString("username", username),
                                    json.optString("displayName"),
                                    json.optString("bio"),
                                    json.optString("avatarUrl"),
                                    json.optString("profileUrl", "https://www.tiktok.com/@" + username),
                                    json.optLong("followers"), json.optLong("following"),
                                    json.optLong("likes"), json.optLong("videos"),
                                    json.optBoolean("verified")
                                ))
                            } catch (_: Exception) { callback(null) }
                        }
                    }
                }
            }
        },
        update = { web ->
            if (requestKey > 0 && username.isNotBlank()) {
                val currentKey = web.getTag(android.R.id.content) as? Int
                if (currentKey != requestKey) {
                    web.setTag(android.R.id.content, requestKey)
                    web.stopLoading()
                    web.loadUrl("https://www.tiktok.com/@" + username)
                }
            }
        }
    )
}
