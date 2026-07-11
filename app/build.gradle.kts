plugins {
    id("com.android.application")
}

android {
    namespace = "com.gptgongjakso.naverhelper"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.gptgongjakso.naverhelper"
        minSdk = 26
        targetSdk = 35
        versionCode = 10002
        versionName = "1.0.2"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
