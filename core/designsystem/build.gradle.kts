plugins {
    id("bitefast.android.library")
    id("bitefast.android.compose")
}

dependencies {
    implementation(project(":core:model"))
    implementation(libs.androidx.core.ktx)
    implementation(libs.compose.icons.extended)
    implementation(libs.coil.compose)
}
