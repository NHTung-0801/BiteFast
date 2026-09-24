plugins {
    id("bitefast.android.library")
    id("bitefast.android.compose")
    id("org.jetbrains.kotlinx.kover")
}

dependencies {
    implementation(project(":core:model"))
    implementation(libs.kotlinx.coroutines.test)
    implementation(libs.turbine)
    implementation(libs.mockk)
    implementation(libs.junit5)
}
