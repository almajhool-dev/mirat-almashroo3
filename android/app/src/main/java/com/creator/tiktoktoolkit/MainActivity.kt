package com.creator.tiktoktoolkit

import android.Manifest
import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.*
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import com.creator.tiktoktoolkit.data.*
import com.creator.tiktoktoolkit.ui.AppTheme
import com.creator.tiktoktoolkit.util.scheduleReminder
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Locale

class CreatorVM(private val db: AppDatabase): ViewModel() {
    val items=db.content().all().stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),emptyList())
    val stats=db.stats().one().stateIn(viewModelScope,SharingStarted.WhileSubscribed(5000),Stats())
    fun add(t:String,s:String)=viewModelScope.launch{if(t.isNotBlank())db.content().add(ContentItem(title=t,script=s,status="فكرة"))}
    fun update(x:ContentItem)=viewModelScope.launch{db.content().update(x)}
    fun delete(x:ContentItem)=viewModelScope.launch{db.content().delete(x)}
    fun save(x:Stats)=viewModelScope.launch{db.stats().save(x)}
}
class MainActivity:ComponentActivity(){
    override fun onCreate(b:Bundle?){
        super.onCreate(b);AppContext.c=this
        val db=AppDatabase.get(this)
        val vm=ViewModelProvider(this,object:ViewModelProvider.Factory{
            override fun <T:ViewModel> create(c:Class<T>)=CreatorVM(db) as T
        }).get(CreatorVM::class.java)
        setContent{AppTheme{CreatorApp(vm)}}
    }
}
object AppContext{lateinit var c:Context}
private data class NavItem(val route:String,val label:String,val icon:String)
private val navItems=listOf(
    NavItem("home","الرئيسية","⌂"),NavItem("ideas","الأفكار","✦"),
    NavItem("content","المحتوى","▣"),NavItem("stats","الإحصائيات","◔"),NavItem("analyzer","تحليل حساب","◎")
)

