plugins {
  id(GradlePluginId.ANDROID_LIBRARY)
  id(GradlePluginId.KOTLIN_ANDROID)
  id(GradlePluginId.KOTLIN_KAPT)
  id(GradlePluginId.HILT_PLUGIN)
}

android {
  compileSdkVersion(AndroidConfig.COMPILE_SDK)

  defaultConfig {
    minSdkVersion(AndroidConfig.MIN_SDK)
    targetSdkVersion(AndroidConfig.TARGET_SDK)
    testInstrumentationRunner = AndroidConfig.TEST_INSTRUMENTATION_RUNNER
  }

  buildTypes {
    getByName(BuildType.RELEASE) {
      isMinifyEnabled = BuildTypeRelease.isMinifyEnabled
      proguardFiles("proguard-android.txt", "proguard-rules.pro")
    }
    getByName(BuildType.DEBUG) {
      isMinifyEnabled = BuildTypeDebug.isMinifyEnabled
    }
  }

  buildFeatures {
    // Enables Jetpack Compose for this module
    compose = true
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
  }
  kotlinOptions {
    jvmTarget = JavaVersion.VERSION_1_8.toString()
  }
  composeOptions {
    kotlinCompilerExtensionVersion = CoreVersion.KT_COMPILER_EXTENSION
  }
}

dependencies {

  implementation(LibraryDependency.CORE_KTX)
  implementation(LibraryDependency.APP_COMPAT)
  implementation(LibraryDependency.MATERIAL)
  implementation(LibraryDependency.CONSTRAINT_LAYOUT)
  implementation(LibraryDependency.KOTLIN)
  implementation(LibraryDependency.RETROFIT)
  implementation(LibraryDependency.RETROFIT_MOSHI_CONVERTER)
  implementation(LibraryDependency.OK_HTTP)
  implementation(LibraryDependency.LOGGING_INTERCEPTOR)
  implementation(LibraryDependency.COROUTINES)
  testImplementation(TestLibraryDependency.COROUTINES_TEST)

  //compose
  implementation(LibraryDependency.MATERIAL_COMPOSE)
  implementation(LibraryDependency.ANIMATION_COMPOSE)
  implementation(LibraryDependency.UI_TOOLING_COMPOSE)
  implementation(LibraryDependency.VIEWMODEL_COMPOSE)
  implementation(LibraryDependency.NAVIGATION_COMPOSE)

  //di
  implementation(LibraryDependency.HILT)
  implementation(LibraryDependency.HILT_COMPOSE)
  kapt(LibraryDependency.HILT_COMPILER)

  //room
  implementation(LibraryDependency.ROOM)
  kapt(LibraryDependency.ROOM_COMPILER)
  implementation(LibraryDependency.ROOM_KTX)
  androidTestImplementation(TestLibraryDependency.ROOM_TESTING)

  testImplementation(TestLibraryDependency.JUNIT)
  androidTestImplementation(TestLibraryDependency.JUNIT_ANDROIDX)
  androidTestImplementation(TestLibraryDependency.ESPRESSO_CORE)
}