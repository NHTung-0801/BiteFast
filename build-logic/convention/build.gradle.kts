plugins {
    `kotlin-dsl`
}

group = "com.bitefast.buildlogic"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.compose.compiler.gradlePlugin)
    compileOnly(libs.ksp.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "bitefast.android.application"
            implementationClass = "com.bitefast.buildlogic.convention.AndroidApplicationConventionPlugin"
        }
        register("androidLibrary") {
            id = "bitefast.android.library"
            implementationClass = "com.bitefast.buildlogic.convention.AndroidLibraryConventionPlugin"
        }
        register("androidCompose") {
            id = "bitefast.android.compose"
            implementationClass = "com.bitefast.buildlogic.convention.AndroidComposeConventionPlugin"
        }
        register("androidHilt") {
            id = "bitefast.android.hilt"
            implementationClass = "com.bitefast.buildlogic.convention.AndroidHiltConventionPlugin"
        }
        register("jvmLibrary") {
            id = "bitefast.jvm.library"
            implementationClass = "com.bitefast.buildlogic.convention.JvmLibraryConventionPlugin"
        }
    }
}
