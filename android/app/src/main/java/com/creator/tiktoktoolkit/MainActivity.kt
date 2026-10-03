package com.creator.tiktoktoolkit

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.*
import androidx.navigation.compose.*
import com.creator.tiktoktoolkit.data.*
import com.creator.tiktoktoolkit.ui.AppTheme
import com.creator.tiktoktoolkit.util.scheduleReminder
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CreatorVM(private val db: AppDatabase): ViewModel() {
    val items=db.content().all().stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),emptyList())
    val stats=db.stats().one().stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),Stats())
    fun add(t:String,s:String)=viewModelScope.launch{if(t.isNotBlank())db.content().add(ContentItem(title=t,script=s,status="فكرة"))}
    fun update(x:ContentItem)=viewModelScope.launch{db.content().update(x)}
    fun delete(x:ContentItem)=viewModelScope.launch{db.content().delete(x)}
    fun save(x:Stats)=viewModelScope.launch{db.stats().save(x)}
}
class MainActivity:ComponentActivity(){
    override fun onCreate(b:Bundle?){super.onCreate(b);AppContext.c=this;val db=AppDatabase.get(this)
        val vm=ViewModelProvider(this,object:ViewModelProvider.Factory{
            override fun <T:ViewModel> create(c:Class<T>)=CreatorVM(db) as T
        }).get(CreatorVM::class.java)
        setContent{AppTheme{CreatorApp(vm)}}
    }
}
object AppContext{lateinit var c:Context}

@Composable fun CreatorApp(vm:CreatorVM){
    val nav=rememberNavController();var dark by remember{mutableStateOf(true)}
    AppTheme(dark){Scaffold(bottomBar={NavigationBar{
        listOf("home" to "الرئيسية","ideas" to "الأفكار","content" to "المحتوى","stats" to "الإحصائيات").forEach{(r,l)->
            NavigationBarItem(selected=false,onClick={nav.navigate(r){launchSingleTop=true}},icon={Text("•")},label={Text(l)})
        }
    }}){p->NavHost(nav,"home",Modifier.padding(p)){
        composable("home"){Home(nav,dark){dark=!dark}}
        composable("ideas"){Ideas()}
        composable("content"){Content(vm)}
        composable("stats"){StatsPage(vm)}
        composable("hooks"){Gen("مولّد Hooks",listOf("توقف! لازم تشوف هذا قبل ما تسوي سكرول.","3 أشياء تمنع فيديوك من جذب الانتباه."))}
        composable("captions"){Gen("مولّد Captions",listOf("فكرة بسيطة، تنفيذ أقوى. شنو رأيك؟","إذا وصلت لهنا، هذا الفيديو إلك 😄"))}
        composable("hashtags"){Gen("مولّد Hashtags",listOf("#TikTok #Creators #صناعة_المحتوى #محتوى"))}
        composable("tele"){Tele()}
        composable("video"){Video()}
        composable("schedule"){Schedule()}
    }}}
}

