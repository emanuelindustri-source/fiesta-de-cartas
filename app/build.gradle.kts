import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.serialization")
}

val keystorePropsFile = rootProject.file("keystore.properties")
val keystoreProps = Properties().apply {
    if (keystorePropsFile.exists()) keystorePropsFile.inputStream().use { load(it) }
}

val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}

val honeygainApiKey: String =
    (localProps.getProperty("honeygain.apiKey") ?: System.getenv("HONEYGAIN_API_KEY") ?: "").trim()

fun signingValue(key: String, env: String): String? =
    keystoreProps.getProperty(key)?.takeIf { it.isNotBlank() } ?: System.getenv(env)?.takeIf { it.isNotBlank() }

val hasReleaseSigning = signingValue("storeFile", "RELEASE_STORE_FILE") != null

android {
    namespace = "com.fiestadecartas.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.fiestadecartas.app"
        minSdk = 24
        targetSdk = 34
        versionCode = 3
        versionName = "1.2.0"
        resourceConfigurations += listOf("es", "en", "pt", "it", "zh")
        vectorDrawables.useSupportLibrary = true
        buildConfigField("String", "HONEYGAIN_API_KEY", "\"${honeygainApiKey.replace("\\", "\\\\").replace("\"", "\\\"")}\"")
    }

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = rootProject.file(signingValue("storeFile", "RELEASE_STORE_FILE")!!)
                storePassword = signingValue("storePassword", "RELEASE_STORE_PASSWORD")
                keyAlias = signingValue("keyAlias", "RELEASE_KEY_ALIAS")
                keyPassword = signingValue("keyPassword", "RELEASE_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            isShrinkResources = false
            if (hasReleaseSigning) signingConfig = signingConfigs.getByName("release")
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    composeOptions { kotlinCompilerExtensionVersion = "1.5.14" }
    packaging {
        resources.excludes += setOf("/META-INF/{AL2.0,LGPL2.1}", "META-INF/DEPENDENCIES")
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.06.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    debugImplementation("androidx.compose.ui:ui-tooling")

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    implementation("com.google.zxing:core:3.5.3")
    implementation("androidx.camera:camera-camera2:1.3.4")
    implementation("androidx.camera:camera-lifecycle:1.3.4")
    implementation("androidx.camera:camera-view:1.3.4")
    implementation("com.google.mlkit:barcode-scanning:17.2.0")
    implementation("com.google.guava:guava:32.1.3-android")
    implementation("androidx.palette:palette-ktx:1.0.0")

    implementation("com.google.android.gms:play-services-ads:23.1.0")

    // Optional Honeygain SDK. The AAR is proprietary and cannot be committed or
    // distributed via Google Play. Drop it in app/libs/ and set honeygain.apiKey
    // in local.properties to enable the support programme.
    val honeygainAar = file("libs/hgsdk.aar")
    if (honeygainAar.exists()) {
        implementation(files(honeygainAar))
        implementation("org.msgpack:msgpack-core:0.9.11")
        implementation("com.google.protobuf:protobuf-javalite:3.25.3")
        implementation("com.squareup.okhttp3:okhttp:4.12.0")
    }
}
