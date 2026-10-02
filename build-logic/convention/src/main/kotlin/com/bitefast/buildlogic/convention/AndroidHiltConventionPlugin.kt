package com.bitefast.buildlogic.convention

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

class AndroidHiltConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.google.devtools.ksp")
                apply("com.google.dagger.hilt.android")
                apply("org.jetbrains.kotlin.kapt") // KAPT cho hilt-compiler (ổn định hơn KSP trong multi-module)
            }

            val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

            dependencies {
                add("implementation", libs.findLibrary("hilt-android").get())
                // Dùng kapt thay vì ksp cho hilt-compiler để tránh bug KSP multi-round trong multi-module
                add("kapt", libs.findLibrary("hilt-compiler").get())
            }
        }
    }
}
