import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.algoviz.plus"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.algoviz.plus"
        minSdk = 24
        targetSdk = 34
        versionCode = 6
        versionName = "1.1.15"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        resourceConfigurations += listOf("en")
    }

    signingConfigs {
        create("release") {
            // Values from local.properties
            val keystorePropertiesFile = rootProject.file("local.properties")
            if (keystorePropertiesFile.exists()) {
                val keystoreProperties = Properties()
                keystoreProperties.load(keystorePropertiesFile.inputStream())
                
                val storeFilePath = keystoreProperties.getProperty("RELEASE_STORE_FILE")
                if (storeFilePath != null) {
                    storeFile = file(storeFilePath)
                }
                storePassword = keystoreProperties.getProperty("RELEASE_STORE_PASSWORD")
                keyAlias = keystoreProperties.getProperty("RELEASE_KEY_ALIAS")
                keyPassword = keystoreProperties.getProperty("RELEASE_KEY_PASSWORD")
            }
        }
        
        create("staging") {
            val keystorePropertiesFile = rootProject.file("local.properties")
            if (keystorePropertiesFile.exists()) {
                val keystoreProperties = Properties()
                keystoreProperties.load(keystorePropertiesFile.inputStream())
                
                val storeFilePath = keystoreProperties.getProperty("STAGING_STORE_FILE")
                if (storeFilePath != null) {
                    storeFile = file(storeFilePath)
                }
                storePassword = keystoreProperties.getProperty("STAGING_STORE_PASSWORD")
                keyAlias = keystoreProperties.getProperty("STAGING_KEY_ALIAS")
                keyPassword = keystoreProperties.getProperty("STAGING_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            isDebuggable = true
            isMinifyEnabled = false
            
            buildConfigField("String", "BASE_URL", "\"https://api-dev.algoviz.app/\"")
            buildConfigField("String", "SUPABASE_URL", "\"https://dev.supabase.co\"")
            buildConfigField("String", "SUPABASE_KEY", "\"\"")
        }
        
        create("staging") {
            initWith(getByName("debug"))
            applicationIdSuffix = ".staging"
            versionNameSuffix = "-staging"
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
            
            signingConfig = signingConfigs.getByName("staging")
            
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            
            buildConfigField("String", "BASE_URL", "\"https://api-staging.algoviz.app/\"")
            buildConfigField("String", "SUPABASE_URL", "\"https://staging.supabase.co\"")
            buildConfigField("String", "SUPABASE_KEY", "\"\"")
        }
        
        release {
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = true
            
            signingConfig = signingConfigs.getByName("release")
            
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            
            buildConfigField("String", "BASE_URL", "\"https://api.algoviz.app/\"")
            buildConfigField("String", "SUPABASE_URL", "\"https://supabase.algoviz.app\"")
            buildConfigField("String", "SUPABASE_KEY", "\"\"")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }

    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += listOf(
            "-opt-in=kotlin.RequiresOptIn",
            "-opt-in=kotlinx.coroutines.ExperimentalCoroutinesApi",
            "-opt-in=kotlinx.coroutines.FlowPreview"
        )
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get()
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "/META-INF/LICENSE.md"
            excludes += "/META-INF/LICENSE-notice.md"
        }
    }

    lint {
        checkReleaseBuilds = false
        abortOnError = false
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

    // Kotlin
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    // Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.bundles.compose)
    implementation(libs.compose.navigation)
    debugImplementation(libs.compose.ui.tooling)
    debugImplementation(libs.compose.ui.test.manifest)

    // Lifecycle
    implementation(libs.bundles.lifecycle)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    kspAndroidTest(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // Logging
    implementation(libs.timber)
    
    // Image Loading
    implementation(libs.coil)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.storage)
    implementation(libs.firebase.firestore)
    implementation(libs.play.services.auth)
    implementation(libs.credentials.play.services.auth)
    implementation(libs.google.id.library)
    implementation(libs.kotlinx.coroutines.play.services)

    // Desugaring
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.4")

    // Testing
    testImplementation(libs.bundles.testing)
    androidTestImplementation(libs.bundles.android.testing)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test.junit4)
}

// Workaround for Android Studio Hilt issues
hilt {
    enableAggregatingTask = true
}
