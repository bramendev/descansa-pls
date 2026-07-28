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
        // Ambos los pasa el CI: el nombre desde el tag git (-Pvname=v1.4) y el
        // código desde el número de ejecución, que siempre sube. En local, 1/"dev".
        versionCode = (project.findProperty("vcode") as String?)?.toIntOrNull() ?: 1
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
