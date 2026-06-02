import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
}
val localProps = Properties()
localProps.load(rootProject.file("local.properties").inputStream())

val apiBaseUrl = localProps.getProperty(
    "api.base.url",
    "http://10.0.2.2:8080/api/"
)

fun String.asBuildConfigString(): String =
    "\"" + replace("\\", "\\\\").replace("\"", "\\\"") + "\""

android {
    namespace = "com.example.my_mobile_app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.my_mobile_app"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // BASE_URL đọc từ local.properties (không commit), fallback về emulator loopback
        val baseUrl = localProps.getProperty("api.base.url", "http://10.0.2.2:8080/api/")
        buildConfigField("String", "BASE_URL", "\"$baseUrl\"")
        buildConfigField("String", "MOMO_QR_PAYLOAD", localProps.getProperty("momo.qr.payload", "").asBuildConfigString())
        buildConfigField("String", "MOMO_QR_IMAGE_URL", localProps.getProperty("momo.qr.image.url", "").asBuildConfigString())
        buildConfigField("String", "MOMO_QR_ACCOUNT_NAME", localProps.getProperty("momo.qr.account.name", "Lensora Shop").asBuildConfigString())
        buildConfigField("String", "MOMO_QR_ACCOUNT_PHONE", localProps.getProperty("momo.qr.account.phone", "").asBuildConfigString())
    }

    buildFeatures {
        buildConfig = true
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
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // Networking
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.google.zxing:core:3.5.3")

    // Image loading
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    // UI
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.viewpager2:viewpager2:1.1.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
