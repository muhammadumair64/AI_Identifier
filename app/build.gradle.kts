plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)

    id("com.google.devtools.ksp")
    id("com.google.dagger.hilt.android")
    id("com.google.gms.google-services")
    id ("com.google.firebase.crashlytics")
    id("com.google.firebase.firebase-perf")

}

android {
    namespace = "com.iobits.tech.app.ai_identifier"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.object.identifier.identify.anything.plant"
        minSdk = 24
        targetSdk = 35
        versionCode = 21
        versionName = "3.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            //applovin
            resValue("string", "AppLovinInterstitial", "YOUR_AD_UNIT_ID")
            resValue("string", "AppLovinBanner", "YOUR_AD_UNIT_ID")
            resValue("string", "APPLOVIN_NATIVE_SMALL_LIST", "YOUR_AD_UNIT_ID")
            resValue("string", "APPLOVIN_NATIVE_MEDIUM_LIST", "YOUR_AD_UNIT_ID")
            resValue("string", "APP_LOVIN_SMALL_NATIVE_AD", "YOUR_AD_UNIT_ID")
            resValue("string", "APP_LOVIN_MEDIUM_NATIVE_AD", "YOUR_AD_UNIT_ID")

            //admob app id
            resValue("string", "admob_app_id", "ca-app-pub-9262123212469470~9266639013")

            resValue("string", "ADMOB_BANNER_V2", "ca-app-pub-9262123212469470/3426793434")
            resValue("string", "ADMOD_BANNER_COLLAPSIBLE", "ca-app-pub-9262123212469470/3426793434")
            resValue("string", "ADMOD_OPEN_AD", "ca-app-pub-9262123212469470/7828663752")
            resValue ("string", "ADMOB_INTERSTITIAL_V2_SPLASH", "ca-app-pub-9262123212469470/9798257525")
            resValue("string", "ADMOB_INTERSTITIAL_V2", "ca-app-pub-9262123212469470/9075067323")
            resValue( // for small native media ads
                "string",
                "ADMOB_NATIVE_WITHOUT_MEDIA_V2",
                "ca-app-pub-9262123212469470/9617222293"
            )



            resValue(
                "string",
                "ADMOB_NATIVE_WITHOUT_MEDIA_V2_HISTORY",
                "ca-app-pub-9262123212469470/9158978132"
            )
            resValue(
                "string",
                "ADMOB_NATIVE_WITHOUT_MEDIA_V2_HOME",
                "ca-app-pub-9262123212469470/1620233616"
            )
            resValue(
                "string",
                "ADMOB_NATIVE_WITHOUT_MEDIA_V2_R_IDENT",
                "ca-app-pub-9262123212469470/3031719330"
            )


            resValue(// for large native media ads
                "string",
                "ADMOB_NATIVE_WITH_MEDIA_V2",
                "ca-app-pub-9262123212469470/4776368277"
            )
            resValue("string", "ADMOB_REWARD_VIDEO", "ca-app-pub-9262123212469470/1930303966")
            resValue(
                "string",
                "ADMOB_REWARD_INTERSTITIAL_V2",
                "ca-app-pub-9262123212469470/1930303966"
            )

        }

        debug {
//            isMinifyEnabled = true
//            isShrinkResources = true
//            proguardFiles(
//                getDefaultProguardFile("proguard-android-optimize.txt"),
//                "proguard-rules.pro"
//            )
            //applovin
            resValue("string", "AppLovinInterstitial", "YOUR_AD_UNIT_ID")
            resValue("string", "AppLovinBanner", "YOUR_AD_UNIT_ID")
            resValue("string", "APPLOVIN_NATIVE_SMALL_LIST", "YOUR_AD_UNIT_ID")
            resValue("string", "APPLOVIN_NATIVE_MEDIUM_LIST", "YOUR_AD_UNIT_ID")
            resValue("string", "APP_LOVIN_SMALL_NATIVE_AD", "YOUR_AD_UNIT_ID")
            resValue("string", "APP_LOVIN_MEDIUM_NATIVE_AD", "YOUR_AD_UNIT_ID")

            //admob app id
            resValue("string", "admob_app_id", "ca-app-pub-3940256099942544~3347511713")

            resValue("string", "ADMOB_BANNER_V2", "ca-app-pub-3940256099942544/6300978111")
            resValue("string", "ADMOD_BANNER_COLLAPSIBLE", "ca-app-pub-3940256099942544/2014213617")
            resValue("string", "ADMOD_OPEN_AD", "ca-app-pub-3940256099942544/9257395921")
            resValue ("string", "ADMOB_INTERSTITIAL_V2_SPLASH", "ca-app-pub-3940256099942544/1033173712")
            resValue("string", "ADMOB_INTERSTITIAL_V2", "ca-app-pub-3940256099942544/1033173712")
            resValue(
                "string",
                "ADMOB_NATIVE_WITHOUT_MEDIA_V2",
                "ca-app-pub-3940256099942544/2247696110"
            )

            resValue(
                "string",
                "ADMOB_NATIVE_WITHOUT_MEDIA_V2_HISTORY",
                "ca-app-pub-3940256099942544/2247696110"
            )
            resValue(
                "string",
                "ADMOB_NATIVE_WITHOUT_MEDIA_V2_HOME",
                "ca-app-pub-3940256099942544/2247696110"
            )
            resValue(
                "string",
                "ADMOB_NATIVE_WITHOUT_MEDIA_V2_R_IDENT",
                "ca-app-pub-3940256099942544/2247696110"
            )

            resValue(
                "string",
                "ADMOB_NATIVE_WITH_MEDIA_V2",
                "ca-app-pub-3940256099942544/2247696110"
            )
            resValue("string", "ADMOB_REWARD_VIDEO", "ca-app-pub-3940256099942544/5224354917")
            resValue(
                "string",
                "ADMOB_REWARD_INTERSTITIAL_V2",
                "ca-app-pub-3940256099942544/5354046379"
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
        dataBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    //Modules
    implementation(project(":ucrop"))


    //room
    val roomVersion = "2.6.1"
//    ksp("org.jetbrains.kotlinx:kotlinx-metadata-jvm:0.6.0")
    implementation(libs.androidx.room.ktx)
    implementation(libs.androidx.room.runtime)
    //noinspection kspUsageInsteadOfKsp
    ksp("androidx.room:room-compiler:$roomVersion")
    val lifecycleVersion = "2.8.6"
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:$lifecycleVersion")
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:$lifecycleVersion")
    implementation(libs.androidx.lifecycle.viewmodel.savedstate)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.glide)
    implementation(libs.androidx.activity)

    // hilt
    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.navigation.fragment)
    ksp(libs.hilt.android.compiler)
    implementation(libs.androidx.lifecycle.extensions)
    implementation(libs.androidx.hilt.navigation.fragment)

    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.navigation.fragment)
    implementation(libs.androidx.navigation.ui)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.fragment.ktx)

    // Kotlin
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    // sdp
    implementation(libs.sdp.android)

    // bubble navigation & lottie
    implementation(libs.bubbletabbar)
    implementation(libs.lottie)

