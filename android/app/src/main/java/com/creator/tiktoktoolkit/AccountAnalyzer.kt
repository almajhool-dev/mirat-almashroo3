package com.creator.tiktoktoolkit

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
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

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        Column(Modifier.fillMaxWidth().padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("TikTok Account Analyzer", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold)
            Text("حلّل حساب TikTok عام بواسطة اليوزر", color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedTextField(
                value = username,
                onValueChange = { username = it.replace("@", "").trim() },
                label = { Text("اسم المستخدم") },
                placeholder = { Text("مثال: username") },
                singleLine = true, modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            )
            Button(
                onClick = { profile = null; error = ""; loading = true; requestKey++ },
                enabled = username.isNotBlank() && !loading,
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)
            ) { Text(if (loading) "جاري جلب البيانات..." else "تحليل الحساب") }
        }
        if (loading) LinearProgressIndicator(Modifier.fillMaxWidth())
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            profile?.let { p ->
                ProfileCard(p)
                StatsCard(p)
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
            Text(
                "المحلّل يقرأ البيانات العامة التي يرسلها TikTok لصفحة الحساب. الحساب الخاص أو تغييرات TikTok أو الحظر المؤقت قد تمنع ظهور بعض البيانات.",
                fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        TikTokWebReader(username, requestKey) {
            loading = false
            if (it == null) error = "تعذر قراءة بيانات الحساب. تأكد من اليوزر وأن الحساب عام."
            else profile = it
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
                                val clean = raw.trim().removePrefix(""").removeSuffix(""").replace("\"", """).replace("\\", "\")
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
                web.stopLoading()
                web.loadUrl("https://www.tiktok.com/@" + username)
            }
        }
    )
}
