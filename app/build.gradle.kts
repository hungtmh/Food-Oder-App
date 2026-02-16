plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.food_order_app"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.example.food_order_app"
        minSdk = 29
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    packaging {
        resources {
            excludes += setOf(
                "META-INF/NOTICE.md",
                "META-INF/LICENSE.md",
                "META-INF/NOTICE.txt",
                "META-INF/LICENSE.txt"
            )
        }
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // ViewPager2 for slideshow
    implementation("androidx.viewpager2:viewpager2:1.1.0")
    // CardView
    implementation("androidx.cardview:cardview:1.0.0")

    // Retrofit + OkHttp for Supabase REST API
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.gson)

    // Glide for image loading
    implementation(libs.glide)

    // CircleImageView for profile avatar
    implementation(libs.circleimageview)

    // JavaMail for sending emails
    implementation(libs.android.mail)
    implementation(libs.android.activation)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}