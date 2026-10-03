package com.creator.tiktoktoolkit.data
import androidx.room.*
import kotlinx.coroutines.flow.Flow
@Entity(tableName="content_items") data class ContentItem(@PrimaryKey(autoGenerate=true) val id:Long=0,val title:String,val script:String,val status:String,val createdAt:Long=System.currentTimeMillis())
@Entity(tableName="stats") data class Stats(@PrimaryKey val id:Int=1,val views:Long=0,val likes:Long=0,val comments:Long=0,val shares:Long=0,val followers:Long=0)
@Dao interface ContentDao { @Query("SELECT * FROM content_items ORDER BY createdAt DESC") fun all():Flow<List<ContentItem>>; @Insert suspend fun add(x:ContentItem); @Update suspend fun update(x:ContentItem); @Delete suspend fun delete(x:ContentItem) }
@Dao interface StatsDao { @Query("SELECT * FROM stats WHERE id=1") fun one():Flow<Stats?>; @Insert(onConflict=OnConflictStrategy.REPLACE) suspend fun save(x:Stats) }
@Database(entities=[ContentItem::class,Stats::class],version=1,exportSchema=false) abstract class AppDatabase:RoomDatabase(){abstract fun content():ContentDao;abstract fun stats():StatsDao;companion object{ @Volatile private var i:AppDatabase?=null;fun get(c:android.content.Context)=i?:synchronized(this){i?:Room.databaseBuilder(c,AppDatabase::class.java,"creator.db").build().also{i=it}}}}