package com.creator.tiktoktoolkit

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import org.json.JSONObject

data class TikTokProfile(
    val username: String,
    val displayName: String = "",
    val followers: Long = 0,
    val following: Long = 0,
    val likes: Long = 0,
    val videos: Long = 0,
    val verified: Boolean = false
)

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun AccountAnalyzer() {
    var username by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf("") }
    var profile by remember { mutableStateOf<TikTokProfile?>(null) }

    LazyColumn(Modifier.fillMaxSize().padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
        item {
            Text("تحليل حساب TikTok", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("اكتب اليوزر بدون @ للحصول على الإحصائيات الظاهرة علناً.", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        item {
            OutlinedTextField(username, { username = it.removePrefix("@").trim() }, label = { Text("اسم المستخدم") }, placeholder = { Text("مثال: username") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        }
        item {
            Button({ loading = true; error = ""; profile = null }, enabled = username.isNotBlank() && !loading, modifier = Modifier.fillMaxWidth()) {
                Text(if (loading) "جاري التحليل..." else "تحليل الحساب")
            }
        }
        if (error.isNotBlank()) item { Text(error, color = MaterialTheme.colorScheme.error) }
        profile?.let { p ->
            item {
                Card {
                    Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("@${p.username}", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                        if (p.displayName.isNotBlank()) Text(p.displayName)
                        if (p.verified) Text("✓ حساب موثّق", color = MaterialTheme.colorScheme.primary)
                        HorizontalDivider()
                        StatLine("المتابعون", p.followers)
                        StatLine("المتابَعون", p.following)
                        StatLine("الإعجابات", p.likes)
                        StatLine("الفيديوهات", p.videos)
                    }
                }
            }
        }
        item { Text("ملاحظة: البيانات تُقرأ من صفحة الحساب العامة في TikTok. إذا طلب TikTok تسجيل دخول أو غيّر بنية الصفحة، قد لا تتوفر الأرقام.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        item {
            AccountWebReader(username, loading) { result ->
                loading = false
                if (result != null && (result.followers > 0 || result.following > 0 || result.likes > 0 || result.videos > 0)) profile = result
                else error = "لم أتمكن من قراءة الإحصائيات لهذا الحساب."
            }
        }
    }
}

@Composable
private fun StatLine(label: String, value: Long) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label); Text(formatCount(value), fontWeight = FontWeight.Bold)
    }
}

private fun formatCount(value: Long): String = when {
    value >= 1_000_000_000 -> "%.1fB".format(value / 1_000_000_000.0)
    value >= 1_000_000 -> "%.1fM".format(value / 1_000_000.0)
    value >= 1_000 -> "%.1fK".format(value / 1_000.0)
    else -> value.toString()
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun AccountWebReader(username: String, start: Boolean, onResult: (TikTokProfile?) -> Unit) {
    val callback by rememberUpdatedState(onResult)
    AndroidView(
        modifier = Modifier.size(1.dp),
        factory = { context ->
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(1, 1)
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                webChromeClient = WebChromeClient()
                webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        if (start && username.isNotBlank()) evaluateJavascript(
                            """(function(){
                                var h=document.documentElement.innerHTML;
                                function pick(k){var r=new RegExp('["\\']'+k+'["\\']\\s*:\\s*["\\']?([0-9]+)["\\']?','i').exec(h);return r?r[1]:"0";}
                                var n=(/"nickname"\\s*:\\s*"([^"]+)"/i.exec(h)||[])[1]||"";
                                var v=/"verified"\\s*:\\s*(true|false)/i.exec(h);
                                return JSON.stringify({username:"$username",displayName:n,followers:pick("followerCount"),following:pick("followingCount"),likes:pick("heartCount"),videos:pick("videoCount"),verified:v?v[1]==="true":false});
                            })();"""
                        ) { raw ->
                            try {
                                val json = JSONObject(raw.trim('"').replace("\\", ""))
                                callback(TikTokProfile(json.optString("username", username), json.optString("displayName"), json.optLong("followers"), json.optLong("following"), json.optLong("likes"), json.optLong("videos"), json.optBoolean("verified")))
                            } catch (_: Exception) { callback(null) }
                        }
                    }
                }
            }
        },
        update = { web -> if (start && username.isNotBlank()) web.loadUrl("https://www.tiktok.com/@$username") }
    )
}
