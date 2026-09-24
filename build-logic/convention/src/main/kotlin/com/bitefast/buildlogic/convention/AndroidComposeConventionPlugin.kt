package com.bitefast.buildlogic.convention

import com.android.build.api.dsl.ApplicationExtension
import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

class AndroidComposeConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("org.jetbrains.kotlin.plugin.compose")

            val appExtension = extensions.findByType(ApplicationExtension::class.java)
            val libExtension = extensions.findByType(LibraryExtension::class.java)

            when {
                appExtension != null -> appExtension.buildFeatures { compose = true }
                libExtension != null -> libExtension.buildFeatures { compose = true }
                else -> error("AndroidComposeConventionPlugin must be applied after com.android.application or com.android.library")
            }

            val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

            dependencies {
                val bom = libs.findLibrary("compose-bom").get()
                add("implementation", platform(bom))
                add("implementation", libs.findLibrary("compose-ui").get())
                add("implementation", libs.findLibrary("compose-material3").get())
                add("implementation", libs.findLibrary("compose-tooling-preview").get())
                add("debugImplementation", libs.findLibrary("compose-tooling").get())
            }
        }
    }
}
