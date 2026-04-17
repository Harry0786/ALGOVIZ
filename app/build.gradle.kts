plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ktlint)
}

import java.util.Properties

val localProperties = Properties().apply {
    val propertiesFile = rootProject.file("local.properties")
    if (propertiesFile.exists()) {
        propertiesFile.inputStream().use(::load)
    }
}

fun readLocalProperty(name: String, defaultValue: String = ""): String {
    return localProperties.getProperty(name, defaultValue)
}

fun toBuildConfigString(value: String): String {
    val escaped = value
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
    return "\"$escaped\""
}

android {
    namespace = "com.algoviz.plus"
    compileSdk = 34

    val releaseStoreFilePath = readLocalProperty("RELEASE_STORE_FILE")
    val hasReleaseSigningConfig = releaseStoreFilePath.isNotBlank() &&
        readLocalProperty("RELEASE_STORE_PASSWORD").isNotBlank() &&
        readLocalProperty("RELEASE_KEY_ALIAS").isNotBlank() &&
        readLocalProperty("RELEASE_KEY_PASSWORD").isNotBlank()

    signingConfigs {
        if (hasReleaseSigningConfig) {
            create("release") {
                storeFile = file(releaseStoreFilePath)
                storePassword = readLocalProperty("RELEASE_STORE_PASSWORD")
                keyAlias = readLocalProperty("RELEASE_KEY_ALIAS")
                keyPassword = readLocalProperty("RELEASE_KEY_PASSWORD")
                enableV1Signing = true
                enableV2Signing = true
            }
        }
    }

    defaultConfig {
        applicationId = "com.algoviz.plus"
        minSdk = 26
        targetSdk = 34
        versionCode = 10
        versionName = "2.0.5"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        vectorDrawables {
            useSupportLibrary = true
        }

        val supabaseKey = readLocalProperty("SUPABASE_KEY", readLocalProperty("SUPABASE_ANON_KEY", ""))
        buildConfigField("String", "SUPABASE_KEY", toBuildConfigString(supabaseKey))
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-DEBUG"
            isDebuggable = true

            val debugSupabaseUrl = readLocalProperty(
                "SUPABASE_URL_DEBUG",
                readLocalProperty("SUPABASE_URL", "https://dev.supabase.co")
            )
            buildConfigField("String", "SUPABASE_URL", toBuildConfigString(debugSupabaseUrl))
        }
        
        create("staging") {
            applicationIdSuffix = ".staging"
            versionNameSuffix = "-STAGING"
            isDebuggable = true
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            val stagingSupabaseUrl = readLocalProperty(
                "SUPABASE_URL_STAGING",
                readLocalProperty("SUPABASE_URL", "https://dev.supabase.co")
            )
            buildConfigField("String", "SUPABASE_URL", toBuildConfigString(stagingSupabaseUrl))
        }
        
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            if (hasReleaseSigningConfig) {
                signingConfig = signingConfigs.getByName("release")
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            val releaseSupabaseUrl = readLocalProperty(
                "SUPABASE_URL_RELEASE",
                readLocalProperty("SUPABASE_URL", "")
            )
            buildConfigField("String", "SUPABASE_URL", toBuildConfigString(releaseSupabaseUrl))
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/gradle/incremental.annotation.processors"
        }
    }
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:ui"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:network"))
    implementation(project(":core:database"))
    implementation(project(":core:datastore"))
    implementation(project(":data"))
    implementation(project(":domain"))
    implementation(project(":features"))
    implementation(project(":features:auth"))

    implementation("androidx.core:core-ktx:1.12.0")
    implementation(platform(libs.compose.bom))
    implementation(libs.bundles.compose)
    implementation(libs.bundles.lifecycle)
    implementation(libs.compose.navigation)
    
    implementation(libs.hilt.android)
    implementation(libs.hilt.navigation.compose)
    ksp(libs.hilt.compiler)

    implementation(libs.bundles.supabase)
    implementation(libs.coil)
    implementation(libs.slf4j.nop)
    
    implementation(libs.timber)
    
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)

    testImplementation(libs.junit)
    testImplementation(libs.coroutines.test)
    androidTestImplementation(libs.junit.ext)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test.junit4)
}
