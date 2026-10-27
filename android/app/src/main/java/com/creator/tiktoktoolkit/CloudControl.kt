package com.creator.tiktoktoolkit

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.MetadataChanges
import com.google.firebase.messaging.FirebaseMessaging
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val OWNER_UID = "gxGMsHfX6GVfbadH0N3JmWNbCS92"

data class PublicConfig(
    val enabled: Boolean = true,
    val title: String = "TK_SABR",
    val subtitle: String = "محلّل حسابات تيك توك",
    val footer: String = "تصميم وبرمجة: المجهول",
    val theme: Int = 0,
    val motion: Boolean = true,
    val background: String = "",
    val logo: String = "",
    val announcement: String = "",
    val maintenance: String = "التطبيق متوقف للصيانة، جرّب لاحقاً",
    val themeNames: String = "خوارزميات الأمن السيبراني,شبكة التشفير,مدارات الحماية"
) {
    fun fields(): Map<String, Any> = mapOf(
        "enabled" to enabled, "title" to title.take(80), "subtitle" to subtitle.take(160),
        "footer" to footer.take(160), "theme" to theme.coerceIn(0, 2), "motion" to motion,
        "background" to background, "logo" to logo, "announcement" to announcement.take(1000),
        "maintenance" to maintenance.take(300), "themeNames" to themeNames.take(500)
    )
}

@Composable
fun CloudControlledApp() {
    val context = LocalContext.current
    val auth = remember { FirebaseAuth.getInstance() }
    val document = remember { FirebaseFirestore.getInstance().document("appConfig/public") }
    var config by remember { mutableStateOf(PublicConfig()) }
    var ready by remember { mutableStateOf(false) }
    var cloudError by remember { mutableStateOf("") }
    var owner by remember { mutableStateOf(auth.currentUser?.uid == OWNER_UID) }
    var panel by remember { mutableStateOf(false) }
    var retry by remember { mutableIntStateOf(0) }
    var notificationStatus by remember { mutableStateOf("") }
    fun subscribe() {
        FirebaseMessaging.getInstance().subscribeToTopic("tk_sabr_public")
            .addOnCompleteListener { task ->
                notificationStatus = if (task.isSuccessful) "تم تفعيل استقبال الإشعارات" else "تعذر تفعيل الإشعارات، جرّب مرة ثانية"
            }
    }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) subscribe() else notificationStatus = "الإشعارات غير مفعّلة؛ تقدر تسمح بيها من إعدادات الهاتف"
    }
    DisposableEffect(auth) {
        val listener = FirebaseAuth.AuthStateListener { owner = it.currentUser?.uid == OWNER_UID }
        auth.addAuthStateListener(listener)
        onDispose { auth.removeAuthStateListener(listener) }
    }
    DisposableEffect(document, retry) {
        ready = false
        val listener = document.addSnapshotListener(MetadataChanges.INCLUDE) { snapshot, error ->
            if (error != null) {
                cloudError = "تعذر التحقق من إعدادات التطبيق. تأكد من الإنترنت وصلاحيات قاعدة البيانات"
                ready = false
            } else if (snapshot != null && !snapshot.metadata.isFromCache && snapshot.exists()) {
                fun value(key: String, default: String, max: Int) = (snapshot.getString(key) ?: default).take(max)
                val parsed = runCatching { PublicConfig(
                    enabled = snapshot.getBoolean("enabled") ?: false,
                    title = value("title", "TK_SABR", 80),
                    subtitle = value("subtitle", "محلّل حسابات تيك توك", 160),
                    footer = value("footer", "تصميم وبرمجة: المجهول", 160),
                    theme = (snapshot.getLong("theme") ?: 0).coerceIn(0, 2).toInt(),
                    motion = snapshot.getBoolean("motion") ?: true,
                    background = value("background", "", 180000), logo = value("logo", "", 180000),
                    announcement = value("announcement", "", 1000),
                    maintenance = value("maintenance", "التطبيق متوقف للصيانة، جرّب لاحقاً", 300),
                    themeNames = value("themeNames", PublicConfig().themeNames, 500)
                ) }.getOrNull()
                if (parsed != null) { config = parsed; ready = true; cloudError = "" }
                else { ready = false; cloudError = "صيغة إعدادات التطبيق غير صالحة" }
            } else if (snapshot != null && !snapshot.metadata.isFromCache) {
                cloudError = "إعدادات التطبيق غير موجودة"; ready = false
            }
        }
        onDispose { listener.remove() }
    }
    Box(Modifier.fillMaxSize()) {
        if (ready && config.enabled) {
            StableAccountAnalyzer(config, { panel = true }, {
                if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
                    permission.launch(Manifest.permission.POST_NOTIFICATIONS)
                else subscribe()
            })
        } else Column(Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Text(config.title, Modifier.combinedClickable(onClick = {}, onLongClick = { panel = true }))
            Spacer(Modifier.height(20.dp))
            Text(if (ready) config.maintenance else cloudError.ifBlank { "جاري التحقق من إعدادات التطبيق؛ يحتاج اتصال بالإنترنت" })
            TextButton(onClick = { retry++ }) { Text("إعادة المحاولة") }
        }
        if (panel) OwnerPanel(auth, owner, config, onClose = { panel = false }, onSave = { edited, done ->
            if (auth.currentUser?.uid != OWNER_UID) done(false)
            else document.set(edited.fields()).addOnCompleteListener { done(it.isSuccessful) }
        })
        if (notificationStatus.isNotEmpty()) AlertDialog(onDismissRequest = { notificationStatus = "" }, text = { Text(notificationStatus) }, confirmButton = { TextButton(onClick = { notificationStatus = "" }) { Text("تم") } })
    }
}

