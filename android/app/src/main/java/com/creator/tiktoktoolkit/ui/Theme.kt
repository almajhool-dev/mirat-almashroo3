package com.creator.tiktoktoolkit.ui
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
private val D=darkColorScheme(primary=Color(0xFFFF3B81),secondary=Color(0xFF7C4DFF),background=Color(0xFF0B0B12),surface=Color(0xFF15151F))
private val L=lightColorScheme(primary=Color(0xFFD81B60),secondary=Color(0xFF5E35B1))
@Composable fun AppTheme(dark:Boolean=true,content:@Composable()->Unit){MaterialTheme(colorScheme=if(dark)D else L,content=content)}