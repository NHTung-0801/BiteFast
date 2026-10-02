plugins {
    id("bitefast.android.application")
    id("bitefast.android.compose")
    id("bitefast.android.hilt")
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:model"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:domain"))
    implementation(project(":core:data"))

    implementation(project(":feature:auth"))
    implementation(project(":feature:discovery"))
    implementation(project(":feature:detail"))
    implementation(project(":feature:cart"))
    implementation(project(":feature:checkout"))
    implementation(project(":feature:tracking"))
    implementation(project(":feature:order"))
    implementation(project(":feature:profile"))
    implementation(project(":feature:rating"))
    implementation(project(":feature:notification"))
    implementation(project(":feature:voucher"))

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.compose.icons.extended)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.messaging)
}
