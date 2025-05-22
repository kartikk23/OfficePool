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
    namespace = "com.agile.officepool"
    compileSdk = 35

    buildFeatures {
        compose = true
        buildConfig = true
    }


    defaultConfig {
        applicationId = "com.agile.officepool"
        minSdk = 30
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        manifestPlaceholders["MAPS_API_KEY"] = mapsApiKey ?: ""
        buildConfigField("String", "MAPS_API_KEY", "\"$mapsApiKey\"")
        buildConfigField("String", "LINKEDIN_CLIENT_ID", "\"${project.findProperty("LINKEDIN_CLIENT_ID")}\"")
        buildConfigField("String", "LINKEDIN_CLIENT_SECRET", "\"${project.findProperty("LINKEDIN_CLIENT_SECRET")}\"")


//    }

    buildTypes {
        debug {
            buildConfigField("String", "MAPS_API_KEY", "\"${mapsApiKey}\"")
        }
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("String", "MAPS_API_KEY", "\"${mapsApiKey}\"")
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
    implementation(libs.scribejava.apis) // For LinkedIn OAuth


    implementation(libs.androidx.material3.v131)
    implementation(libs.accompanist.systemuicontroller)

    implementation (libs.androidx.material3)

    //Google Map
    implementation(libs.maps.compose) // Maps Compose library
    implementation(libs.gms.play.services.maps) // Google Maps SDK
    implementation(libs.androidx.foundation) // Compose foundation
    implementation(libs.androidx.ui.v177)
    implementation(libs.androidx.material.v177) // Compose Material")

    implementation(libs.androidx.material.v100)
    implementation(libs.places.v310)
    implementation(libs.gms.play.services.maps)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.database.ktx) // ✅ Realtime DB
    implementation(libs.kotlinx.coroutines.play.services)


    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.places)
    implementation(libs.protolite.well.known.types)
    implementation(libs.firebase.messaging.ktx)
    implementation(libs.firebase.installations.ktx)
    implementation(libs.androidx.core.ktx.v1120)
    implementation (libs.volley)

}}

