import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.cw.remote.download.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.cw.remote.download.app"
        minSdk = 24
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        create("release") {
            keyAlias = "key0"
            keyPassword = "2005120210"
            storeFile = file("../KeyStore.jks")
            storePassword = "2005120210"
        }
        getByName("debug") {
            keyAlias = "key0"
            keyPassword = "2005120210"
            storeFile = file("../KeyStore.jks")
            storePassword = "2005120210"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }

    androidComponents.onVariants {
        it.outputs
            // default type don't have outputFileName field
            .all { output ->
                val dateFormat = SimpleDateFormat("yyyy-MM-dd HHmmss", Locale.getDefault())
                val formattedDate = dateFormat.format(Date())
                val fileName = "文件下载_v${defaultConfig.versionName}(${defaultConfig.versionCode})_${formattedDate}_${it.buildType.toString()}.apk"
                output.outputFileName = fileName
                false
            }
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/INDEX.LIST"
            excludes += "/META-INF/io.netty.versions.properties"
        }
    }
    buildFeatures {
        dataBinding = true
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.splashscreen)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.longan)
    implementation(libs.eventbus)
    implementation(libs.permissionx)
    implementation(libs.zxing.core)
    implementation(libs.ktor.server.core)
    implementation(libs.ktor.server.netty)
}
