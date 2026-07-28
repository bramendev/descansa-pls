plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.bramen.descanso"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.bramen.descanso"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        // versionName lo pasa el CI desde el tag git (-Pvname=v1.2); "dev" en local.
        versionName = (project.findProperty("vname") as String?) ?: "dev"
    }

    buildTypes {
        release { isMinifyEnabled = false }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}
