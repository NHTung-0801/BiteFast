plugins {
    id("bitefast.jvm.library")
}

dependencies {
    implementation(project(":core:model"))
    implementation(libs.kotlinx.coroutines.core)
    compileOnly("javax.inject:javax.inject:1")
}