@Composable
private fun OwnerPanel(auth: FirebaseAuth, owner: Boolean, config: PublicConfig, onClose: () -> Unit, onSave: (PublicConfig, (Boolean) -> Unit) -> Unit) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var busy by remember { mutableStateOf(false) }
    var status by remember { mutableStateOf("") }
    var draft by remember { mutableStateOf(config) }
    var pickLogo by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) scope.launch {
            busy = true
            val encoded = withContext(Dispatchers.IO) {
                runCatching {
                    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                    context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
                    var sample = 1
                    while (bounds.outWidth / sample > 800 || bounds.outHeight / sample > 800) sample *= 2
                    val bitmap = context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample }) } ?: error("image")
                    val output = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 65, output)
                    bitmap.recycle()
                    require(output.size() <= 125000)
                    "data:image/jpeg;base64," + Base64.encodeToString(output.toByteArray(), Base64.NO_WRAP)
                }.getOrNull()
            }
            if (encoded == null) status = "الصورة كبيرة أو غير صالحة، اختار صورة أصغر"
            else draft = if (pickLogo) draft.copy(logo = encoded) else draft.copy(background = encoded)
            busy = false
        }
    }
    AlertDialog(onDismissRequest = { if (!busy) onClose() }, title = { Text(if (owner) "لوحة المالك — إعدادات الجميع" else "دخول المالك") }, text = {
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (!owner) {
                OutlinedTextField(email, { email = it }, label = { Text("الإيميل") }, singleLine = true, enabled = !busy)
                OutlinedTextField(password, { password = it }, label = { Text("كلمة السر") }, visualTransformation = PasswordVisualTransformation(), singleLine = true, enabled = !busy)
                Button(enabled = !busy && email.isNotBlank() && password.isNotBlank(), onClick = {
                    busy = true; status = ""
                    auth.signInWithEmailAndPassword(email.trim(), password).addOnCompleteListener { task ->
                        password = ""; busy = false
                        if (!task.isSuccessful) status = "تعذر الدخول. تحقق من الإيميل وكلمة السر والإنترنت"
                        else if (auth.currentUser?.uid != OWNER_UID) { auth.signOut(); status = "هذا الحساب مو حساب المالك" }
                    }
                }) { Text("دخول") }
            } else {
                Text("التغييرات تنطبق على النسخ المرتبطة بالخدمة عند اتصالها. تغييرات الكود وأيقونة الهاتف تحتاج تحديث APK.")
                Row(verticalAlignment = Alignment.CenterVertically) { Text("تشغيل التطبيق", Modifier.weight(1f)); Switch(draft.enabled, { draft = draft.copy(enabled = it) }, enabled = !busy) }
                OutlinedTextField(draft.title, { draft = draft.copy(title = it.take(80)) }, label = { Text("اسم التطبيق الداخلي") })
                OutlinedTextField(draft.subtitle, { draft = draft.copy(subtitle = it.take(160)) }, label = { Text("العنوان الفرعي") })
                OutlinedTextField(draft.footer, { draft = draft.copy(footer = it.take(160)) }, label = { Text("نص أسفل التطبيق") })
                OutlinedTextField(draft.maintenance, { draft = draft.copy(maintenance = it.take(300)) }, label = { Text("رسالة الإيقاف") })
                OutlinedTextField(draft.announcement, { draft = draft.copy(announcement = it.take(1000)) }, label = { Text("إعلان داخل التطبيق للجميع") })
                Row(verticalAlignment = Alignment.CenterVertically) { Text("حركة الخلفية", Modifier.weight(1f)); Switch(draft.motion, { draft = draft.copy(motion = it) }, enabled = !busy) }
                draft.themeNames.split(',').take(3).forEachIndexed { index, name -> TextButton(onClick = { draft = draft.copy(theme = index) }) { Text((if (draft.theme == index) "✓ " else "") + name) } }
                OutlinedTextField(draft.themeNames, { draft = draft.copy(themeNames = it.take(500)) }, label = { Text("أسماء 3 سمات مفصولة بفاصلة ,") })
                TextButton(enabled = !busy, onClick = { pickLogo = false; picker.launch("image/*") }) { Text("خلفية من جهازي للجميع") }
                TextButton(enabled = !busy, onClick = { pickLogo = true; picker.launch("image/*") }) { Text("شعار داخلي من جهازي للجميع") }
                TextButton(enabled = !busy, onClick = { draft = draft.copy(background = "", logo = "") }) { Text("استرجاع الصور الأصلية للجميع") }
                Text("الإعلان أعلاه داخل التطبيق، مو إشعار هاتف. إرسال إشعار الهاتف متاح من Firebase Messaging بعد موافقة المستخدم.")
                Button(enabled = !busy, onClick = { busy = true; onSave(draft) { ok -> busy = false; status = if (ok) "تم حفظ إعدادات الجميع" else "فشل الحفظ، تحقق من صلاحيات المالك والإنترنت" } }) { Text("حفظ ونشر للجميع") }
                TextButton(enabled = !busy, onClick = { auth.signOut(); onClose() }) { Text("تسجيل خروج المالك") }
            }
            if (busy) CircularProgressIndicator()
            if (status.isNotEmpty()) Text(status)
        }
    }, confirmButton = { TextButton(enabled = !busy, onClick = onClose) { Text("إغلاق") } })
}
