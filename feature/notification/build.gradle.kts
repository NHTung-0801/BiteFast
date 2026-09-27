plugins {
    id("bitefast.android.library")
    id("bitefast.android.compose")
    id("bitefast.android.hilt")
}

dependencies {
    implementation(project(":core:model"))
    implementation(project(":core:common"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:domain"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.compose.icons.extended)
    implementation(libs.kotlinx.coroutines.core)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)
}
