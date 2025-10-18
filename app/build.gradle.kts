plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
    id("kotlin-parcelize")
    id("androidx.navigation.safeargs.kotlin")
}

android {
    namespace = "com.example.stratify"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.stratify"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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

    sourceSets{
        getByName("main"){
            java.srcDirs("src/main/java", "src/scrum_section/java")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }


    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.auth)
    implementation(libs.firebase.storage)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.recyclerview)
    implementation(libs.glide)

    // Dependensi untuk testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    //foto bulat
    implementation("de.hdodenhof:circleimageview:3.1.0")

    implementation("com.google.code.gson:gson:2.10.1")

    implementation(libs.philjay.mpandroidchart)
    implementation("androidx.slice:slice-builders:1.0.0")

}

// This is a harmless comment to trigger a project rebuild and clear the cache.