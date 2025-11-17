plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.yourcompany.mainapp3"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.yourcompany.mainapp3"
        minSdk = 29  // UE AAR 实际要求最低 29
        targetSdk = 34  // 保持兼容性
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // 只支持 arm64-v8a 架构
        ndk {
            abiFilters += listOf("arm64-v8a")
        }
        
        // 启用 MultiDex
        multiDexEnabled = true
    }

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
    buildFeatures {
        viewBinding = true
    }
    
    packaging {
        resources {
            pickFirsts += listOf(
                "lib/arm64-v8a/libUnreal.so",
                "lib/arm64-v8a/libpsoservice.so",
                "lib/arm64-v8a/libswappy.so"
            )
        }
    }
}

dependencies {
    // ==================== 虚幻引擎 AAR ====================
    implementation(files("libs/ue-library.aar"))
    
    // UE AAR 必需的依赖
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")
    annotationProcessor("androidx.lifecycle:lifecycle-compiler:2.6.1")
    
    // MultiDex 支持
    implementation("androidx.multidex:multidex:2.0.1")
    
    // Google Play Services (UE AAR 需要)
    implementation("com.google.android.gms:play-services-base:18.5.0")
    implementation("com.google.android.gms:play-services-auth:21.2.0")
    implementation("com.google.android.gms:play-services-games-v2:20.1.2")
    
    // ==================== 原有依赖 ====================
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}