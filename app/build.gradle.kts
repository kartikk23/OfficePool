import com.android.manifmerger.Actions.load
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.gms)

}

val localProperties = Properties().apply {
    load(rootProject.file("local.properties").inputStream())
}

val mapsApiKey: String? = localProperties.getProperty("GOOGLE_MAPS_API_KEY")

android {
    defaultConfig {
        manifestPlaceholders["MAPS_API_KEY"] = mapsApiKey ?: ""
    }
}

android {
    namespace = "com.agile.officepool"
    compileSdk = 35

    buildFeatures {
        compose = true
        buildConfig = true
    }
    defaultConfig {
        buildConfigField("String", "MAPS_API_KEY", "\"$mapsApiKey\"")
    }

    defaultConfig {
        applicationId = "com.agile.officepool"
        minSdk = 30
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "LINKEDIN_CLIENT_ID", "\"${project.findProperty("LINKEDIN_CLIENT_ID")}\"")
        buildConfigField("String", "LINKEDIN_CLIENT_SECRET", "\"${project.findProperty("LINKEDIN_CLIENT_SECRET")}\"")


//    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }

}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    implementation(libs.ui)
    implementation(libs.androidx.material)
    implementation(libs.ui.tooling.preview)
    implementation(libs.androidx.lifecycle.runtime.ktx.v262)
    implementation(libs.androidx.activity.compose.v180)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.gson)
    implementation("com.github.scribejava:scribejava-apis:8.3.3") // For LinkedIn OAuth


    implementation("androidx.compose.material3:material3:1.3.1")
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.34.0")

    implementation ("androidx.compose.material3:material3:1.3.2")

    //Google Map
    implementation("com.google.maps.android:maps-compose:6.4.1") // Maps Compose library
    implementation("com.google.android.gms:play-services-maps:19.0.0") // Google Maps SDK
    implementation("androidx.compose.foundation:foundation:1.7.7") // Compose foundation
    implementation("androidx.compose.ui:ui:1.7.7")
    implementation("androidx.compose.material:material:1.7.7") // Compose Material")

    implementation(libs.androidx.material.v100)
    implementation(libs.places.v310)
    implementation(libs.gms.play.services.maps)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)

}}
dependencies {
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.places)
    implementation(libs.protolite.well.known.types)
    implementation(libs.firebase.messaging.ktx)
    implementation(libs.androidx.core.ktx.v1120)

}