// CameraX core library using camera2 implementation
    implementation(libs.androidx.camera.camera2)
// CameraX Lifecycle Library
    implementation(libs.androidx.camera.lifecycle)
// CameraX View class
    implementation(libs.androidx.camera.view)

    // ML Kit
    implementation (libs.android.gif.drawable)
    implementation (libs.image.labeling)
    //noinspection UseTomlInstead
    implementation ("com.google.mlkit:object-detection:17.0.2")


    //shimmer effect for images
    implementation (libs.shimmer)

    // for gemini
    implementation(libs.generativeai)
//    check this could be harmful implementation ("io.noties.markwon:core:4.6.2")

    //sample rating bar
    implementation ("com.github.ome450901:SimpleRatingBar:1.5.1"){
        exclude(group = "com.some.conflicting.group")
    }

    //for dot indicator
    implementation(libs.dotsindicator)


    // retrofit for networking
    implementation(libs.retrofit)
    implementation(libs.retrofit2.kotlin.coroutines.adapter)
    implementation(libs.converter.moshi)
    implementation(libs.logging.interceptor)
    implementation(libs.converter.gson)

    // moshi for parsing the JSON format
    implementation(libs.moshi)
    implementation(libs.moshi.kotlin)
    ksp(libs.moshi.kotlin.codegen)
    implementation(libs.moshi.adapters)

    // SubsamplingScaleImageView
    implementation (libs.subsampling.scale.image.view.androidx)

    /**
     * firebase
     */
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics.ktx)
    implementation(libs.firebase.analytics.ktx)
    implementation(libs.firebase.messaging.ktx)
    implementation(libs.firebase.config.ktx)
    implementation(libs.firebase.perf)

    //billing
    implementation(libs.billing)
    implementation("com.google.guava:listenablefuture:9999.0-empty-to-avoid-conflict-with-guava")
    implementation("com.google.guava:guava:32.0.1-jre")

    /**
     * Managers
     */
    //ads
    implementation(libs.play.services.ads)
    implementation(libs.user.messaging.platform)
    implementation(libs.consent.library)
    implementation(libs.timber)
    implementation (libs.vungle)
    implementation (libs.vungle.ads)
    implementation(libs.facebook)
    implementation(libs.mintegral)
    implementation(libs.applovin)

}