@Composable fun Home(n:NavHostController,d:Boolean,t:()->Unit){
    LazyColumn(Modifier.padding(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
        item{Text("TikTok Creator Toolkit",style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Bold);Text("أدوات مستقلة لصانع المحتوى — بدون تسجيل دخول إلى TikTok.")}
        item{Row(horizontalArrangement=Arrangement.spacedBy(8.dp)){Button({n.navigate("ideas")}){Text("✦ أفكار")};OutlinedButton(t){Text(if(d)"☀ فاتح" else "☾ داكن")}}}
        item{Text("الأدوات",style=MaterialTheme.typography.titleLarge)}
        listOf("🎯 مولّد الأفكار" to "ideas","⚡ مولّد Hooks" to "hooks","✍️ مولّد Captions" to "captions","#️⃣ مولّد Hashtags" to "hashtags","🎬 محلل الفيديو" to "video","📜 Teleprompter" to "tele","🗓 جدول النشر" to "schedule").forEach{(a,r)->item{Card(onClick={n.navigate(r)}){Column(Modifier.padding(16.dp)){Text(a,fontWeight=FontWeight.Bold);Text("فتح الأداة →",color=MaterialTheme.colorScheme.primary)}}}}
    }
}
@Composable fun Ideas(){
    var x by remember{mutableStateOf("")};val list=if(x.isBlank())emptyList() else listOf("3 أشياء ما تعرفها عن "+x,"جربت "+x+" لمدة 7 أيام — النتيجة","أكبر خطأ للمبتدئ في "+x)
    LazyColumn(Modifier.padding(20.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){item{Text("مولّد أفكار TikTok",style=MaterialTheme.typography.headlineSmall)};item{OutlinedTextField(x,{x=it},label={Text("مجال حسابك")},modifier=Modifier.fillMaxWidth())};items(list){IdeaCard(it)}}}
@Composable fun IdeaCard(title:String){Card{Column(Modifier.padding(16.dp)){Text(title,fontWeight=FontWeight.Bold);Text("Hook: توقف لحظة… عندي لك شيء مهم.");Text("عنوان ووصف مقترحان حسب الموضوع.")}}}
@Composable fun Gen(title:String,examples:List<String>){var x by remember{mutableStateOf("")};LazyColumn(Modifier.padding(20.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){item{Text(title,style=MaterialTheme.typography.headlineSmall)};item{OutlinedTextField(x,{x=it},label={Text("الموضوع")},modifier=Modifier.fillMaxWidth())};items(examples){Card{Text(it,Modifier.padding(16.dp))}}}}
@Composable fun Content(vm:CreatorVM){val list by vm.items.collectAsState();var t by remember{mutableStateOf("")};var s by remember{mutableStateOf("")};LazyColumn(Modifier.padding(20.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){item{Text("منظّم المحتوى",style=MaterialTheme.typography.headlineSmall)};item{OutlinedTextField(t,{t=it},label={Text("العنوان")},modifier=Modifier.fillMaxWidth())};item{OutlinedTextField(s,{s=it},label={Text("السكربت")},minLines=4,modifier=Modifier.fillMaxWidth())};item{Button({vm.add(t,s);t="";s=""},enabled=t.isNotBlank()){Text("حفظ")}};items(list){x->Card{Column(Modifier.padding(12.dp)){Text(x.title,fontWeight=FontWeight.Bold);Text(x.status);Text(x.script);Row{TextButton({vm.update(x.copy(status="قيد الإعداد"))}){Text("إعداد")};TextButton({vm.update(x.copy(status="جاهز"))}){Text("جاهز")};TextButton({vm.delete(x)}){Text("حذف")}}}}}}}
@Composable fun StatsPage(vm:CreatorVM){val old by vm.stats.collectAsState();var v by remember{mutableStateOf(old.views.toString())};var l by remember{mutableStateOf(old.likes.toString())};var c by remember{mutableStateOf(old.comments.toString())};var sh by remember{mutableStateOf(old.shares.toString())};val views=v.toLongOrNull()?:0;val rate=if(views>0)((l.toLongOrNull()?:0)+(c.toLongOrNull()?:0)+(sh.toLongOrNull()?:0)).toDouble()/views*100 else 0.0;LazyColumn(Modifier.padding(20.dp),verticalArrangement=Arrangement.spacedBy(8.dp)){item{Text("إحصائيات شخصية",style=MaterialTheme.typography.headlineSmall)};listOf("Views" to v,"Likes" to l,"Comments" to c,"Shares" to sh).forEach{(a,z)->item{OutlinedTextField(z,{q->when(a){"Views"->v=q;"Likes"->l=q;"Comments"->c=q;"Shares"->sh=q}},label={Text(a)},modifier=Modifier.fillMaxWidth())}};item{Text("Engagement Rate: "+String.format("%.2f",rate)+"%",style=MaterialTheme.typography.titleLarge)};item{Button({vm.save(Stats(views=views,likes=l.toLongOrNull()?:0,comments=c.toLongOrNull()?:0,shares=sh.toLongOrNull()?:0))}){Text("حفظ")}}}}
@Composable fun Tele(){var x by remember{mutableStateOf("")};var size by remember{mutableFloatStateOf(30f)};LazyColumn(Modifier.padding(20.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){item{Text("Teleprompter",style=MaterialTheme.typography.headlineSmall)};item{OutlinedTextField(x,{x=it},label={Text("السكربت")},minLines=7,modifier=Modifier.fillMaxWidth())};item{Text("حجم الخط: "+size.toInt());Slider(size,{size=it},valueRange=18f..54f)};item{Card{Text(if(x.isBlank())"ضع السكربت هنا…" else x,Modifier.padding(20.dp),fontSize=size.sp)}}}}
@Composable fun Video(){var out by remember{mutableStateOf("")};val pick=rememberLauncherForActivityResult(ActivityResultContracts.GetContent()){u->if(u!=null)out=readVideo(u)};LazyColumn(Modifier.padding(20.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){item{Text("محلل الفيديو المحلي",style=MaterialTheme.typography.headlineSmall)};item{Button({pick.launch("video/*")}){Text("اختيار فيديو")}};item{Text(out)};item{Text("يتم تحليل الملف محلياً فقط؛ لا يتم رفع الفيديو.")}}}
fun readVideo(u:Uri):String=try{MediaMetadataRetriever().run{setDataSource(AppContext.c,u);val d=extractMetadata(9);val w=extractMetadata(18);val h=extractMetadata(19);val b=extractMetadata(20);release();"المدة: "+((d?.toLongOrNull()?:0)/1000)+" ثانية\nالدقة: "+(w?:"?")+"×"+(h?:"?")+"\nBitrate: "+(b?:"غير متاح")}}catch(_:Exception){"تعذر قراءة بيانات الفيديو."}
@Composable fun Schedule(){var title by remember{mutableStateOf("")};var min by remember{mutableStateOf("60")};val c=androidx.compose.ui.platform.LocalContext.current;Column(Modifier.padding(20.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){Text("جدول النشر",style=MaterialTheme.typography.headlineSmall);OutlinedTextField(title,{title=it},label={Text("عنوان الفيديو")},modifier=Modifier.fillMaxWidth());OutlinedTextField(min,{min=it},label={Text("بعد كم دقيقة؟")},modifier=Modifier.fillMaxWidth());Button({scheduleReminder(c,if(title.isBlank())"موعد نشر الفيديو" else title,min.toLongOrNull()?:60)}){Text("جدولة تذكير محلي")};Text("لا يتم النشر تلقائياً على TikTok.")}}
