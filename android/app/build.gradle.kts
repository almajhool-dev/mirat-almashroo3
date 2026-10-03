plugins { id("com.android.application"); id("org.jetbrains.kotlin.plugin.compose"); id("com.google.devtools.ksp") }
android { namespace="com.creator.tiktoktoolkit"; compileSdk=36
 defaultConfig { applicationId="com.creator.tiktoktoolkit"; minSdk=24; targetSdk=36; versionCode=2; versionName="1.1.0" }
 buildTypes { release { isMinifyEnabled=false; proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"),"proguard-rules.pro") } }
 compileOptions { sourceCompatibility=JavaVersion.VERSION_17; targetCompatibility=JavaVersion.VERSION_17 }; buildFeatures { compose=true }
}
kotlin { jvmToolchain(17) }
dependencies { val bom=platform("androidx.compose:compose-bom:2026.06.00"); implementation(bom)
 implementation("androidx.core:core-ktx:1.17.0"); implementation("androidx.activity:activity-compose:1.11.0"); implementation("androidx.compose.ui:ui"); implementation("androidx.compose.ui:ui-tooling-preview"); debugImplementation("androidx.compose.ui:ui-tooling"); implementation("androidx.compose.material3:material3"); implementation("androidx.navigation:navigation-compose:2.9.5"); implementation("androidx.lifecycle:lifecycle-runtime-compose:2.10.0"); implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.10.0"); implementation("androidx.room:room-runtime:2.8.5"); implementation("androidx.room:room-ktx:2.8.5"); ksp("androidx.room:room-compiler:2.8.5") }