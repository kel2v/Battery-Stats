plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.devtools.ksp)
    alias(libs.plugins.google.dagger.hilt.android)
    alias(libs.plugins.room)
}

android {
    namespace = "com.bytemanager.stats"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.bytemanager.stats"
        minSdk = 28
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "com.bytemanager.stats.fakeImplementations.hilt.HiltTestRunner"
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

    buildFeatures {
        compose = true
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}

dependencies {
    // composeBom
    implementation(platform(libs.androidx.compose.bom))

    // core
    implementation(libs.core.ktx)

    // androidx
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.junit.ktx)
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.work.testing)
    implementation(libs.androidx.hilt.work)
    implementation(libs.androidx.runner)

    // hilt
    ksp(libs.hilt.android.compiler)
    implementation(libs.hilt.android)
    ksp(libs.androidx.hilt.compiler)


    // room
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.room.runtime)

    // vico
    implementation(libs.patrykandpatrick.vico.compose)

    // test: Android
    testImplementation(libs.core.ktx)

    // test: Hilt
    testImplementation(libs.hilt.android.testing)
    ksp(libs.hilt.compiler)

    // test: Junit
    testImplementation(libs.junit)
    testImplementation(libs.junit.vintage.engine)
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)

    // test: Kotlin-coroutines
    testImplementation(libs.kotlinx.coroutines.test)

    // test: robolectric
    testImplementation(libs.robolectric)

    // test: kotlin
    testImplementation(kotlin("test"))

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.work.testing)
    androidTestImplementation(libs.hilt.android.testing)
    kspAndroidTest(libs.hilt.android.compiler)

    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}