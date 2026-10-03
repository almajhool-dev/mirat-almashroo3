package com.creator.tiktoktoolkit.ui

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Dark = darkColorScheme(
    primary=Color(0xFFFF4F8B),onPrimary=Color(0xFF3D001B),primaryContainer=Color(0xFF6A163E),
    onPrimaryContainer=Color(0xFFFFD9E6),secondary=Color(0xFFB9A7FF),secondaryContainer=Color(0xFF3C3267),
    background=Color(0xFF08090D),surface=Color(0xFF101218),surfaceVariant=Color(0xFF1A1D25)
)
private val Light = lightColorScheme(
    primary=Color(0xFFB41458),primaryContainer=Color(0xFFFFD9E6),onPrimaryContainer=Color(0xFF3D001B),
    secondary=Color(0xFF5D43A8),secondaryContainer=Color(0xFFE8DEFF),background=Color(0xFFF9F7FA),
    surface=Color(0xFFFFFFFF),surfaceVariant=Color(0xFFF0EDF2)
)
@Composable
fun AppTheme(dark:Boolean=true,content:@Composable()->Unit){
    MaterialTheme(colorScheme=if(dark)Dark else Light,content=content)
}