@Composable fun CreatorApp(vm:CreatorVM){
    val nav=rememberNavController();var dark by remember{mutableStateOf(true)}
    AppTheme(dark){
        Scaffold(
            containerColor=MaterialTheme.colorScheme.background,
            bottomBar={
                NavigationBar(containerColor=MaterialTheme.colorScheme.surface){
                    navItems.forEach{item->
                        val selected=nav.currentBackStackEntryAsState().value?.destination?.route==item.route
                        NavigationBarItem(
                            selected=selected,
                            onClick={nav.navigate(item.route){launchSingleTop=true;restoreState=true}},
                            icon={Text(item.icon,fontSize=21.sp)},label={Text(item.label,fontSize=11.sp)}
                        )
                    }
                }
            }
        ){p->
            NavHost(nav,"home",Modifier.padding(p),enterTransition={fadeIn()},exitTransition={fadeOut()}){
                composable("home"){Home(nav,dark){dark=!dark}};composable("analyzer"){AccountAnalyzer()}
                composable("ideas"){Ideas()};composable("content"){Content(vm)};composable("stats"){StatsPage(vm)}
                composable("hooks"){Gen("مولّد Hooks",listOf("توقف! لازم تشوف هذا قبل ما تسوي سكرول.","3 أشياء تمنع فيديوك من جذب الانتباه."))}
                composable("captions"){Gen("مولّد Captions",listOf("فكرة بسيطة، تنفيذ أقوى. شنو رأيك؟","إذا وصلت لهنا، هذا الفيديو إلك 😄"))}
                composable("hashtags"){Gen("مولّد Hashtags",listOf("#TikTok #Creators #صناعة_المحتوى #محتوى"))}
                composable("tele"){Tele()};composable("video"){Video()};composable("schedule"){Schedule()}
            }
        }
    }
}
@Composable fun Home(n:NavHostController,d:Boolean,t:()->Unit){
    LazyColumn(Modifier.fillMaxSize().padding(horizontal=18.dp),verticalArrangement=Arrangement.spacedBy(14.dp),contentPadding=PaddingValues(top=18.dp,bottom=24.dp)){
        item{
            Row(verticalAlignment=Alignment.CenterVertically,modifier=Modifier.fillMaxWidth()){
                Column(Modifier.weight(1f)){
                    Text("TikTok Creator",style=MaterialTheme.typography.titleMedium,color=MaterialTheme.colorScheme.primary,fontWeight=FontWeight.Bold)
                    Text("Toolkit",style=MaterialTheme.typography.headlineLarge,fontWeight=FontWeight.ExtraBold)
                    Text("أدواتك لصناعة محتوى أفضل، من مكان واحد.",color=MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Surface(shape=RoundedCornerShape(18.dp),color=MaterialTheme.colorScheme.primaryContainer){
                    Text("TC",Modifier.padding(horizontal=14.dp,vertical=12.dp),fontWeight=FontWeight.Black,color=MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
        }
        item{
            Card(shape=RoundedCornerShape(24.dp),colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.primaryContainer)){
                Row(Modifier.fillMaxWidth().padding(18.dp),verticalAlignment=Alignment.CenterVertically){
                    Column(Modifier.weight(1f)){
                        Text("ابدأ بفكرة جديدة",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold)
                        Text("اكتب مجالك وخلي الأداة تقترح عليك أفكاراً قابلة للتنفيذ.",color=MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha=.78f))
                    }
                    Button(onClick={n.navigate("ideas")},shape=RoundedCornerShape(14.dp)){Text("ابدأ")}
                }
            }
        }
        item{Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){
            Text("الأدوات السريعة",style=MaterialTheme.typography.titleLarge,fontWeight=FontWeight.Bold)
            TextButton(onClick=t){Text(if(d)"الوضع الفاتح" else "الوضع الداكن")}
        }}
        item{
            val tools=listOf(
                "✦" to ("أفكار المحتوى" to "ideas"),"⚡" to ("Hooks" to "hooks"),
                "✎" to ("Captions" to "captions"),"#" to ("Hashtags" to "hashtags"),
                "◎" to ("تحليل حساب" to "analyzer"),"▶" to ("تحليل الفيديو" to "video"),"▤" to ("Teleprompter" to "tele"),
                "◷" to ("جدول النشر" to "schedule")
            )
            Column(verticalArrangement=Arrangement.spacedBy(10.dp)){
                tools.chunked(2).forEach{row->
                    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.spacedBy(10.dp)){
                        row.forEach{(icon,data)->ToolCard(icon,data.first){n.navigate(data.second)}}
                        if(row.size==1)Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
        item{
            Surface(shape=RoundedCornerShape(18.dp),color=MaterialTheme.colorScheme.surfaceVariant){
                Text("خصوصيتك أولاً: لا يوجد تسجيل دخول إلى TikTok ولا نشر تلقائي. بياناتك المحلية تبقى داخل التطبيق.",Modifier.padding(14.dp),fontSize=13.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}
@Composable private fun RowScope.ToolCard(icon:String,title:String,onClick:()->Unit){
    Card(onClick=onClick,modifier=Modifier.weight(1f),shape=RoundedCornerShape(20.dp)){
        Column(Modifier.padding(16.dp).heightIn(min=92.dp),verticalArrangement=Arrangement.SpaceBetween){
            Text(icon,fontSize=24.sp,color=MaterialTheme.colorScheme.primary);Text(title,fontWeight=FontWeight.SemiBold);Text("فتح →",fontSize=12.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
@Composable fun Ideas(){
    var x by remember{mutableStateOf("")}
    val list=if(x.isBlank())emptyList() else listOf("3 أشياء ما تعرفها عن $x","جربت $x لمدة 7 أيام — النتيجة","أكبر خطأ للمبتدئ في $x")
    LazyColumn(Modifier.fillMaxSize().padding(18.dp),verticalArrangement=Arrangement.spacedBy(12.dp),contentPadding=PaddingValues(bottom=24.dp)){
        item{Text("مولّد أفكار TikTok",style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Bold);Text("اكتب تخصصك للحصول على أفكار مباشرة.",color=MaterialTheme.colorScheme.onSurfaceVariant)}
        item{OutlinedTextField(x,{x=it},label={Text("مجال حسابك")},singleLine=true,modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(16.dp))}
        items(list){IdeaCard(it)}
    }
}
@Composable fun IdeaCard(title:String){Card(shape=RoundedCornerShape(18.dp)){Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(6.dp)){Text(title,fontWeight=FontWeight.Bold);Text("Hook: توقف لحظة… عندي لك شيء مهم.",color=MaterialTheme.colorScheme.primary);Text("عنوان ووصف مقترحان حسب الموضوع.",color=MaterialTheme.colorScheme.onSurfaceVariant,fontSize=13.sp)}}}
@Composable fun Gen(title:String,examples:List<String>){
    var x by remember{mutableStateOf("")}
    LazyColumn(Modifier.fillMaxSize().padding(18.dp),verticalArrangement=Arrangement.spacedBy(12.dp),contentPadding=PaddingValues(bottom=24.dp)){
        item{Text(title,style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Bold)}
        item{OutlinedTextField(x,{x=it},label={Text("الموضوع")},singleLine=true,modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(16.dp))}
        items(examples){Card(shape=RoundedCornerShape(18.dp)){Text(it,Modifier.padding(16.dp))}}
    }
}
@Composable fun Content(vm:CreatorVM){
    val list by vm.items.collectAsState();var t by remember{mutableStateOf("")};var s by remember{mutableStateOf("")}
    LazyColumn(Modifier.fillMaxSize().padding(18.dp),verticalArrangement=Arrangement.spacedBy(10.dp),contentPadding=PaddingValues(bottom=24.dp)){
        item{Text("منظّم المحتوى",style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Bold);Text("خلي أفكارك مرتبة من الفكرة إلى النشر.",color=MaterialTheme.colorScheme.onSurfaceVariant)}
        item{OutlinedTextField(t,{t=it},label={Text("العنوان")},singleLine=true,modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(16.dp))}
        item{OutlinedTextField(s,{s=it},label={Text("السكربت")},minLines=4,modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(16.dp))}
        item{Button({vm.add(t,s);t="";s=""},enabled=t.isNotBlank(),modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(14.dp)){Text("إضافة إلى المحتوى")}}
        items(list){x->Card(shape=RoundedCornerShape(18.dp)){Column(Modifier.padding(14.dp),verticalArrangement=Arrangement.spacedBy(5.dp)){Text(x.title,fontWeight=FontWeight.Bold);Text(x.status,color=MaterialTheme.colorScheme.primary);Text(x.script,maxLines=3,color=MaterialTheme.colorScheme.onSurfaceVariant);Row{TextButton({vm.update(x.copy(status="قيد الإعداد"))}){Text("إعداد")};TextButton({vm.update(x.copy(status="جاهز"))}){Text("جاهز")};TextButton({vm.delete(x)}){Text("حذف")}}}}}
    }
}
@Composable fun StatsPage(vm:CreatorVM){
    val old by vm.stats.collectAsState();var v by remember(old){mutableStateOf((old?.views ?: 0).toString())};var l by remember(old){mutableStateOf((old?.likes ?: 0).toString())};var c by remember(old){mutableStateOf((old?.comments ?: 0).toString())};var sh by remember(old){mutableStateOf((old?.shares ?: 0).toString())}
    val views=v.toLongOrNull()?:0;val rate=if(views>0)((l.toLongOrNull()?:0)+(c.toLongOrNull()?:0)+(sh.toLongOrNull()?:0)).toDouble()/views*100 else 0.0
    LazyColumn(Modifier.fillMaxSize().padding(18.dp),verticalArrangement=Arrangement.spacedBy(10.dp),contentPadding=PaddingValues(bottom=24.dp)){
        item{Text("إحصائيات شخصية",style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Bold);Text("أدخل أرقامك يدوياً وتابع التفاعل.",color=MaterialTheme.colorScheme.onSurfaceVariant)}
        listOf("Views" to v,"Likes" to l,"Comments" to c,"Shares" to sh).forEach{(a,z)->item{OutlinedTextField(z,{q->when(a){"Views"->v=q;"Likes"->l=q;"Comments"->c=q;"Shares"->sh=q}},label={Text(a)},singleLine=true,modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(14.dp))}}
        item{Card(shape=RoundedCornerShape(20.dp),colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.secondaryContainer)){Column(Modifier.padding(18.dp)){Text("معدل التفاعل");Text(String.format(Locale.US,"%.2f%%",rate),fontSize=30.sp,fontWeight=FontWeight.ExtraBold)}}}
        item{Button({vm.save(Stats(views=views,likes=l.toLongOrNull()?:0,comments=c.toLongOrNull()?:0,shares=sh.toLongOrNull()?:0))},modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(14.dp)){Text("حفظ الإحصائيات")}}
    }
}
@Composable fun Tele(){
    var x by remember{mutableStateOf("")};var size by remember{mutableFloatStateOf(30f)}
    LazyColumn(Modifier.fillMaxSize().padding(18.dp),verticalArrangement=Arrangement.spacedBy(12.dp),contentPadding=PaddingValues(bottom=24.dp)){
        item{Text("Teleprompter",style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Bold)}
        item{OutlinedTextField(x,{x=it},label={Text("السكربت")},minLines=7,modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(16.dp))}
        item{Text("حجم الخط: ${size.toInt()}");Slider(size,{size=it},valueRange=18f..54f)}
        item{Card(shape=RoundedCornerShape(20.dp)){Text(if(x.isBlank())"ضع السكربت هنا…" else x,Modifier.padding(20.dp),fontSize=size.sp,lineHeight=(size*1.35f).sp)}}
    }
}
@Composable fun Video(){
    var out by remember{mutableStateOf("")};val pick=rememberLauncherForActivityResult(ActivityResultContracts.GetContent()){u->if(u!=null)out=readVideo(u)}
    LazyColumn(Modifier.fillMaxSize().padding(18.dp),verticalArrangement=Arrangement.spacedBy(12.dp),contentPadding=PaddingValues(bottom=24.dp)){
        item{Text("محلل الفيديو المحلي",style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Bold);Text("فحص سريع للملف بدون رفعه إلى الإنترنت.",color=MaterialTheme.colorScheme.onSurfaceVariant)}
        item{Button({pick.launch("video/*")},modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(14.dp)){Text("اختيار فيديو")}}
        item{if(out.isNotBlank())Card(shape=RoundedCornerShape(18.dp)){Text(out,Modifier.padding(16.dp))}}
        item{Text("يتم تحليل الملف محلياً فقط؛ لا يتم رفع الفيديو.",fontSize=13.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)}
    }
}
fun readVideo(u:Uri):String=try{MediaMetadataRetriever().run{setDataSource(AppContext.c,u);val d=extractMetadata(9);val w=extractMetadata(18);val h=extractMetadata(19);val b=extractMetadata(20);release();"المدة: "+((d?.toLongOrNull()?:0)/1000)+" ثانية\nالدقة: "+(w?:"?")+"×"+(h?:"?")+"\nBitrate: "+(b?:"غير متاح")}}catch(_:Exception){"تعذر قراءة بيانات الفيديو."}
@Composable fun Schedule(){
    var title by remember{mutableStateOf("")};var min by remember{mutableStateOf("60")};val c=androidx.compose.ui.platform.LocalContext.current
    val permission=rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()){}
    LaunchedEffect(Unit){if(Build.VERSION.SDK_INT>=33 && c.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=0) permission.launch(Manifest.permission.POST_NOTIFICATIONS)}
    Column(Modifier.fillMaxSize().padding(18.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
        Text("جدول النشر",style=MaterialTheme.typography.headlineMedium,fontWeight=FontWeight.Bold);Text("تذكير محلي بموعد التصوير أو النشر.",color=MaterialTheme.colorScheme.onSurfaceVariant)
        OutlinedTextField(title,{title=it},label={Text("عنوان الفيديو")},singleLine=true,modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(16.dp))
        OutlinedTextField(min,{min=it},label={Text("بعد كم دقيقة؟")},singleLine=true,modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(16.dp))
        Button({scheduleReminder(c,if(title.isBlank())"موعد نشر الفيديو" else title,min.toLongOrNull()?:60)},modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(14.dp)){Text("جدولة تذكير محلي")}
        Text("لا يتم النشر تلقائياً على TikTok.",fontSize=13.sp,color=MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
