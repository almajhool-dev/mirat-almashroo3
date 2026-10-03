package com.creator.tiktoktoolkit.util
import android.app.*
import android.content.*
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
const val CHANNEL="publishing"
class ReminderReceiver:BroadcastReceiver(){override fun onReceive(c:Context,i:Intent){val m=c.getSystemService(NotificationManager::class.java);if(Build.VERSION.SDK_INT>=26)m.createNotificationChannel(NotificationChannel(CHANNEL,"تذكيرات النشر",NotificationManager.IMPORTANCE_DEFAULT));if(Build.VERSION.SDK_INT<33||c.checkSelfPermission("android.permission.POST_NOTIFICATIONS")==0)NotificationManagerCompat.from(c).notify((System.currentTimeMillis()%100000).toInt(),NotificationCompat.Builder(c,CHANNEL).setSmallIcon(android.R.drawable.ic_dialog_info).setContentTitle("TikTok Creator Toolkit").setContentText(i.getStringExtra("title")?:"موعد نشر الفيديو").setAutoCancel(true).build())}}
fun scheduleReminder(c:Context,title:String,minutes:Long){val a=c.getSystemService(AlarmManager::class.java);val i=Intent(c,ReminderReceiver::class.java).putExtra("title",title);val p=PendingIntent.getBroadcast(c,(System.currentTimeMillis()%100000).toInt(),i,PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE);a.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,System.currentTimeMillis()+minutes.coerceAtLeast(1)*60000,p)}